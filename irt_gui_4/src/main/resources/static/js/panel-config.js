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
	interval = clearInterval(interval) ;
	buisy = false;;
}

function typeChange(type){
	if(storedUnitType && (JSON.stringify(storedUnitType)===JSON.stringify(type)))
		return;
	storedUnitType = type;
	loader.setUnitType(type, c=>onControllerLoaded(c));
	if(window.location.pathname === '/'){
		console.log('The user is not allowed to manage registers.');
		return;
	}
	switch(type.name){
		case 'LNB':
		case 'CONTROLLER_ODRC':
			if(type.revision>30)
				import('./controller/controller-lnb-registers.js')
			        .then(({default: Controller})=>{
						controller2 = new Controller($card);
					});
//			interval = clearInterval(interval) ;
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
	if(controller?.constructor.name !== Controller.name || controller?.parameter?.constructor.name !== loader.parameter.name){
		controller = new Controller($card);
		controller.parametersClass = new loader.parameter();
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

	if(action.buisy || !controller?.parametersClass){
		console.log(action.buisy ?'Buisy' : 'No data to send');
		return
	}

	const toRead = controller.toRead;
	if(!toRead){
		console.log('No data to Read')
		return;
	}

	action.buisy = true;

	if(action.data.parameterCode?.toString() !== toRead?.toString()){
		action.update = true;
		action.data.parameterCode = toRead;
	}
	if(action.data.parameterCode.length)
		serialPort.postObject($card, action);
	else
		stop();
}

action.f_Config = function(packet){
	if(!controller)
		return;

	controller.update = packet.payloads;
}

let actionSet;
function onChange(packetId, value, parameterCode, groupId){
	actionSet.update = true;
	actionSet.groupId = groupId ?? controller.groupId
	actionSet.packetId = packetId;
	actionSet.data.value = value;
	actionSet.data.parameterCode = parameterCode;
	actionSet.command = true;
	serialPort.postObject($card, actionSet);
}

export function update(object){
	controller.update = object;
}
