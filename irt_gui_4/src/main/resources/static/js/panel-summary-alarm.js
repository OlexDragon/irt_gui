import {showError} from './worker.js'
import Packet from './packet/packet.js'
import {code, parser} from './packet/parameter/alarm.js'
import RequestPackt from './packet/request-packet.js'
import {id as f_PacketId} from './packet/packet-properties/packet-id.js'

const $card = $('#summaryAlarmCard');
const $title = $('#summaryAlarmTitle');

const statusChangeEvent = [];
const packetId = f_PacketId('alarmSummary');

let oldValue;
let interval;
export function start(){
	oldValue = undefined;
	interval = setInterval(run, 2000);
}

export function stop(){
	clearInterval(interval);
}

export function onStatusChange(e){
	statusChangeEvent.push(e);
}

let buisy;
function run(){

	if(buisy){
		console.warn('Buisy');
		return
	}

	buisy = true;

	const requestPacket = new RequestPackt(packetId);
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

		if(!jqXHR.responseJSON){

			$title.text('Closed').attr('title', 'GUI is Closed.');
			if(oldValue){
				$title.removeClass(oldValue.boorstrapClass);
				oldValue = undefined;
				statusChangeEvent.forEach(e=>e());
			}
		}else if(jqXHR.responseJSON.message){

			if(jqXHR.responseJSON.status==406)
				$title.text('SP Error').attr('title', 'Serial Port Error');
			else
				$title.text('NC').attr('title', 'No Connection');
			if(oldValue){
				$title.removeClass(oldValue.boorstrapClass);
				oldValue = undefined;
				statusChangeEvent.forEach(e=>e());
			}

			console.log(requestPacket);
			console.error(jqXHR.responseJSON.message);
			if(showError)
				showToast(jqXHR.responseJSON.error, jqXHR.responseJSON.message, 'text-bg-danger bg-opacity-50');
		}

	});
}

const module = {}
module.fSummaryAlarms = function(packet){

	packet.payloads.forEach(pl=>{
		const value = parser(pl.parameter.code)(pl.data);
		if(!oldValue || value.severities!=oldValue.severities){
			statusChangeEvent.forEach(e=>e(value));
			if(oldValue)
				$title.removeClass(oldValue.boorstrapClass);
			$title.addClass(value.boorstrapClass);
			$title.text(value.text).attr('title', value.text);
			oldValue = value;
		}
	});
}
