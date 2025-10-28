import * as serialPort from './serial-port.js'
import './production/cal-mode.js'
import './panel-measurement.js'
import { onStatusChange } from './panel-summary-alarm.js'
import { type, onStartAll, profileSearch, onTypeChange, onSerialChange } from './panel-info.js'
import f_deviceType from './packet/service/device-type.js'

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

			switch(dType){

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
		if(type)
			typeChange(type);
	}
}

function setController(id, c){
	controller = new c($productionContent);
	controller.name = id;
	controller.onSet = onSet;
	if(dType)
		controller.typeName = dType;
}

function run(){

	if(!type)
		return;

	const action = controller.action;
	if(action.packetError){
		action.packetError = undefined;
		stop();
		return;
	}
	if(action.doNotSend || action.data.value===undefined){
		console.warn(action.doNotSend, action.data);
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

let dType;
function typeChange(type){
	const $navbar = $prodactionNav.parents('.navbar')
	const $admv = $navbar.find('#admv1013');
	const t = f_deviceType(type[0]);
	if(t===dType)
		return;
	dType = t;
	if(controller)
		controller.typeName = dType
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
			.append($('<input>', {id: admvId, name:'prodactionNav', type: 'radio', class: 'btn-check', autocomplete: 'off'}).change(onNavChenge))
			.append($('<label>', {for: admvId, title:'ADMV1013', text: 'ADMV 1013', class: 'btn btn-outline-primary'}))

			// PLL 2
			const $divSTUW81300 = $('<div>', {class: 'col-auto ms-1'})
			.append($('<input>', {id: stuwId, name:'prodactionNav', type: 'radio', class: 'btn-check', autocomplete: 'off'}).change(onNavChenge))
			.append($('<label>', {for: stuwId, title:'STUW81300', text: 'STUW 81300', class: 'btn btn-outline-primary'}))

			$navbar.append([$divADMV1013, $divSTUW81300]);
		}
		return;

	case 'REFERENCE_BOARD':
		$prodactionNav.filter(':checked').filter((_,{id})=>id==='cbDACs').change();
		break;

	default:
		console.log(type, dType);
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
	if(serialNumber===sn)
		return;
	serialNumber = sn;
	addCalibrationButton(serialNumber);
	try{
		const data = Cookies.get(`profilePath${serialNumber}`);
		if(data){
			showProfileButton(JSON.parse(data));
			return;
		}
	}catch (e){
		console.log(e);
	}
	profileSearch(profilePath);
}
function addCalibrationButton(sn){
	const $navBar = $prodactionNav.parents('.navbar');
	$navBar.find('.cal-link').remove();
	const $div = $('<div>', {class: 'col-auto cal-link ms-2'});
	if(admin)
		$div.append($('<a>', {class: 'btn btn-outline-info', target: '_blank', href:`http://irttechnologies:8089/calibration?sn=${sn}`, text: 'Calibration'}));
	$div.appendTo($navBar);
}
function profilePath(profilePath){
	if(!profilePath?.path){
		console.warn(profilePath);
		return;
	}
	Cookies.set(`profilePath${serialNumber}`, JSON.stringify(profilePath));
	showProfileButton(profilePath);
}
function showProfileButton(data){
	if(!admin)
		return;
	const $navBar = $prodactionNav.parents('.navbar');
	$navBar.find('.btn-group').remove();
	const arr = []
	data.path.forEach(p=>{
		const $div = $('<div>', {class: 'btn-group ms-2'});
		arr.push($div);
		$('<a>', {class: 'btn btn-outline-secondary', href: `/file/open?p=${p}`, text: 'Profile', title: p}).click(linkEvent).appendTo($div);
		$('<button>', {type: 'button', class: 'btn btn-outline-secondary dropdown-toggle dropdown-toggle-split', 'data-bs-toggle': 'dropdown', 'aria-expanded': false})
		.append($('<span>', {class: 'visually-hidden', text: 'Toggle Dropdown'})).appendTo($div);
		$('<ul>', {class: 'dropdown-menu'})
		.append($('<li>').append($('<a>', {class: 'dropdown-item', href: `/file/location?p=${p}`, text: 'Location'}).click(linkEvent)))
		.append($('<li>').append($('<a>', {class: 'dropdown-item', href: `/file/upload/profile?p=${p}`, text: 'Upload'}).click(linkEvent)))
		.appendTo($div);
	});
	if(arr.length)
		$navBar.append(arr);
}
function linkEvent(e){
	e.preventDefault();
	const {currentTarget: {href}} = e;
	$.get(href)
	.done(data=>data ?? serialPort.showToast('File not found', `he file\n${href}\nnot found.`, 'text-bg-danger bg-opacity-50'))
	.fail(e=>console.log(e));
	serialPort.showToast('Profile Upgrade', 'The update is starting, please wait for the profile to load.');
}
