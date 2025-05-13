import {type as typeFromDT} from './packet/service/device-type.js'
import {type as typeFromInfo} from './panel-info.js'
import {id as fPacketId} from './packet/packet-properties/packet-id.js'
import {id as fGroupId} from './packet/packet-properties/group-id.js'
import Packet from './packet/packet.js'
import RequestPackt from './packet/request-packet.js'
import {run as doRun, showError} from './worker.js'
import {code as parameterCode, description, parser} from './packet/parameter/configuration.js'

const $card = $('.controlCard');
const $body = $('.control');

let $attenuationValue;
let $attenuationControl;
let $attenuationStep;
let $attenuationRange;

let $gainValue;
let $gainControl;
let $gainStep;
let $gainRange

let $freqValue;
let $freqControl;
let $freqStep;
let $freqRange;

let type;
let interval;
let delay = 5000;

export function start(){
	stop();
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

function tabClick(e){
	Cookies.set('tabCookies', e.currentTarget.id);
}

const packetId = fPacketId('controlAll');

let timeout;
let buisy;
let oldType;
function run(){
	if(!typeFromInfo)
		return;

	if(buisy){
		console.log('Buisy')
		return
	}

	buisy = doRun;

	if(!doRun)
		return;

	const fragmentName = chooseFragmentName();
	// losd control panel
	if(oldType!==fragmentName){
		oldType = fragmentName;
		const url = `/fragment/control/${fragmentName}`;
		$body.load(url,()=>{
			buisy = false;

			const $attenuationTab = $('#attenuationTab').click(tabClick);
			$('#gainTab').click(tabClick);
			$('#freqTab').click(tabClick);

			$attenuationValue = $('#attenuationValue');
			$attenuationControl = $('#attenuationControl');
			$attenuationStep = $('#attenuationStep');
			$attenuationRange = $('#attenuationRange').on('input', rangeInpot).change(rangeChange);

			$gainValue = $('#gainValue');
			$gainControl = $('#gainControl');
			$gainStep = $('#gainStep');
			$gainRange = $('#gainRange').on('input', rangeInpot).change(rangeChange);

			$freqValue = $('#freqValue');
			$freqControl = $('#freqControl');
			$freqStep = $('#freqStep');
			$freqRange = $('#freqRange').on('input', rangeInpot).change(rangeChange);

			const tabCookies = Cookies.get('tabCookies');
			if(tabCookies)
				new bootstrap.Tab($('#' + tabCookies)).show();
			else
				new bootstrap.Tab($attenuationTab).show();

				sendRequest();
		});
		return;
	}

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
module.fConfig = function(packet){

	if(packet.header.groupId == fGroupId('alarm')){
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

		case parameterCode('gain_range'):
		case parameterCode('attenuation_range'):
		case parameterCode('frequency_range'):
			setRange(pl);
			break;

		default:
			showValue(pl);
		}
	});
}
function showValue(pl){
	
	const valId = 'infoVal' + pl.parameter.code;
	const descrId = 'infoDescr' + pl.parameter.code;
	const $descr = $body.find('#' + descrId);
	const val = setValue(pl);

	if($descr.length){
		const $val = $body.find('#' + valId);
		if(val !== $val.text())
			$val.text(val);
	}else{
		const showText = description(pl.parameter.code);
		const $row = $('<div>', {class: 'row'});
		if(pl.parameter.code === parameterCode('type'))
			type = val;

		let $v;
		if(showText && showText !== 'Description'){
			$row.append($('<div>', {id: descrId, class: 'col-5', text: showText}));
			$v = $('<div>', {id: valId, class: 'col', text: val});
		}else
			$v =$('<div>', {id: descrId, class: 'col'}).append($('<h4>', {text: val}));

		$row.append($v).appendTo($body);
	}
}
function setRange(pl){

	const val = pl.parameter.code == parameterCode('frequency') ? parser(pl.parameter.code)(pl.data)/1000000n : parser(pl.parameter.code)(pl.data);
	switch(pl.parameter.code){

		case parameterCode('attenuation_range'):
			const negVal = val[1]*-1;
			if(!$attenuationRange.prop('min') || parseInt($attenuationRange.prop('min'))!=negVal)
				$attenuationRange.prop('min', negVal);
			if(!$attenuationRange.prop('max') || parseInt($attenuationRange.prop('max'))!=val[0])
				$attenuationRange.prop('max', val[0]);
			break;

		case parameterCode('gain_range'):
			if(!$gainRange.prop('min') || parseInt($gainRange.prop('min'))!=val[0])
				$gainRange.prop('min', val[0]);
			if(!$gainRange.prop('max') || parseInt($gainRange.prop('max'))!=val[1])
				$gainRange.prop('max', val[1]);
			break;

		case parameterCode('frequency_range'):
			if(!$freqRange.prop('min') || parseInt($freqRange.prop('min'))!=val[0])
				$freqRange.prop('min', val[0]);
			if(!$freqRange.prop('max') || parseInt($freqRange.prop('max'))!=val[1])
				$freqRange.prop('max', val[1]);
			break;

			default:
				console.warn(pl);	
	}
}
function setValue(pl){

	let val = pl.parameter.code == parameterCode('frequency') ? parser(pl.parameter.code)(pl.data)/1000000n : parser(pl.parameter.code)(pl.data);
	switch(pl.parameter.code){

		case parameterCode('attenuation'):
			{
				const valToShow = val/10;
				if(!$attenuationControl.val())
					$attenuationControl.val(valToShow);
				if(!$attenuationValue.val() || parseInt($attenuationValue.val())!=valToShow)
					$attenuationValue.val(valToShow);
				if(!$attenuationRange.val() || parseInt($attenuationRange.val())!=val)
					$attenuationRange.val(val);
				val = valToShow + ' dB';
			}
			break;

		case parameterCode('gain'):
			{

				const valToShow = val/10;
				if(!$gainControl.val())
					$gainControl.val(valToShow);
				if(!$gainValue.val() || parseInt($gainValue.val())!=valToShow)
					$gainValue.val(valToShow);
				if(!$gainRange.val() || parseInt($gainRange.val())!=val)
					$gainRange.val(val);
				val = valToShow + ' dB';
			}
			break;

		case parameterCode('frequency'):
			if(!$freqControl.val())
				$freqControl.val(val);
			if(!$freqValue.val() || parseInt($freqValue.val())!=val)
				$freqValue.val(val);
				val += ' MHz';
			break;

			default:
				console.warn(pl);	
	}
	return val;
}
function rangeInpot(e){
	toSet(e);
}
function rangeChange(e){
	toSet(e);
}
function toSet(e){
	const name = e.currentTarget.id.replace('Range', 'Control');
	if(name.startsWith('freq')){
		$('#' + name).val(e.currentTarget.value);
	}else if(name.startsWith('att'))
		$('#' + name).val(e.currentTarget.value/10*-1);
	else
		$('#' + name).val(e.currentTarget.value/10);
}
export {type}