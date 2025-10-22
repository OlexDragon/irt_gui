import * as serialPort from '../serial-port.js'
import packetId from '../packet/packet-properties/packet-id.js'
import groupId from '../packet/packet-properties/group-id.js'
import dd from '../packet/parameter/device-debug.js'
import {onStatusChange} from '../panel-summary-alarm.js'

serialPort.onStart(spOnStart);

const $calModeDiv = $('#calModeDiv');
const $cbCalMode = $('#cbCalMode').change(onChange);
const action = {packetId: packetId.calMode, groupId: groupId.deviceDebug, data: {parameterCode: dd.calibrationMode.code}, function: 'f_calMode'};

export let calMode;

function spOnStart(yes){
	yes ? start() : stop()
}
let interval;
function start(){
console.log("start()")
	if(interval)
		return;
	interval = setInterval(run, 5000);
}

function stop(){
	interval = clearInterval(interval);
}

function run(){
	if(action.packetError){
		action.packetError = undefined;
		stop();
		return;
	}
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
		const text = calMode ? 'Cal. ON' : 'Cal. OFF';
		const $label = $cbCalMode.next();
		if($label.text()!==text)
			$label.text(text);
		
	});
}

const actionSet = Object.assign({}, action);
actionSet.packetId = packetId.calModeSet;
function onChange({currentTarget:{checked}}){
	actionSet.update = true;
	const v = checked ? 1 : 0;
	actionSet.data.value = v;
	serialPort.postObject($cbCalMode, actionSet);
}
onStatusChange(alarmStatus =>{
	switch(alarmStatus.severities){

	case 'Closed':
	case 'TIMEOUT':
	case 'Stopped':
		stop();
		$cbCalMode.prop('disabled', true);
		break;

		default:
			start();
	}
});