import {type as typeFromDT} from './packet/service/device-type.js'
import Packet from './packet/packet.js'
import RequestPackt from './packet/request-packet.js'
import {run as doRun, showError} from './worker.js'
import {id as f_PacketId} from './packet/packet-properties/packet-id.js'
import {description, parser} from './packet/parameter/measurement.js'
import ControllerMeasurement from './classes/controller-measurement.js'

const $card = $('.measurementCard');
const $body = $('.measurement');

let delay = 3000;
let interval;
export function start(){
	if(interval)
		return;

	$body.empty();

	chooseFragmentName();

	run();
	interval = setInterval(run, delay);
}

export function stop(){
	clearInterval(interval);
	interval = undefined;
}

const packetIdNoAction = f_PacketId('noAction');
let packetId;
let buisy;
let contrillerMeasurement;

function chooseFragmentName(){
	switch(typeFromDT()){
	case 'CONTROLLER':
		packetId = f_PacketId('irpc');
		const url = `/fragment/measurement/irpc`;
		$body.load(url, ()=>{
			const $controllerStatus	 = $body.find('#controllerStatus');
			const $unitsStatus		 = $body.find('#unitsStatus');
			contrillerMeasurement = new ControllerMeasurement($controllerStatus, $unitsStatus);
		});
		break;

	default:
		packetId = f_PacketId('measurementAll');
	}
}
function run(){

	if(packetId === packetIdNoAction){
		stop();
		return;
	}
	if(buisy){
		console.log('Buisy')
		return;
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

		if(!data.function){
			console.warn("No function name.");
			return;
		}

		const packet = new Packet(data.answer, true); // true - packet with LinkHeader

		if(packet.header.packetId !== packetId){
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
			if(showError)
				showToast(jqXHR.responseJSON.error, jqXHR.responseJSON.message, 'text-bg-danger bg-opacity-50');
		}
	});
}

const module = {};
module.fMeasurement = (packet)=>{

	const payloads = packet.payloads;

	if(!payloads?.length){
		console.log(packet.toString());
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
module.f_IRPC = (packet)=>contrillerMeasurement.update = packet.payloads;