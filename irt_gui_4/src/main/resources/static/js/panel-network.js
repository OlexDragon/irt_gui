import {type as typeFromDT} from './packet/service/device-type.js'
import {showError} from './worker.js'

import {id as fPacketId} from './packet/packet-properties/packet-id.js'
import {id as fGroupId} from './packet/packet-properties/group-id.js'
import Packet from './packet/packet.js'
import RequestPackt from './packet/request-packet.js'

const $card = $('#userCard');
const $body = $('#network-tab-pane');

let interval;
let delay = 5000;

export function start(){
	console.log('Network')
	stop();
	if(!$body.find(':first-child').length){
		const name = chooseFragmentName();
		$body.load(`/fragment/network/${name}`);
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

const packetId = fPacketId('networkAll');

let timeout;
let buisy;
let oldType;
function run(){

	if(buisy){
		console.log('Buisy');
		return
	}

	buisy = true;

	sendRequest();
}
function sendRequest(){

	const requestPacket = new RequestPackt(packetId);

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
const module = {}
module.fNetwork = function(packet){

	if(packet.header.groupId !== fGroupId('network')){
		console.warn(packet);
		timeout = blink($card, timeout, 'connection-wrong');
		if(showError)
			showToast("Packet Error", packet.toString());
		return;
	}

	const payloads = packet.payloads;

	if(!payloads?.length){
		console.warn('No payloads to parse.');
		timeout = blink($card, timeout, 'connection-wrong');
		return;
	}

	timeout = blink($card, timeout);

	payloads.forEach(pl=>{
		switch(pl.parameter.code){

		case 1:
			parseNetwork(pl);
			break;

		default:
			console.warn(pl);
		}
	});
}
function parseNetwork(pl){
	if(pl.data?.length!=13){
		console.warn('Wrong data size.');
		return;
	}
	$('#selectNetworkType').val(pl.data[0]);
	$('#networkAddress').children().each((i, el)=>el.value = pl.data[++i]&0xff);
	$('#networkMask').children().each((i, el)=>el.value = pl.data[i + 5]&0xff);
	$('#networkGateway').children().each((i, el)=>el.value = pl.data[i + 9]&0xff);
}