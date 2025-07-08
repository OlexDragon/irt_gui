import * as serialPort from './serial-port.js'
import f_unitType from './packet/service/device-type.js'
import packetId from './packet/packet-properties/packet-id.js'
import groupId from './packet/packet-properties/group-id.js'
import { onTypeChange } from './panel-info.js'

const $unitsSelect = $('#unitsSelect');

const chengeEvents = [];
const map = new Map();
const parameter = {};
let interval;

const action = {packetId: packetId.module, groupId: groupId.control, data: {}, function: 'f_modules'};

setTimeout(typeChange, 100);
let type;
function typeChange(){
	onTypeChange(t=>{
		const str = t.toString();
		if(type!==str){
			type = str;
			action.data.parameterCode = undefined;
			clear();
			stop();
			start();
		}
	});
}
export function start(){

	if(interval)
		return;

	action.buisy = false;
	action.packetError = undefined;

	const unitType = f_unitType();
	switch(unitType){
	case 'CONVERTER':
		return;
	}

	getParser();
}

export function stop(){
	interval = clearInterval(interval)
}

export function disable(d){
	Array.from(map.values()).forEach($el=>$el.prop('disabled', d));
}

export function change(e){
	chengeEvents.push(e);
}

async function getParser(){

	if(!parameter.parser){
		const {default: config, parser} = await import('./packet/parameter/config-buc.js');
		parameter.config = config;
		parameter.parser = parser;
	}

	if(!action.data.parameterCode)
		action.data.parameterCode = parameter.config.moduleList;
	run();
	clearInterval(interval);
	interval = setInterval(run, 10000);
}

function run(){
	if(action.packetError){
		stop();
		clear();
		return;
	}

	if(!serialPort.doRun()){
		stop();
		return;
	}

	if(action.buisy){
		console.log('Buisy')
		return
	}

	action.buisy = true;

	serialPort.postObject($unitsSelect, action);
}

action.f_modules = (packet) =>{

	packet.payloads.forEach(pl=>{
		switch(pl.parameter.code){

		case parameter.config.moduleList:
			action.update = true;
			action.data.parameterCode = parameter.config.activeModule;
			if(addElements(pl.data))
				run();
			else
				stop();
			break;

		case parameter.config.activeModule:
			if(!map.size){
				console.log('This Panel is not ready yet.')
				return;
			}
			const key = parameter.parser(parameter.config.activeModule)(pl.data)
			const $buttom = map.get(key);
			if(!$buttom)
				break;
			if(!$buttom.prop('checked'))
				$buttom.prop('checked', true);

			if(moduleChange){
				moduleChange = false;
				disable(false);
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
	const entries = Object.entries(modules);
	if(entries.length<2)
		return false;
	const columns = entries.sort((a,b)=>a[1]-b[1]).map(([k, v])=>{
		const id = 'module' + v;
		const $button = $('<input>', {type: 'radio', class: 'btn-check', name: 'moduleConnect', id: id, autocomplete: 'off', value: v}).change(onChange);
		map.set(v, $button);
		const $col = $('<div>', {class: 'col'}).append($button).append($('<label>', {for: id, class: 'btn btn-outline-primary form-control', text: k}));
		return $col;
	});
	$unitsSelect.append(columns);
	return true;
}

let moduleChange;
function onChange({currentTarget:{value}}){

	disable(true);
	const a = Object.assign({}, action);
	a.packetId = packetId.moduleSet;
	a.update = true;
	a.data.value = +value;
	serialPort.postObject($unitsSelect, a);
	moduleChange = true;
}

function clear(){
	map.clear();
	$unitsSelect.empty();
}