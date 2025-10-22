import * as serialPort from './serial-port.js'
import { change as unitChange} from './panel-units.js'
import packetId from './packet/packet-properties/packet-id.js'
import groupId from './packet/packet-properties/group-id.js'
import { onStatusChange } from './panel-summary-alarm.js'

const $card = $('.infoCard');
const $body = $('.info');

export let type;
let serialNumber

const map = new Map();
const parameter = {};

let started;
let interval;
const action = {packetId: packetId.deviceInfo, groupId: groupId.deviceInfo, data: {}, function: 'f_Info'};

onStatusChange(statusChange);

function statusChange(alarmStatus){
	const doRun = alarmStatus.index !== 7 && alarmStatus.index !== 8;
	if(doRun && serialPort.doRun())
		start();
	else{
		stop();
	}
}

unitChange(()=>{
	stop();
	start();
})

serialPort.onStart(onStart);
function onStart(doRun){
	
	if(doRun)
		console.log('start')
//		start();
	else
		stop();
}

let emptyCard;
export function start(){
	emptyCard = $card.find('.placeholder').length;

	if(interval)
		return;

	started = true;
	action.buisy = false;
	getParameter();
}

export function stop(){
	interval = clearInterval(interval) ;
	stopAll();
}

const typeChangeEvents = new Set();
export function onTypeChange(e){
	typeChangeEvents.add(e);
}

const serialNumberChangeEvents = new Set();
export function onSerialChange(e){
	serialNumberChangeEvents.add(e);
}

async function getParameter(){

	if(!parameter.parser){
		const {default: deviceInfo, description, comparator, parser} = await import('./packet/parameter/device-info.js');
		parameter.deviceInfo = deviceInfo;
		parameter.parser = parser;
		parameter.description = description;
		parameter.comparator = comparator;
		action.data.parameterCode = parameter.deviceInfo.all;
	}
	run();
	clearInterval(interval);
	interval = setInterval(run, 5000);
}

function run(){
	if(!serialPort.doRun()){
		stop();
		return;
	}

	if(action.buisy){
		console.log('Buisy')
		return
	}

	action.buisy = true;

	serialPort.postObject($card, action);
}

action.f_Info = function(packet){

	const payloads = packet.payloads;

	let timeout;
	if(!map.size)
		payloads.sort(parameter.comparator);

	payloads.forEach(pl=>{

		const parameterCode = pl.parameter.code;
		const $row = map.get(parameterCode);
		const valId = 'infoVal' + parameterCode;
		const descrId = 'infoDescr' + parameterCode;
		const parser = parameter.parser(parameterCode);
		if(!parser){
			console.warn('No parser. (Parameter code: )' + parameterCode)
			return;
		}
		const val = parser(pl.data);
		if($row?.length){
			const $val = $row.find('#' + valId);
			switch(parameterCode){
			case parameter.deviceInfo.type:
				const compar = val.filter((v,i)=>v===type[i]);
				if(compar.length !== type.length){
					$val.text(val);
				}
				break;

			default:
				if(val !== $val.text())
					$val.text(val);
			}
		}else{
			if(emptyCard){
				emptyCard = false;
				$body.empty();
			}
			const showText = parameter.description(parameterCode);
			const $row = $('<div>', {class: 'row'});

			let $v;
			if(showText && showText !== 'Description'){
				$row.append($('<div>', {id: descrId, class: 'col-5', text: showText}));
				if(parameterCode===5)	// Serial Number
					$v = $('<div>', {class: 'col'}).append($('<a>', {id: valId, text: val, target: '_blank', href: `http://${val}`}));
				else
					$v = $('<div>', {id: valId, class: 'col', text: val});
			}else
				$v =$('<div>', {id: descrId, class: 'col'}).append($('<h4>', {id: valId,text: val}));

			$row.append($v);

			map.set(parameterCode, $row);
			clearTimeout(timeout);
			timeout = setTimeout(()=>$body.append(Array.from(map.values())), 100);
		}
		switch(parameterCode){

		case parameter.deviceInfo.type:
			if(started || type?.toString()!==val.toString()){
				started = false;
				changeType(val);
			}
			break;

		case parameter.deviceInfo.serialNumber:
			if(serialNumber !== val){
				serialNumber = val;
				import('./fw-upgrade.js').then(m=>m.setSerialNumber(val));
				serialNumberChangeEvents.forEach(cb=>cb(val));
				if($row)
					$row.find(`#${valId}`).attr('href', `http://${val}`);
			}
			break;

		case parameter.deviceInfo.serialNumber:
			const split = document.title.split(' - ');
			if(val!==split[0])
				document.title = val + ' - ' + split[1];
		}
	});
}

function changeType(val){
	type = val;
	startAll();
	typeChangeEvents.forEach(e=>e(type));
}

const onStartEvent = [];
export function onStartAll(cb){
	onStartEvent.push(cb);
}

function startAll(){
	onStartEvent.forEach(cb=>cb(true));
}

function stopAll(){
onStartEvent.forEach(cb=>cb(false));
}

export function profileSearch(cb){
	if(!serialNumber){
		setTimeout(()=>getProfilePath(cb), 100);
		return;
	}
	getProfilePath(cb);
}

function getProfilePath(cb){
	if(!serialNumber){
		cb({warn: 'No serial number'});
		return;
	}
	$.get('/file/path/profile', {sn: serialNumber})
	.done(data=>{
		cb({path: data})
	})
}