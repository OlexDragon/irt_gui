import f_unitType from './packet/service/device-type.js'
import Packet from './packet/packet.js'
import f_toSend from './to-send.js'
import packetId from './packet/packet-properties/packet-id.js'
import {run as doRun, showError} from './serial-port.js'

const $unitsSelect = $('#unitsSelect');

const chengeEvents = [];
const map = new Map();
const parameter = {};
let interval;

export function start(){

	if(interval)
		return;

	const unitType = f_unitType();
	switch(unitType){
	case 'CONVERTER':
		return;
	}

	getParser();

	map.clear();
	$unitsSelect.empty();
	interval = setInterval(run, 10000);
}

export function stop(){
	clearInterval(interval)
}

export function disable(){
	Array.from(map.values()).forEach($el=>$el.prop('disabled', true));
}

export function change(e){
	chengeEvents.push(e);
}

async function getParser(){

	if(!parameter.parser){
		const {default: config, parser} = await import('./packet/parameter/config-buc.js');
		parameter.config = config;
		parameter.parser = parser;
		parameterToGet = parameter.config.moduleList;
		run();
	}
}

let parameterToGet;
let buisy;
function run(){
	if(!serialPort.doRun()){
		stop();
		return;
	}

	if(buisy){
		console.log('Buisy')
		return
	}

	buisy = doRun;

	if(!doRun)
		return;
	f_toSend(packetId.module, callBack, parameterToGet);
}

function callBack(toSend){

	toSend.function = 'f_modules';

	postObject('/serial/send', toSend)
	.done(data=>{
		buisy = false;
		module[data.function](new Packet(data.answer, true));
	})
	.fail((jqXHR)=>{
		buisy = false;

		if(jqXHR.responseJSON?.message){
			if(showError)
				serialPort.showToast(jqXHR.responseJSON.error, jqXHR.responseJSON.message, 'text-bg-danger bg-opacity-50');
		}
	});
}

const module = {}
module.f_modules = (packet) =>{

	if(![packetId.module, packetId.moduleSet].includes(packet.header.packetId)){
		console.log(packet);
		console.warn('Received wrong packet.');
		return;
	}

	if(packet.header.error){
		stop();
		const packetStr = packet.toString();
		console.error(packetStr);
		if(showError)
			serialPort.showToast('Packet Error', packetStr, 'text-bg-danger bg-opacity-50');

		return;
	}

	packet.payloads.forEach(pl=>{
		switch(pl.parameter.code){

		case parameter.config.moduleList:
			parameterToGet = parameter.config.activeModule;
			addElements(pl.data);
			run();
			break;

		case parameter.config.activeModule:
			const key = parameter.parser(parameter.config.activeModule)(pl.data)
			const $buttom = map.get(key);
			if(!$buttom.prop('checked'))
				$buttom.prop('checked', true);

			if(moduleChange){
				moduleChange = false;
				Array.from(map.values()).forEach($el=>$el.prop('disabled', false));
				chengeEvents.forEach(e=>e());
			}		
			break;

		default:
			console.warn(packet.toString());
		}
	});
}

function addElements(data){
	const modules = parameter.parser(parameter.config.moduleList)(data);
	const columns = Object.entries(modules).sort((a,b)=>a[1]-b[1]).map(([k, v])=>{
		const id = 'module' + v;
		const $butten = $('<input>', {type: 'radio', class: 'btn-check', name: 'moduleConnect', id: id, autocomplete: 'off', value: v}).change(onChange);
		map.set(v, $butten);
		const $col = $('<div>', {class: 'col'}).append($butten).append($('<label>', {for: id, class: 'btn btn-outline-primary form-control', text: k}));
		return $col;
	});
	$unitsSelect.append(columns);
}

let moduleChange;
function onChange(e){
	disable();
	f_toSend(packetId.moduleSet, toSend=>{callBack(toSend)}, e.currentTarget.value);
	moduleChange = true;
}