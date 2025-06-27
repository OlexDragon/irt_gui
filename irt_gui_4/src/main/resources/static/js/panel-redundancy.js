import * as serialPort from './serial-port.js'
import f_deviceType from './packet/service/device-type.js'
import Packet from './packet/packet.js'
import groupId from './packet/packet-properties/group-id.js'
import packetId from './packet/packet-properties/packet-id.js'
import control, {code, parser} from './packet/parameter/control.js'

const $card = $('#userCard');
const $body = $('#redundancy-tab-pane');
let $selectEnable;
let $selectStandby;
let $selectName;
let $redundancyImg;
let $btnSetOnline;
let $redundancyStatus;

const action = {packetId: packetId.redundancyAll, groupId: groupId.configuration, data: {}, function: 'f_Redundancy'};

let interval;
let delay = 5000;

export function start(){
	if(interval)
		return;
	action.buisy = false;
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
	const type = f_deviceType();
	switch(type){
	default:
		return 'buc';	
	}
}

let buisy;
function run(){
	if(!serialPort.doRun()){
		stop();
		return;
	}

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

	serialPort.postObject('/serial/send', requestPacket)
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
				serialPort.showToast(jqXHR.responseJSON.error, jqXHR.responseJSON.message, 'text-bg-danger bg-opacity-50');
		}
	
	});
}

action.f_Redundancy = function(packet){

	if(packet.header.error){
		console.warn(packet.toString());
		blink($card, 'connection-wrong');
		if(showError)
			serialPort.showToast("Packet Error", packet.toString());
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
const imageLinks = ['/images/BUC_X.jpg', '/images/BUC_A.jpg', '/images/BUC_B.jpg'];
const status = ['', 'Online', 'Standby']
function parse(pl){
	const value = parser(pl.parameter.code)(pl.data);

	switch(pl.parameter.code){

	case control.Redundancy.code:
		$selectEnable.val(value.toString());
		$selectEnable.prop('disabled', false);
		break;

	case control.Name.code:
		$selectName.val(value);
		$selectName.prop('disabled', false);
		break;

	case control.Mode.code:
		$selectStandby.val(value);
		$selectStandby.prop('disabled', false);
		break;

	case control.Status.code:
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
		id = packetId.packetIdSetOnline;
		break;

	case 'selectEnable':
		if($selectEnable.val()=='true')
			id = packetId.packetIdSetEnable;
		else
			id = packetId.packetIdSetDisable;
		break;

	case 'selectStandby':
		if($selectStandby.val()=='0')
			id = packetId.packetIdSetCold;
		else
			id = packetId.packetIdSetHot;
		break;

	case 'selectName':
		if($selectName.val()=='1')
			id = packetId.packetIdSetNameA;
		else if($selectName.val()=='2')
			id = packetId.packetIdSetNameB;
		break;

	default:
		console.warn(e.currentTarget.id);
	}

	if(!id)
		return;

	const packet = new RequestPackt(id);
	sendRequest(packet);
}