import {type as typeFromDT} from './packet/service/device-type.js'
import {showError} from './worker.js'
import {id as f_packetId} from './packet/packet-properties/packet-id.js'
import {id as f_groupId} from './packet/packet-properties/group-id.js'
import Packet from './packet/packet.js'
import RequestPackt from './packet/request-packet.js'
import NetworkControl from './network/network-control.js'

const $card = $('#userCard');
const $body = $('#network-tab-pane');

let networkControl;
let interval;
let delay = 10000;

export function start(){

	if(interval)
		return;

	const name = chooseFragmentName();
	$body.load(`/fragment/network/${name}`, ()=>{
		const $selectNetworkType = $('#selectNetworkType');
		const $networkAddress = $('#networkAddress');
		const $networkMask = $('#networkMask');
		const $networkGateway = $('#networkGateway');
		const $btnVetworkOk = $('#btnVetworkOk');
		const $btnNetworkCancel = $('#btnNetworkCancel');
		networkControl = new NetworkControl($selectNetworkType, $networkAddress, $networkMask, $networkGateway, $btnVetworkOk, $btnNetworkCancel);
		networkControl.onChange(onChange);
		networkControl.onNotSaved(onNotSaved);
		run();
	});
	interval = setInterval(run, delay);
}
export function stop(){
	clearInterval(interval) ;
	interval = undefined;
}

function chooseFragmentName(){
	const type = typeFromDT();
	switch(type){
	default:
		return 'buc';	
	}
}

const packetId = f_packetId('network');
const packetIdSet = f_packetId('networkSet');

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
	post(requestPacket);
}
function post(requestPacket){

	postObject('/serial/send', requestPacket)
	.done(data=>{
		buisy = false;

		if(!data.answer?.length){
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

		if(![packetId, packetIdSet].includes(packet.header.packetId)){
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
const module = {}
module.fNetwork = function(packet){

	if(packet.header.groupId !== f_groupId('network')){
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

	networkControl.value = pl.data;
}

function onChange(ipAddress){
	const requestPacket = new RequestPackt(packetIdSet, ipAddress.bytes);
	post(requestPacket);
}

function onNotSaved(e){

	showToast("Network settings are not saved.", e.currentValue.toString(), 'text-bg-warning bg-opacity-50');
	console.log(e);
}
