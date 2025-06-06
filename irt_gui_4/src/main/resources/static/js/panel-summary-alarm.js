import {showError} from './worker.js'
import Packet from './packet/packet.js'
import {parser} from './packet/parameter/alarm.js'
import RequestPackt from './packet/request-packet.js'
import {id as f_PacketId} from './packet/packet-properties/packet-id.js'
import {status as f_alarmStatus} from './packet/parameter/value/alarm-status.js'

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

		if(!data.answer?.length){
			if(data.error){
//				console.warn(data.error);
				blink($card, 'connection-fail');
				showError && showToast('Error', data.error, 'text-bg-danger bg-opacity-50');
				const status = f_alarmStatus(data.error);
				setStatus(status);
					
			}else{
//				console.warn('No answer.');
				blink($card, 'connection-wrong');
				showError && showToast('Warning', 'No answer.', 'text-bg-wrong bg-opacity-50');
				const status = f_alarmStatus('No answer.');
				setStatus(status);
			}
			return;
		}

		blink($card, 'connection-ok');
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

			const status = f_alarmStatus('Closed');
			setStatus(status);

		}else if(jqXHR.responseJSON.message){
			console.error(jqXHR.responseJSON.message);

			if(jqXHR.responseJSON.status==406){
				const status = f_alarmStatus('SP Error');
				setStatus(status);
			}else if(jqXHR.responseJSON.status==400){
			const status = f_alarmStatus('UA Error');
			setStatus(status);
			}else{
				const status = f_alarmStatus('NC');
				setStatus(status);
			}
		}
	});
}

function removeCalsses(){
	const classes = $title.attr('class').split(' ').filter(c=>c.startsWith('text-bg-')).join(' ');
	$title.removeClass(classes);
}

const module = {}
module.fSummaryAlarms = function(packet){

	if(packet.header.error){
		console.warn(packet.toString());
		blink($card, 'connection-wrong');
		if(showError)
			showToast("Packet Error", packet.toString());
		return;
	}

	packet.payloads?.forEach(pl=>{
		const value = parser(pl.parameter.code)(pl.data);
		setStatus(value);
	});
}

function setStatus(value){

	if(!oldValue || value.severities!=oldValue.severities){
		showError && showToast(value.severities, value.text, (value.boorstrapClass ?? 'text-bg-danger') + ' bg-opacity-50');
		statusChangeEvent.forEach(e=>e(value));

		if(oldValue)
			$title.removeClass(oldValue.boorstrapClass);
		else
			removeCalsses();

		if(value.index<0){
			if(value.text.startsWith('The read operation timed out'))
				$title.text('Timeout').attr('title', value.text);

			else if(value.text==='Closed')
				$title.text('Closed').attr('title', 'Closed');

			else
				$title.text('Error').attr('title', value.text);

		}else{
			value.boorstrapClass && $title.addClass(value.boorstrapClass);
			value.index > 6 ? $title.text(value.severities) : $title.text(value.text);
			$title.attr('title', value.text);
		}
		oldValue = value;
	}
}
