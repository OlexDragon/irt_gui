import {showError} from './worker.js'
import Packet from './packet/packet.js'
import RequestPackt from './packet/request-packet.js'
import {code, parser} from './packet/parameter/alarm.js'
import {boorstrapClass} from './packet/parameter/value/alarm-status.js'
import {id as groupId} from './packet/packet-properties/group-id.js'
import {id as f_PacketId} from './packet/packet-properties/packet-id.js'
import {code as packetTypeCode} from './packet/packet-properties/packet-type.js'

const $card = $('#userCard');
const $body = $('#alarms-tab-pane');

//const packetIdSummary = f_PacketId('alarmSummary');
const packetIdAlarmIDs = f_PacketId('alarmIDs');
const packetIdAlarmAll = f_PacketId('alarmAll');
const packetIdAlarm = f_PacketId('alarm');
let packetId;
let alarmIDs;

let interval;
let delay = 5000;

export function start(){
	if(interval)
		return;

	$body.empty();
	run();
	interval = setInterval(run, delay);
}

export function stop(){
	clearInterval(interval) ;
	interval = undefined
}

function chooseFragmentName(){
	const type = typeFromDT();
	switch(type){
	default:
		return 'buc';	
	}
}

let buisy;
function run(){

	if(buisy){
		console.warn('Buisy');
		return
	}

	buisy = true;

	sendRequest();
}

export function sendRequest(){

	let requestPacket;

	if(alarmIDs){

		packetId = packetIdAlarmAll;
		requestPacket = new RequestPackt(packetId, undefined, alarmIDs);

	}else{
		packetId = packetIdAlarmIDs;
		requestPacket = new RequestPackt(packetIdAlarmIDs);
	}

	postObject('/serial/send', requestPacket)
	.done(data=>{
		buisy = false;
		blink($card);

		if(!data.answer?.length){
			console.warn("No answer.");
			blink($card, 'connection-wrong');
			return;
		}

		if(!data.function){
			console.warn("No function name.");
			return;
		}

		const packet = new Packet(data.answer, true); // true - packet with LinkHeader

		if(packet.header.packetId !== packetId){
			console.log(packet);
			console.warn('Received wrong packet.');
			blink($card, 'connection-wrong');
			return;
		}

		module[data.function](packet);
	})
	.fail((jqXHR)=>{
		buisy = false;
		blink($card, 'connection-fail');

		if(jqXHR.responseJSON?.message){
			console.log(requestPacket);
			console.error(jqXHR.responseJSON.message);
			if(showError)
				showToast(jqXHR.responseJSON.error, jqXHR.responseJSON.message, 'text-bg-danger bg-opacity-50');
		}
	
	});
}
let alarmIndex;
const module = {}
module.fAlarms = function(packet){
	alarmIndex = -1;

	if(packet.header.groupId !== groupId('alarm')){
		console.warn(packet);
		blink($card, 'connection-wrong');
		if(showError)
			showToast("Packet Error", packet.toString());
		return;
	}

	const payloads = packet.payloads;

	if(!payloads?.length){
		console.log(packet.toString());
		console.warn('No payloads to parse.');
		blink($card, 'connection-wrong');
		return;
	}

	blink($card);

	payloads.forEach(parseAlarm);
}
const codeIdIDs = code('IDs');
const codeIdDescription = code('description');
const codeIdStatus = code('status');
const codeIdName = code('name');
function parseAlarm(pl){

	switch(pl.parameter.code){
	case codeIdIDs:
		alarmIDs = parser(pl.parameter.code)(pl.data);
		sendRequest();
		break; 

	case codeIdDescription:
		showDescription(pl);
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
		$div.removeClass(boorstrapClass).addClass('col ' + value.boorstrapClass)

	if(value.index>alarmIndex)
		alarmIndex = value.index;
}

//function showName(pl){
//	const value = parser(pl.parameter.code)(pl.data);
//	const $row = getRow(value.id);
//	const $div = $row.find('.name');
//	if($div.text()!==value.string)
//		$div.text($div.text());
//}

function showDescription(pl){
const value = parser(pl.parameter.code)(pl.data);
const $row = getRow(value.id);
const $div = $row.find('.name');
if($div.text()!==value.string)
	$div.text(value.string);
}

function getRow(id){
	let $row = $body.find('#row' + id);
	if(!$row.length){
		$row = $('<div>', {id: 'row' + id, class: 'row mt-1'})
				.append($('<div>', {class: 'col name text-end fw-bold'}))
				.append($('<div>', {class: 'col value text-center fs-6'}));
		$body.append($row);
	}
	return $row;
}
