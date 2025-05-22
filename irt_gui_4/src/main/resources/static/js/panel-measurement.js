import Packet from './packet/packet.js'
import RequestPackt from './packet/request-packet.js'
import {run as doRun, showError} from './worker.js'
import {id as fPacketId} from './packet/packet-properties/packet-id.js'
import {id as fGroupId} from './packet/packet-properties/group-id.js'
import {description, parser} from './packet/parameter/measurement.js'

const $card = $('.measurementCard');
const $body = $('.measurement');

let delay = 3000;
let interval;
export function start(){
	if(interval)
		return;

	$body.empty();
	run();
	interval = setInterval(run, delay);
}

export function stop(){
	clearInterval(interval);
	interval = undefined;
}

const packetId = fPacketId('measurementAll');
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

const module = {};
module.fMeasurement = function(packet){

	if(packet.header.groupId == fGroupId('alarm')){
		console.warn(packet);
		blink($card, 'connection-wrong');
		if(showError)
			showToast("Packet Error", packet.toString());
		return;
	}

	const payloads = packet.payloads;

	if(!payloads?.length){
		console.log(packet);
		console.warn('No payloads to parse.');
		blink($card, 'connection-wrong');
		return;
	}

	blink($card);
	payloads.forEach(pl=>{

		const valId = 'measVal' + pl.parameter.code;
		const descrId = 'measDescr' + pl.parameter.code;
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
