import {showError} from './worker.js'
import Packet from './packet/packet.js'
import RequestPackt from './packet/request-packet.js'
import {type as typeFromDT} from './packet/service/device-type.js'
import {id as f_groupId} from './packet/packet-properties/group-id.js'
import {id as f_PacketId} from './packet/packet-properties/packet-id.js'
import {code, parser} from './packet/parameter/control.js'

const $card = $('#userCard');
const $body = $('#redundancy-tab-pane');
let $selectEnable;
let $selectStandby;
let $selectName;
let $redundancyImg;
let $btnSetOnline;
let $redundancyStatus;

const packetId = f_PacketId('redundancyAll');
const packetIdSetOnline = f_PacketId('redundancySetOnline');
const packetIdSetEnable = f_PacketId('redundancySetEnable');
const packetIdSetDisable = f_PacketId('redundancySetDisable');
const packetIdSetCold = f_PacketId('redundancySetCold');
const packetIdSetHot = f_PacketId('redundancySetHot');
const packetIdSetNameA = f_PacketId('redundancySetNameA');
const packetIdSetNameB = f_PacketId('redundancySetNameB');
const packetIds = [packetId, packetIdSetOnline, packetIdSetEnable, packetIdSetDisable, packetIdSetCold, packetIdSetHot, packetIdSetNameA, packetIdSetNameB]
const groupId = f_groupId('configuration')

let interval;
let delay = 5000;

export function start(){
	if(interval)
		return;
	const name = chooseFragmentName();
	$body.load(`/fragment/redundancy/${name}`, ()=>{
		$selectEnable = $('#selectEnable').change(onSendCommand);
		$selectStandby = $('#selectStandby').change(onSendCommand);
		$selectName = $('#selectName').change(onSendCommand);
		$redundancyImg = $('#redundancyImg');
		$btnSetOnline = $('#btnSetOnline').click(onSendCommand);
		$redundancyStatus = $('#redundancyStatus');
		setTimeout(sendRequest, 100);
		interval = setInterval(run, delay);
	});
}

export function stop(){
	if(!interval)
		return;
	clearInterval(interval) ;
	interval = undefined;
}
export function disable(){
	$selectEnable?.prop('disabled', true);
	$selectStandby?.prop('disabled', true);
	$selectName?.prop('disabled', true);
	$btnSetOnline?.prop('disabled', true);
}
function chooseFragmentName(){
	const type = typeFromDT();
	switch(type){
	default:
		return 'buc';	
	}
}

let buisy;
function run(){

	if(buisy){
		console.log('Buisy')
		return
	}

	buisy = true;

	sendRequest();
}

function sendRequest(packet){

	if(!$redundancyStatus){
		buisy = false;
		return;
	}

	const requestPacket =  packet ? packet : new RequestPackt(packetId);

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

		if(!packetIds.includes(packet.header.packetId)){
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
module.fRedundancy = function(packet){

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

	payloads.forEach(parse);
}
const enableCode = code('redundancy_enable');
const modeCode = code('redundancy_mode');
const nameCode = code('redundancy_name');
const statusCode = code('redundancy_status');
const setOnlineCode = code('redundancy_set_online');
const imageLinks = ['/images/BUC_X.jpg', '/images/BUC_A.jpg', '/images/BUC_B.jpg'];
const status = ['', 'Online', 'Standby']
function parse(pl){
	const value = parser(pl.parameter.code)(pl.data);

	switch(pl.parameter.code){

	case enableCode:
		$selectEnable.val(value.toString());
		$selectEnable.prop('disabled', false);
		break;

	case nameCode:
		$selectName.val(value);
		$selectName.prop('disabled', false);
		break;

	case modeCode:
		$selectStandby.val(value);
		$selectStandby.prop('disabled', false);
		break;

	case statusCode:
		$redundancyStatus.text(status[value]);
		let disable = value===1 || value===0;
		$btnSetOnline.attr('disabled', disable);
		disable = !disable;
		$selectEnable.attr('disabled', disable);
		$selectStandby.attr('disabled', disable);
		$selectName.attr('disabled', disable);
		const nameId = $selectName.val();
		if(nameId)
			if(nameId==1 && value==1)
				$redundancyImg.attr('src', imageLinks[1]);
			else if(nameId==2 && value==1)
				$redundancyImg.attr('src', imageLinks[2]);
			else if(nameId==1 && value==2)
				$redundancyImg.attr('src', imageLinks[2]);
			else if(nameId==2 && value==2)
				$redundancyImg.attr('src', imageLinks[1]);
			else
				$redundancyImg.attr('src', imageLinks[0]);
		else
			$redundancyImg.attr('src', imageLinks[0]);
		break;

	default:
		console.log(pl);
	}
}

function onSendCommand(e){

	let id;

	switch(e.currentTarget.id){

	case 'btnSetOnline':
		id = packetIdSetOnline;
		break;

	case 'selectEnable':
		if($selectEnable.val()=='true')
			id = packetIdSetEnable;
		else
			id = packetIdSetDisable;
		break;

	case 'selectStandby':
		if($selectStandby.val()=='0')
			id = packetIdSetCold;
		else
			id = packetIdSetHot;
		break;

	case 'selectName':
		if($selectName.val()=='1')
			id = packetIdSetNameA;
		else if($selectName.val()=='2')
			id = packetIdSetNameB;
		break;

	default:
		console.warn(e.currentTarget.id);
	}

	if(!id)
		return;

	const packet = new RequestPackt(id);
	sendRequest(packet);
}