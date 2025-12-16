import * as serialPort from './serial-port.js';
import './production/cal-mode.js';
import './panel-measurement.js';
import './panel-config.js'
import './user-panels.js'
import { onStatusChange } from './panel-summary-alarm.js';
import { type as unitType, onStartAll, profileSearch, onTypeChange, onSerialChange } from './panel-info.js';

const $prodactionNav = $('input[name=prodactionNav]').change(onNavChenge);
const $productionContent = $('div#productionContent');

let controller;

onTypeChange(typeChange);
onSerialChange(serialNumberChange);
onStartAll(onStart);
function onStart(yes){
	yes ? start() : stop()
}
onStatusChange(statusChange);

function statusChange(alarmStatus){
	switch(alarmStatus.severities){

	case 'Closed':
	case 'TIMEOUT':
	case 'Stopped':
		stop();
		break;

	default:
		start();
	}
}

let interval;
function start(){
	if(interval){
		console.log('Buisy');
		return;
	}

	$prodactionNav.filter(':checked').each((_,{id})=>loadController(id));
}

function stop(){
	interval = clearInterval(interval);
}

function onNavChenge({currentTarget:{id}}){
	$prodactionNav.prop('disabled', true);
	loadController(id);
}

async function loadController(id){

	if(controller?.name !== id)
		switch(id){

		case 'cbDACs':
		{

			switch(unitType?.name){

			case 'REFERENCE_BOARD':
			{
				const {default:c} = await import('./production/controller-rcm.js');
				setController(id, c);
				break;
			}

			default:
				const {default:c} = await import('./production/controller-dacs.js');
				setController(id, c);
			}
			break;
		}

		case 'potentiometersId':
			const {default:c} = await import('./production/controller-pots.js');
			setController(id, c);
			break;

		case 'admv1013':
		{
			const {default:c} = await import('./production/controller-admv1013-converter.js');
			setController(id, c);
			break;
		}

		case 'admv1013Bias':
		{
			const {default:c} = await import('./production/controller-admv1013-bias.js');
			setController(id, c);
			break;
		}

		case 'stuw81300':
		{
			const {default:c} = await import('./production/controller-stuw81300-converter.js');
			setController(id, c);
			break;
		}

		case 'stuw81300Bias':
		{
			const {default:c} = await import('./production/controller-stuw81300-bias.js');
			setController(id, c);
			break;
		}

		case 'cbDump':
		{
			const {default:c} = await import('./production/controller-dump.js');
			setController(id, c);
			break;
		}

		default:
			controller = undefined;
			console.warn('Unknown controller:', id);
		}

	if(controller){
		run();
		clearInterval(interval);
		interval = setInterval(run, 3000);
		$prodactionNav.prop('disabled', false);
		if(unitType)
			typeChange(unitType);
	}
}

function setController(id, c){
	controller = new c($productionContent);
	controller.name = id;
	controller.onSet = onSet;
	if(unitType)
		controller.typeName = unitType.name;
}

function run(){

	if(!unitType)
		return;

	const action = controller.action;
	if(action.packetError){
		action.packetError = undefined;
		stop();
		return;
	}
	if(action.doNotSend || action.data.value===undefined){
//		console.log(action.doNotSend, action.data);
		return;
	}
	if(action.buisy){
		console.log('buisy');
		return;
	}

	action.buisy = true;

	serialPort.postObject($productionContent, action);
}

function onSet(action){
	serialPort.postObject($productionContent, action);
}

function typeChange(){
	console.log('Device Type Change:', unitType);
	const $navbar = $prodactionNav.parents('.navbar')
	const $admv = $navbar.find('.pllRegisters');
	const dType = unitType?.name;
	changeProfilePath()
	if(controller)
		controller.typeName = dType;
	switch(dType){

	case 'CONVERTER_KA':
	case 'KA_BIAS':
		if(!$admv.length){

			let admvId;
			let stuwId;
			if(dType == 'CONVERTER_KA'){
				admvId = 'admv1013';
				stuwId = 'stuw81300';
			}else{
				admvId = 'admv1013Bias';
				stuwId = 'stuw81300Bias';
			}

			// PLL 1
			const $divADMV1013 = $('<div>', {class: 'col-auto ms-1'})
			.append($('<input>', {id: admvId, name:'prodactionNav', type: 'radio', class: 'btn-check pllRegisters', autocomplete: 'off'}).change(onNavChenge))
			.append($('<label>', {for: admvId, title:'ADMV1013', text: 'ADMV 1013', class: 'btn btn-outline-primary'}))

			// PLL 2
			const $divSTUW81300 = $('<div>', {class: 'col-auto ms-1'})
			.append($('<input>', {id: stuwId, name:'prodactionNav', type: 'radio', class: 'btn-check pllRegisters', autocomplete: 'off'}).change(onNavChenge))
			.append($('<label>', {for: stuwId, title:'STUW81300', text: 'STUW 81300', class: 'btn btn-outline-primary'}))

			// Potentiometers
			const $potentiometers = $('<div>', {class: 'col-auto ms-1'})
			.append($('<input>', {id: 'potentiometersId', name:'prodactionNav', type: 'radio', class: 'btn-check', autocomplete: 'off'}).change(onNavChenge))
			.append($('<label>', {for: 'potentiometersId', title:'Potentiometers', text: 'POTs', class: 'btn btn-outline-primary'}))

			$navbar.append([$divADMV1013, $divSTUW81300, $potentiometers]);
		}
		return;

	case 'REFERENCE_BOARD':
		$prodactionNav.filter(':checked').filter((_,{id})=>id==='cbDACs').change();
		break;

	default:
		console.log(unitType);
	case 'CONTROLLER_IRPC':
	case 'CONTROLLER_ODRC':
	}

	if($admv.length){
		$prodactionNav.find('label[for=admv1013]').remove();
		$admv.remove();
	}
}

let serialNumber;
function serialNumberChange(sn){
	console.log('Serial Number Change:', sn);
	if(serialNumber===sn)
		return;
	serialNumber = sn;
	addCalibrationButton(serialNumber);
	profileSearch(showProfileButton);
}
function addCalibrationButton(sn){
	const $navBar = $prodactionNav.parents('.navbar');
	$navBar.find('.cal-link').remove();
	const $div = $('<div>', {class: 'col-auto cal-link ms-2'});
	if(admin)
		$div.append($('<a>', {class: 'btn btn-outline-info', target: '_blank', href:`http://irttechnologies:8089/calibration?sn=${sn}`, text: 'Calibration'}));
	setTimeout(()=>{
		$div.appendTo($navBar);
	}, 100);
}
function showProfileButton(data){
	if(!data?.path){
		console.warn('No profile path found', data);
		return;
	}
	if(!admin)
		return;
	const $navBar = $prodactionNav.parents('.navbar');
	$navBar.find('.btn-group').remove();
	const arr = []
	data.path.forEach(p=>{
		const $div = $('<div>', {class: 'col-auto btn-group ms-2'});
		arr.push($div);
		$('<a>', {class: 'btn btn-outline-secondary', href: `/file/open?p=${p}`, text: 'Profile', title: p}).click(linkEvent).appendTo($div);
		$('<button>', {type: 'button', class: 'btn btn-outline-secondary dropdown-toggle dropdown-toggle-split', 'data-bs-toggle': 'dropdown', 'aria-expanded': false})
		.append($('<span>', {class: 'visually-hidden', text: 'Toggle Dropdown'})).appendTo($div);
		$('<ul>', {class: 'dropdown-menu'})
		.append($('<li>').append($('<a>', {class: 'dropdown-item', href: `/file/location?p=${p}`, text: 'Location'}).click(linkEvent)))
		.append($('<li>').append($('<a>', {id: 'profilrUpload', class: 'dropdown-item', href: `/file/upload/profile?p=${p}`, text: 'Upload'}).click(updateProfile)))
		.appendTo($div);
	});
	$navBar.append(arr);
	changeProfilePath();
}
function linkEvent(e){
	e.preventDefault();
	const {currentTarget: {href}} = e;
	$.post(href)
	.done(data=>data ?? serialPort.showToast('File not found', `he file\n${href}\nnot found.`, 'text-bg-danger bg-opacity-50'))
	.fail(e=>console.log(e));
}
function updateProfile(e) {
	linkEvent(e);
	serialPort.showToast('Profile Upgrade', 'The update is starting, please wait for the profile to load.');
}
// Change profile upload path based on device type and serialNumber
function changeProfilePath(){
	const $profilrUpload = $('#profilrUpload');
	if (!$profilrUpload.length || !unitType)
		return;
	const search = $profilrUpload.attr('href').split('?')[1];
	if(unitType.name.startsWith('CONVERTER')){
		$('#profilrUpload').attr('href', `/upgrade/rest/profile/${serialPort.serialPort}/0?${search}`);
	}else
    	$('#profilrUpload').attr('href', `/file/upload/profile?${search}`);
}