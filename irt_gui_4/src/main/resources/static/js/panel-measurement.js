import * as serialPort from './serial-port.js'
import f_deviceType from './packet/service/device-type.js'
import { type, onTypeChange } from './panel-info.js'
import MeasurementLoader from './helper/measurement-loader.js'
import groupId from './packet/packet-properties/group-id.js'

const $card = $('.measurementCard');
const $body = $('.measurement');
const action = {data: {}, function: 'f_measurement'};
action.groupId = groupId.measurement;

let controller;

let loader = new MeasurementLoader();
let interval;
let buisy;
export function start(){
	if(interval || buisy)
		return;

	buisy = true;
	action.buisy = false;
	if(action.packetId){
		clearInterval(interval);
		interval = setInterval(run, 3000);
	}else{
		 if(type)
			typeChange(type);
		onTypeChange(typeChange);
	}
}

export function stop(){
	clearInterval(interval);
	interval = undefined;
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
		action.packetId = loader.packetId;
		action.groupId = controller.groupId;
		action.data.parameterCode = loader.parameter.default.all.code;
	}

	buisy = false;

	run();
	clearInterval(interval);
	interval = setInterval(run, 3000);
}

function run(){
	if(!serialPort.doRun()){
		stop();
		return;
	}

	if(action.buisy){
		console.log('Buisy')
		return;
	}

	action.buisy = true;

	serialPort.postObject($card, action);
}

action.f_measurement = (packet)=>controller && (controller.update = packet.payloads);