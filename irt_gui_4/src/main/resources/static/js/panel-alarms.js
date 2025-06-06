import {showError} from './worker.js'
import Packet from './packet/packet.js'
import RequestPackt from './packet/request-packet.js'
import {code, parser} from './packet/parameter/alarm.js'
import {id as f_PacketId} from './packet/packet-properties/packet-id.js'

const $card = $('#userCard');
const $body = $('#alarms-tab-pane');

//const packetIdSummary = f_PacketId('alarmSummary');
const packetIdAlarmIDs = f_PacketId('alarmIDs');
const packetIdAlarmDescription = f_PacketId('alarmDescription');
const packetIdAlarm = f_PacketId('alarm');
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

let readAlarmDescription = true;
let buisy;
function run(){

	if(buisy){
		console.warn('Buisy');
		return
	}

	buisy = true;

	if(alarmIDs){

		if(readAlarmDescription)
			getAlarmDescription();

		const requestPacket = new RequestPackt(packetIdAlarm, undefined, alarmIDs);
		sendRequest(requestPacket);

	}else{

		const requestPacket = new RequestPackt(packetIdAlarmIDs);
		sendRequest(requestPacket);
	}

}

export function sendRequest(requestPacket){

	postObject('/serial/send', requestPacket)
	.done(data=>{
		buisy = false;

		if(!data.answer?.length){
			console.warn("No answer.");
			blink($card, 'connection-wrong');
			return;
		}

		if(!data.function){
			console.warn("No function name.");
			blink($card, 'connection-wrong');
			return;
		}

		const packet = new Packet(data.answer, true); // true - packet with LinkHeader

		if(![packetIdAlarmIDs, packetIdAlarmDescription, packetIdAlarm].includes(packet.header.packetId)){
			console.log(packet.toString());
			console.warn('Received wrong packet.');
			blink($card, 'connection-wrong');
			return;
		}

		if(packet.header.error){
			console.warn(packet.toString());
			blink($card, 'connection-wrong');
			if(showError)
				showToast("Packet Error", packet.toString());
			return;
		}

		blink($card);

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
	packet.payloads.forEach(parseAlarm);
}
const codeIdIDs = code('IDs');
const codeIdDescription = code('description');
const codeIdStatus = code('status');
const codeIdName = code('name');
function parseAlarm(pl){

	switch(pl.parameter.code){
	case codeIdIDs:
		alarmIDs = parser(pl.parameter.code)(pl.data);
		run();
		break; 

	case codeIdDescription:
		getAlarmDescription();
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
		removeCalsses($div).addClass('col ' + value.boorstrapClass)

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

function removeCalsses($el){
	const classes = $el.attr('class').split(' ').filter(c=>c.startsWith('text-bg-')).join(' ');
	$el.removeClass(classes);
	return $el;
}

let descriptionIndex = 0;
function getAlarmDescription(){
	if(descriptionIndex>=alarmIDs.length){
		readAlarmDescription = false;
		return;
	}

	const requestPacket = new RequestPackt(packetIdAlarmDescription, undefined, alarmIDs[descriptionIndex]);
	++descriptionIndex;
	sendRequest(requestPacket);

}
