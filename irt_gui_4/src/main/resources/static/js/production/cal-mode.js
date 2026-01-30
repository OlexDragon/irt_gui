import * as serialPort from '../serial-port.js'
import packetId from '../packet/packet-properties/packet-id.js'
import groupId from '../packet/packet-properties/group-id.js'
import dd from '../packet/parameter/device-debug.js'
import {onStatusChange, } from '../panel-summary-alarm.js'
import { onStartAll } from '../panel-info.js'


const $calModeDiv = $('#calModeDiv');
const $cbCalMode = $('#cbCalMode').change(onChange);
const action = {packetId: packetId.calMode, groupId: groupId.deviceDebug, data: {parameterCode: dd.calibrationMode.code}, function: 'f_calMode', f_error: packetError};

let hasError;

export let calMode;

onStartAll(yes=>{
	if(hasError)
		return;
	yes ? start() : stop()
});
let interval;
function start(){
	if(interval)
		return;
	$calModeDiv.removeClass('visually-hidden');
//	console.warn("start()");
	run();
	interval = setInterval(run, 5000);
}

function stop(){
	interval = clearInterval(interval);
	hasError = false;
}
function run(){
	if(action.buisy){
		console.log('Buisy');
		return;
	}
	action.buisy = true;
	serialPort.postObject($calModeDiv, action);
}

action.f_calMode = (packet)=>{
	packet.payloads.forEach(pl=>{
		if(pl.parameter.code != dd.calibrationMode.code){
			console.warn(pl);
			return;
		}
		calMode = dd.calibrationMode.parser(pl.data);
		if($cbCalMode.prop('checked')!==calMode || $cbCalMode.prop('disabled')){
			$cbCalMode.prop('disabled', false).prop('checked', calMode);
		}
		const text = calMode ? 'Calibration  ON' : 'Calibration  OFF';
		const title = calMode ? 'Click to set Calibration  OFF' : 'Click to set Calibration  ON';
		const $label = $cbCalMode.next();
		if($label.text()!==text){
			$label.text(text);
			$label.attr('title', title);
		}
	});
}

const actionSet = Object.assign({}, action);
actionSet.packetId = packetId.calModeSet;
function onChange({currentTarget:{checked}}){
	actionSet.update = true;
	const v = checked ? 1 : 0;
	actionSet.data.value = v;
	serialPort.postObject($calModeDiv, actionSet);
}
onStatusChange(alarmStatus =>{
	switch(alarmStatus.severities){

	case 'Closed':
	case 'TIMEOUT':
		$cbCalMode.prop('disabled', true);
	case 'Stopped':
		stop();
		break;

	default:
		if(!hasError)
			start();
	}
});
function packetError(packet){
	if(packet.header.error === 10){	// Requested element not foundr
		console.warn('The Packet has an error. Controller stops.\n', packet.toString());
		stop();
		hasError = true;
		$calModeDiv.addClass('visually-hidden');
	}else
		console.warn(packet.toString());
}
