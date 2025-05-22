import Packet from './packet/packet.js'
import RequestPackt from './packet/request-packet.js'
import {run as doRun, showError} from './worker.js'
import {code, description, comparator, parser} from './packet/parameter/device-info.js'
import {id as fPacketId} from './packet/packet-properties/packet-id.js'
import {id as fGroupId} from './packet/packet-properties/group-id.js'

const $card = $('.infoCard');
const $body = $('.info');

export let type;

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
	interval = undefined;
}

const packetId = fPacketId('deviceInfo');

let buisy;
function run(){

	if(buisy){
		console.log('Buisy')
		return
	}

	buisy = doRun;

	if(!doRun)
		return;

	const requestPacket = new RequestPackt(packetId);

	postObject('/serial/send', requestPacket)
	.done(data=>{
		buisy = false;

		if(!data.answer?.length){
			console.log(data);
			console.warn("No answer.");
			blink($card, 'connection-wrong');
			return;
		}
		blink($card);

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

		if(packet.header.error){
			console.log(data);
			const packetStr = packet.toString();
			console.error(packetStr);
			blink($card, 'connection-wrong');
			if(showError)
				showToast('Packet Error', packetStr, 'text-bg-danger bg-opacity-50');

			return;
		}
		module[data.function](packet);
	})
	.fail((jqXHR)=>{
		buisy = false;
		blink($card, 'connection-fail');

		if(jqXHR.responseJSON?.message){
			if(showError)
				showToast(jqXHR.responseJSON.error, jqXHR.responseJSON.message, 'text-bg-danger bg-opacity-50');
		}
	});
}
const module = {}
module.fInfo = function(packet){

	if(packet.header.groupId == fGroupId('alarm')){
		console.warn(packet);
		blink($card, 'connection-wrong');
		if(showError)
			showToast("Packet Error", packet.toString());
		return;
	}

	const payloads = packet.payloads;

	if(!payloads?.length){
		console.warn('No payloads to parse.');
		blink($card, 'connection-wrong');
		return;
	}

	blink($card);

	payloads.sort(comparator).forEach(pl=>{

		const valId = 'infoVal' + pl.parameter.code;
		const descrId = 'infoDescr' + pl.parameter.code;
		const $sesct = $body.find('#' + descrId);
		if($sesct.length){
			const val = parser(pl.parameter.code)(pl.data);
			const $val = $body.find('#' + valId);
			if(val !== $val.text())
				$val.text(val);
		}else{
			const showText = description(pl.parameter.code);
			const $row = $('<div>', {class: 'row'});
			const val = parser(pl.parameter.code)(pl.data);
			if(pl.parameter.code === code('type')){
				type = val;
				typeChangeEvents.forEach(e=>e(type));
			}

			let $v;
			if(showText && showText !== 'Description'){
				$row.append($('<div>', {id: descrId, class: 'col-5', text: showText}));
				$v = $('<div>', {id: valId, class: 'col', text: val});
			}else
				$v =$('<div>', {id: descrId, class: 'col'}).append($('<h4>', {text: val}));

			$row.append($v).appendTo($body);
		}
	});
}
const typeChangeEvents = [];
export function onTypeChange(e){
	typeChangeEvents.push(e);
}