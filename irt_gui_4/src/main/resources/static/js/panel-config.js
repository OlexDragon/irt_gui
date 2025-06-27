import * as serialPort from './serial-port.js'
import f_deviceType from './packet/service/device-type.js'
import { onTypeChange } from './panel-info.js'
import ControlLoader from './helper/config-loader.js'

const $card = $('div.controlCard');
const $body = $('div.control');

const action = {data: {}, function: 'f_Config'};

let controller;

let loader = new ControlLoader();
let interval;
let buisy;
export function start(){
	if(interval || buisy)
		return;

	buisy = true;;
	action.buisy = false;
	if(action.packetId){
		interval = setInterval(run, 3000);
	}else
		onTypeChange(typeChange);
}

function typeChange(type){
	$body.empty();
	loader.setUnitType(f_deviceType(type[0]), c=>onControllerLoaded(c));
}

let controllerName;
function onControllerLoaded(Controller){
	if(controllerName !== Controller.name){
		controllerName = Controller.name;
		controller = new Controller($card);
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
	interval = setInterval(run, 3000);
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

let type;

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
	serialPort.postObject($card, actionSet);
}

export function update(object){
	controller.update = object;
}
export {type}
