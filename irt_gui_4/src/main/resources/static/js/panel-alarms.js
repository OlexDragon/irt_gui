import * as serialPort from './serial-port.js'
import groupId from './packet/packet-properties/group-id.js'
import packetId from './packet/packet-properties/packet-id.js'
import {code, parser} from './packet/parameter/alarm.js'
import {type} from './panel-info.js'

const $card = $('#userCard');
const $body = $('#alarms-tab-pane');
const codeIdIDs = code('IDs');
const codeIdDescription = code('description');
const codeIdStatus = code('status');

const action = { groupId: groupId.alarm, data: {parameterCode: codeIdIDs }, function: 'f_Alarms'};

//const packetIdSummary = f_PacketId('alarmSummary');
//const packetIdAlarmIDs = f_PacketId('alarmIDs');
//const packetIdAlarmDescription = f_PacketId('alarmDescription');
//const packetIdAlarm = f_PacketId('alarm');

let unitType;

let interval;
let delay = 5000;
const map = new Map();

export function start(){

	if(interval)
		return;

	action.buisy = false;
	if(unitType?.toString() !== type.toString()){
		action.IDs = undefined;
		action.packetId = packetId.alarmIDs
		action.data.parameterCode = codeIdIDs;
		readAlarmDescription = true;
		descriptionIndex = 0;
		unitType = type;
		$body.empty();
		map.clear();
	}
	run();
	clearInterval(interval) ;
	interval = setInterval(run, delay);
}

export function stop(){
	clearInterval(interval) ;
	interval = undefined;
	action.IDs = undefined;
}

let readAlarmDescription = true;
function run(){
	if(!serialPort.doRun()){
		stop();
		return;
	}

	if(action.buisy){
		console.warn('action.buisy');
		return
	}

	action.buisy = true;

	if(action.IDs){

		if(readAlarmDescription)
			getAlarmDescription();
		else
			serialPort.postObject($card, action);

	}else{

		serialPort.postObject($card, action);
	}

}

let alarmIndex;
action.f_Alarms = function(packet){
	alarmIndex = -1;
	packet.payloads.forEach(parseAlarm);
}
function parseAlarm(pl){

	switch(pl.parameter.code){
	case codeIdIDs:
		action.IDs = action.data.value = parser(pl.parameter.code)(pl.data);
		action.packetId = packetId.alarmDescription;
		action.data.parameterCode = codeIdDescription;
		run();
		break; 

	case codeIdDescription:
		showDescription(pl);
		getAlarmDescription();
		break;

//	case codeIdName:
//		showName(pl);
//		break;

	case codeIdStatus:
		showValue(pl);
		break;

	default:
		console.warn(pl);
	}
}
function showValue(pl){
	const value = parser(pl.parameter.code)(pl.data);
	const $row = getRow(value.id);
	const $div = $row.find('.value');
	if($div.text()!==value.text)
		$div.text(value.text);
	if(!$div.hasClass(value.boorstrapClass))
		removeCalsses($div).addClass('col ' + value.boorstrapClass)

	if(value.index>alarmIndex)
		alarmIndex = value.index;
}

function showDescription(pl){
	const value = parser(pl.parameter.code)(pl.data);
	const $row = getRow(value.id);
	const $div = $row.find('.name');
	if($div.text()!==value.string)
		$div.text(value.string);
}

let timeout;
function getRow(id){
	let $row = map.get(id);
	if(!$row?.length){
		$row = $('<div>', {id: 'row' + id, class: 'row mt-1'})
				.append($('<div>', {class: 'col name text-end fw-bold'}))
				.append($('<div>', {class: 'col value text-center fs-6'}));

	map.set(id, $row);
	clearTimeout(timeout);
	timeout = setTimeout(()=>$body.append(Array.from(map.values())), 100);
	}
	return $row;
}

function removeCalsses($el){
	const classes = $el.attr('class').split(' ').filter(c=>c.startsWith('text-bg-')).join(' ');
	$el.removeClass(classes);
	return $el;
}

let descriptionIndex = 0;
function getAlarmDescription(){
	if(descriptionIndex>=action.IDs.length){
		action.packetId = packetId.alarm;
		action.data.parameterCode = codeIdStatus;
		action.data.value = action.IDs;
		readAlarmDescription = false;
		run();
		return;
	}
	action.update = true;

	action.data.value = [action.IDs[descriptionIndex]];
	++descriptionIndex;
	serialPort.postObject($card, action);

}
