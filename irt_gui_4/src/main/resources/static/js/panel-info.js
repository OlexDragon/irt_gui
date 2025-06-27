import * as serialPort from './serial-port.js'
import packetId from './packet/packet-properties/packet-id.js'
import groupId from './packet/packet-properties/group-id.js'
import { start as measStart, stop as measStop} from './panel-measurement.js'
import { start as controlStart, stop as controlStop} from './panel-config.js'
import { start as userStart, stop as userStop} from './user-panels.js'

const $card = $('.infoCard');
const $body = $('.info');

export let type;
const map = new Map();
const parameter = {};

let interval;
const action = {packetId: packetId.deviceInfo, groupId: groupId.deviceInfo, data: {}, function: 'f_Info'};

serialPort.onStart(onStart);

function onStart(doRun){
	if(!doRun){
		stopAll();
	}
}

export function start(){
	if(interval)
		return;

	action.buisy = false;
	getParameter();
	run();
	interval = setInterval(run, 5000);
}

export function stop(){
	clearInterval(interval) ;
	interval = undefined;
}

const typeChangeEvents = new Set();
export function onTypeChange(e){
	typeChangeEvents.add(e);
}

function stopAll(){
	stop(); measStop(); controlStop(); userStop();
}
async function getParameter(){

	if(!parameter.parser){
		const {default: deviceInfo, description, comparator, parser} = await import('./packet/parameter/device-info.js');
		parameter.deviceInfo = deviceInfo;
		parameter.parser = parser;
		parameter.description = description;
		parameter.comparator = comparator;
		action.data.parameterCode = parameter.deviceInfo.all;
	}else
		startAll();
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

	if(packet.header.error){
		console.warn(packet.toString());
		serialPort.blink($card, 'connection-wrong');
		if(showError)
			serialPort.showToast("Packet Error", packet.toString());
		return;
	}

	const payloads = packet.payloads;

	if(!payloads?.length){
		console.log(packet.toString());
		console.warn('No payloads to parse.');
		serialPort.blink($card, 'connection-wrong');
		return;
	}

	serialPort.blink($card);

	let timeout;
	if(!map.size)
		payloads.sort(parameter.comparator);

	payloads.forEach(pl=>{

		const $row = map.get(pl.parameter.code);
		const valId = 'infoVal' + pl.parameter.code;
		const descrId = 'infoDescr' + pl.parameter.code;
		const parser = parameter.parser(pl.parameter.code);
		if(!parser){
			console.warn('No parser. (Parameter code: )' + pl.parameter.code)
			return;
		}
		if($row?.length){
			const val = parser(pl.data);
			const $val = $row.find('#' + valId);
			if(pl.parameter.code === parameter.deviceInfo.type){
				const compar = val.filter((v,i)=>v===type[i]);
				if(compar.length !== type.length){
					$val.text(val);
					chashType(val);
				}
			}else if(val !== $val.text())
				$val.text(val);
		}else{
			const showText = parameter.description(pl.parameter.code);
			const $row = $('<div>', {class: 'row'});
			const val = parser(pl.data);
			if(pl.parameter.code === parameter.deviceInfo.type){
				chashType(val);`	`
			}

			let $v;
			if(showText && showText !== 'Description'){
				$row.append($('<div>', {id: descrId, class: 'col-5', text: showText}));
				if(pl.parameter.code===5)	// Serial Number
					$v = $('<div>', {class: 'col'}).append($('<a>', {id: valId, text: val, target: '_blank', href: `http://${val}`}));
				else
					$v = $('<div>', {id: valId, class: 'col', text: val});
			}else
				$v =$('<div>', {id: descrId, class: 'col'}).append($('<h4>', {id: valId,text: val}));

			$row.append($v);

			map.set(pl.parameter.code, $row);
			clearTimeout(timeout);
			timeout = setTimeout(()=>$body.append(Array.from(map.values())), 100);
		}
	});
}

function chashType(val){
	type = val;
	startAll();// controlStart();
	typeChangeEvents.forEach(e=>e(type));
}

function startAll(){
	measStart(); controlStart(); userStart();
}
