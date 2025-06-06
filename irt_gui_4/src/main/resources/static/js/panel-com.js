import {type as typeFromDT} from './packet/service/device-type.js'
import {showError, unitAddress, baudrate} from './worker.js'
import {id as f_packetId} from './packet/packet-properties/packet-id.js'
import Packet from './packet/packet.js'
import RequestPackt from './packet/request-packet.js'
import ComControl from './com-control.js'
import {code, parser} from './packet/parameter/protocol.js'
import {longToBytes} from './packet/service/converter.js'


const $card = $('#userCard');
const $body = $('#com-tab-pane');

let comControl;
let interval;
let delay = 10000;
let parameters;
let type;

export function start(){

	stop()

	if(interval)
		return;

	type = typeFromDT();
	switch(type){

	case 'CONTROLLER':
		parameters = [3, 4, 5];
		break;

	default:
		console.log(type);
		parameters = [3, 4, 5, 6];
	}

	const name = chooseFragmentName();
	$body.load(`/fragment/com/${name}`, ()=>{
		const $comAddress = $('#comAddress');
		const $comRetransmits = $('#comRetransmits');
		const $comStandard = $('#comStandard');
		const $comBaudrate = $('#comBaudrate');
		comControl = new ComControl($comAddress, $comRetransmits, $comStandard, $comBaudrate);
		comControl.onChange(onChange);

		switch(type){
		case 'CONTROLLER':
			$comStandard.parents('.to-hide').addClass('visually-hidden');
		}
//		networkControl.onChange(onChange);
//		networkControl.onNotSaved(onNotSaved);
		run();
	});
	interval = setInterval(run, delay);
}
export function stop(){
	clearInterval(interval) ;
	interval = undefined;
}
export function disable(){
	comControl?.disable();
}
function chooseFragmentName(){
	const type = typeFromDT();
	switch(type){
	default:
		return 'buc';	
	}
}

const packetId = f_packetId('comAll');
const packetIdSetAddress = f_packetId('comSetAddress');
const packetIdSetRetransmit = f_packetId('comSetRetransmit');
const packetIdSetStandard = f_packetId('comSetStandard');
const packetIdSetBaudrate = f_packetId('comSetBaudrate');

let buisy;
function run(){

	if(buisy){
		console.log('Buisy');
		return
	}

	buisy = true;

	sendRequest();
}

function sendRequest(){

	const requestPacket = new RequestPackt(packetId, parameters);
	post(requestPacket);
}

function post(requestPacket){

	postObject('/serial/send', requestPacket)
	.done(data=>{
		buisy = false;

		if(!data.answer?.length){
			console.log(requestPacket);
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

		if(![packetId, packetIdSetAddress, packetIdSetBaudrate, packetIdSetRetransmit, packetIdSetStandard].includes(packet.header.packetId)){
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

const addressCode = code('address');
const retransmitsCode = code('retransmit');
const baudrateCode = code('baudrate');
const standardCode = code('tranceiver_mode');

const module = {}
module.fCom = function(packet){

	if(packet.header.error){
		console.warn(packet.toString());
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

	comControl.disable = false;

	payloads.forEach(pl=>{

		const value = parser(pl.parameter.code)(pl.data);
		switch(pl.parameter.code){

		case addressCode:
			comControl.address = value;
			unitAddress.unitAddress = value;
			break;

		case retransmitsCode:
			comControl.retransmits = value;
			break;

		case baudrateCode:
			comControl.baudrate = value;
			break;

		case standardCode:
			comControl.standard = value;
			break;

		default:
			console.warn(pl);
		}
	});
}

function onChange(v){
	const keys = Object.keys(v);
	if(!keys.length){
		console.warn('something went wrong.')
		return;
	}

	let id;
	let value;
	const key = keys[0];
	switch(key){

	case 'address':
		id = packetIdSetAddress;
		value = [v.address];
		break;

	case 'retransmit':
		id = packetIdSetRetransmit;
		value = [v.retransmit];
		break;

	case 'standard':
		id = packetIdSetStandard;
		value = [v.standard];
		break;

	case 'baudrate':
		id = packetIdSetBaudrate;
		value = longToBytes(v.baudrate);
		break;

	default:
		console.warn(v);
		return;
	}

	const requestPacket = new RequestPackt(id, value);
	post(requestPacket);
	if(key === 'baudrate')
		baudrate.baudrate = v.baudrate;
}
