import * as serialPort from './serial-port.js'
import { onTypeChange, onStartAll } from './panel-info.js'
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

onStartAll(yes=>yes ? start() : stop())
onTypeChange(typeChange);
export function start(){
	if(interval || buisy)
		return;

	buisy = true;
	action.buisy = false;
	if(action.packetId){
		clearInterval(interval);
		run();
		interval = setInterval(run, 3000);
	}
}

export function stop(){
	clearInterval(interval);
	interval = undefined;
	buisy = false;
}

function typeChange(type){
	loader.setUnitType(type, c=>onControllerLoaded(c));
}

function onControllerLoaded(Controller){
	if(!Controller){
		console.log('The Controller is not ready.')
		return;
	}
	if(controller?.constructor.name !== Controller.name || controller?.parameter.constructor.name !== loader.parameter.name){
		!hasPlaceholder && $body.empty();
		controller = new Controller($card);
		controller.parametersClass = new loader.parameter();
		action.packetId = loader.packetId;
		action.groupId = controller.groupId;
		action.data.parameterCode = 255;
	}

	buisy = false;
	start();

//	run();
//	clearInterval(interval);
//	interval = setInterval(run, 3000);
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

let hasPlaceholder = true;
action.f_measurement = (packet)=>{
		if(hasPlaceholder){
			$card.find('.placeholder').length && $body.empty();
			hasPlaceholder = false;
		}
		controller && (controller.update = packet.payloads);
	};