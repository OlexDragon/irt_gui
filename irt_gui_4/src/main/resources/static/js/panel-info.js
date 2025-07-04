import * as serialPort from './serial-port.js'
import packetId from './packet/packet-properties/packet-id.js'
import groupId from './packet/packet-properties/group-id.js'
import { change as unitChange} from './panel-units.js'

const $card = $('.infoCard');
const $body = $('.info');

export let type;
const map = new Map();
const parameter = {};

let interval;
const action = {packetId: packetId.deviceInfo, groupId: groupId.deviceInfo, data: {}, function: 'f_Info'};

serialPort.onStart(onStart);
unitChange(()=>{
	stop();
	start();
})

function onStart(doRun){
	if(!doRun){
		stop();
	}
}

export function start(){
	if(interval)
		return;

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
			if(parameterCode === parameter.deviceInfo.type){
				const compar = val.filter((v,i)=>v===type[i]);
				if(compar.length !== type.length){
					$val.text(val);
				}
			}else if(val !== $val.text())
				$val.text(val);
		}else{
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
			if(type?.toString()!==val.toString())
				changeType(val);
			break;

		case parameter.deviceInfo.serialNumber:
			const title =  val + ' : IRT Technologies';
			if(document.title!==title)
				document.title = title;
		}
	});
}

function changeType(val){
	type = val;
	startAll();
	typeChangeEvents.forEach(e=>e(type));
}

const MODULES = ['./panel-measurement.js', './panel-config.js', './user-panels.js']
function startAll(){
	MODULES.forEach(u=>import(u).then(m=>m.start()));
}

function stopAll(){
	MODULES.forEach(u=>import(u).then(m=>m.stop()));
}
