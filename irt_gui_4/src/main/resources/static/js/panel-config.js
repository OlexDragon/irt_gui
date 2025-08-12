import * as serialPort from './serial-port.js'
import f_deviceType from './packet/service/device-type.js'
import { type as unitType, onTypeChange, onStartAll } from './panel-info.js'
import ControlLoader from './helper/config-loader.js'

const $card = $('div.controlCard');
const $body = $('div.control');

const action = {data: {}, function: 'f_Config'};

let controller;

let loader = new ControlLoader();
let interval;
let buisy;
const DELAY = 5000;

onStartAll(yes=>yes ? start() : stop())
export function start(){
	if(interval || buisy)
		return;

	buisy = true;;
	action.buisy = false;
	if(action.packetId){
		clearInterval(interval) ;
		interval = setInterval(run, DELAY);
	}else{
		if(unitType)
			typeChange(unitType);
		onTypeChange(typeChange);
	}
}

function typeChange(type){
	loader.setUnitType(f_deviceType(type[0]), c=>onControllerLoaded(c));
}

function onControllerLoaded(Controller){
	if(!Controller){
		console.log('This Controller is not ready.')
		return;
	}
	if(controller?.name !== Controller.name){
		$body.empty();
		controller = new Controller($card);
		controller.name = Controller.name;
		controller.parameter = loader.parameter;
		controller.toRead = loader.toRead;
		controller.change = onChange;
		action.packetId = loader.packetId
		action.groupId = controller.groupId
		actionSet = Object.assign({}, action);
		actionSet.data = {};
	}

	buisy = false;

	run();
	clearInterval(interval) ;
	interval = setInterval(run, DELAY);
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

	if(action.data.parameterCode?.toString() !== controller.toRead?.toString()){
		action.update = true;
		action.data.parameterCode = controller.toRead;
	}
	serialPort.postObject($card, action);
}

export function stop(){
	clearInterval(interval) ;
	interval = undefined;
}

action.f_Config = function(packet){

	const payloads = packet.payloads;
	controller.update = payloads;
}

let actionSet;
function onChange(packetId, value, parameterCode){
	actionSet.update = true;
	actionSet.packetId = packetId;
	actionSet.data.value = value;
	actionSet.data.parameterCode = parameterCode;
	actionSet.command = true;
	serialPort.postObject($card, actionSet);
}

export function update(object){
	controller.update = object;
}
