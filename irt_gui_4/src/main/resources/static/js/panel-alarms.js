import {showError} from './worker.js'
import Packet from './packet/packet.js'
import Header from './packet/header.js'
import RequestPackt from './packet/request-packet.js'
import {code, parser, payloads} from './packet/parameter/alarm.js'
import {boorstrapClass} from './packet/parameter/value/alarm-status.js'
import {type as typeFromDT} from './packet/service/device-type.js'
import {id as groupId} from './packet/packet-properties/group-id.js'
import {id as fPacketId} from './packet/packet-properties/packet-id.js'
import {code as packetTypeCode} from './packet/packet-properties/packet-type.js'

const $card = $('#userCard');
const $body = $('#alarms-tab-pane');
const $userTabAlarn = $('#userTabAlarn');

const packetIdSummary = fPacketId('alarmSummary');
const packetIdAlarmIDs = fPacketId('alarmIDs');
const packetIdAlarm = fPacketId('alarm');
let packetId;
let alarmIDs;

let interval;
let delay = 1000;

export function start(){
	stop();
	if(!$body.find(':first-child').length){
		const name = chooseFragmentName();
		$body.load(`/fragment/alarms/${name}`);
	}
	interval = setInterval(run, delay);
}

export function stop(){
	clearInterval(interval) ;
}

function chooseFragmentName(){
	const type = typeFromDT();
	switch(type){
	default:
		return 'buc';	
	}
}

let timeout;
let buisy;
function run(){

	if(buisy){
		console.log('Buisy')
		return
	}

	buisy = true;

	sendRequest();
}

function sendRequest(){

	let requestPacket;

	if(alarmIDs){

		packetId = packetIdAlarm;
		requestPacket = new RequestPackt(packetId);
		const pls = payloads(alarmIDs,true);
		const packet = new Packet(new Header(packetTypeCode('request'), requestPacket.id, groupId('alarm')), pls, requestPacket.unitAddr);
		requestPacket.bytes = packet.toSend();

	}else{
		packetId = packetIdAlarmIDs;
		requestPacket = new RequestPackt(packetIdAlarmIDs);
	}

	postObject('/serial/send', requestPacket)
	.done(data=>{
		buisy = false;
		timeout = blink($card, timeout);

		if(!data.answer?.length){
			console.warn("No answer.");
			timeout = blink($card, timeout, 'connection-wrong');
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
			timeout = blink($card, timeout, 'connection-wrong');
			return;
		}

		module[data.function](packet);
	})
	.fail((jqXHR)=>{
		buisy = false;
		$card.addClass('connection-fail');
		timeout = blink($card, timeout, 'connection-fail');

		if(jqXHR.responseJSON?.message){
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
		timeout = blink($card, timeout, 'connection-wrong');
		if(showError)
			showToast("Packet Error", packet.toString());
		return;
	}

	const payloads = packet.payloads;

	if(!payloads?.length){
		console.log(packet.toString());
		console.warn('No payloads to parse.');
		timeout = blink($card, timeout, 'connection-wrong');
		return;
	}

	timeout = blink($card, timeout);

	payloads.forEach(parseAlarm);
}
function parseAlarm(pl){

	switch(pl.parameter.code){
	case code('IDs'):
		alarmIDs = parser(pl.parameter.code)(pl.data);
		break; 

	case code('description'):
		showDescription(pl);
		break;

//	case code('name'):
//		showName(pl);
//		break;

	case code('status'):
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
