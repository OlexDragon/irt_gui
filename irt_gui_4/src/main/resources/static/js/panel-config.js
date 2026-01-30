import * as serialPort from './serial-port.js'
import { type as unitType, onStartAll, onTypeChange } from './panel-info.js'
import ControlLoader from './helper/config-loader.js'

const $card = $('div.controlCard');

const action = {data: {}, function: 'f_Config'};

let controller;
let controller2;

let loader = new ControlLoader();
let interval;
let buisy;
const DELAY = 5000;

onStartAll(yes=>{
	if(yes){
		start();
		if(controller2)
			controller2.start();
	}else{
		stop();
	if(controller2)
		controller2.stop();
	}
})
let storedUnitType;
onTypeChange(type=>{
	if(storedUnitType && (JSON.stringify(storedUnitType)===JSON.stringify(type)))
		return;
	if(controller){
		console.log(controller.constructor.name)
		controller.stop();
		controller.destroy();
		controller = null;
	}
	if(controller2){
		controller2.stop();
		controller2.destroy();
		controller2 = null;
	}
	typeChange(type);
});
export function start(){
	if(interval || buisy)
		return;

	buisy = true;;
	action.buisy = false;
	if(action.packetId){
		clearInterval(interval) ;
		run()
		interval = setInterval(run, DELAY);
	}else{
		if(unitType)
			typeChange(unitType);
	}
}

export function stop(){
	clearInterval(interval) ;
	interval = undefined;
	buisy = false;;
}

function typeChange(type){
	if(storedUnitType && (JSON.stringify(storedUnitType)===JSON.stringify(type)))
		return;
	storedUnitType = type;
	loader.setUnitType(type, c=>onControllerLoaded(c));
	const pathname = window.location.pathname;
	if(pathname === '/')
		return;
	switch(type.name){
		case 'LNB':
		case 'CONTROLLER_ODRC':
			if(type.revision>30)
				import('./controller/controller-lnb-registers.js')
			        .then(({default: Controller})=>{
						controller2 = new Controller($card);
					});
	        break;

		default:
			if(controller2){
				controller2.destroy();
				controller2 = undefined;
			}
		}
}

function onControllerLoaded(Controller){
	if(!Controller){
		console.log('Controller is not ready.')
		return;
	}
	if(controller?.constructor.name !== Controller.name){
		controller = new Controller($card);
		controller.parameter = loader.parameter;
		controller.toRead = loader.toRead;
		controller.change = onChange;
		action.packetId = loader.packetId
		action.groupId = controller.groupId
		actionSet = Object.assign({}, action);
		actionSet.data = {};
		action.update = true;
	}

	buisy = false;

	start();
}

function run(){
	if(!serialPort.doRun()){
		stop();
		return;
	}

	if(action.buisy || !controller){
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

action.f_Config = function(packet){
	if(!controller)
		return;

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
