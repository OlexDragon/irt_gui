import {type as typeFromDT} from './packet/service/device-type.js'
import {type as typeFromInfo} from './panel-info.js'
import {id as f_packetId} from './packet/packet-properties/packet-id.js'
import Packet from './packet/packet.js'
import RequestPackt from './packet/request-packet.js'
import {run as doRun, showError} from './worker.js'
import {code as parameterCode, parser} from './packet/parameter/configuration.js'
import ValuePanel from './value-panel.js'

const $card = $('.controlCard');
const $body = $('.control');

let $attenuationValue;
let $attenuationControl;
let $attenuationStep;
let $attenuationRange;
let attenuationValuePanel;

let $gainValue;
let $gainControl;
let $gainStep;
let $gainRange
let gainValuePanel;

let $freqValue;
let $freqControl;
let $freqStep;
let $freqRange;
let freqValuePanel;

let $btnMute;

let type;
let interval;
let delay = 5000;

export function start(){
	if(interval)
		return;

	const fragmentName = chooseFragmentName();
	const url = `/fragment/control/${fragmentName}`;
	$body.load(url,()=>{
		buisy = false;

		const $attenuationTab = $('#attenuationTab').click(tabClick);
		$('#gainTab').click(tabClick);
		$('#freqTab').click(tabClick);

		$attenuationValue = $('#attenuationValue');
		$attenuationControl = $('#attenuationControl');
		$attenuationStep = $('#attenuationStep');
		$attenuationRange = $('#attenuationRange');
		new bootstrap.Tooltip($attenuationRange);
		attenuationValuePanel =  new ValuePanel($attenuationControl, $attenuationRange, $attenuationStep);
		attenuationValuePanel.change((v)=>onChange(v, pIdAtenuationSet));

		$gainValue = $('#gainValue');
		$gainControl = $('#gainControl');
		$gainStep = $('#gainStep');
		$gainRange = $('#gainRange');
		new bootstrap.Tooltip($gainRange);
		gainValuePanel = new ValuePanel($gainControl, $gainRange, $gainStep);
		gainValuePanel.change((v)=>onChange(v, pIdGainSet));

		$freqValue = $('#freqValue');
		$freqControl = $('#freqControl');
		$freqStep = $('#freqStep');
		$freqRange = $('#freqRange');
		new bootstrap.Tooltip($freqRange);
		freqValuePanel = new ValuePanel($freqControl, $freqRange, $freqStep);
		freqValuePanel.change((v)=>onChange(v, pIdFrequencySet));

		$btnMute = $('#btnMute').change(onChangeBtnMute);

		const tabCookies = Cookies.get('tabCookies');
		if(tabCookies)
			new bootstrap.Tab($('#' + tabCookies)).show();
		else
			new bootstrap.Tab($attenuationTab).show();

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

function tabClick(e){
	Cookies.set('tabCookies', e.currentTarget.id);
	switch(e.currentTarget.id){


		case 'attenuationTab':
			attenuationValuePanel.active()
			break;

		case 'gainTab':
			attenuationValuePanel.active()
			break;

		case 'freqTab':
			attenuationValuePanel.active()
			break;

		default:
			console.warn(e.currentTarget.id);
	}
}

const packetId = f_packetId('controlAll');
const pIdAtenuationSet = f_packetId('atenuationSet');
const pIdGainSet = f_packetId('gainSet');
const pIdFrequencySet = f_packetId('frequencySet');
const pIdMuteControl = f_packetId('mute_control');

let buisy;
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

	sendRequest();
}
	let oldType;
function sendRequest(toSend){

	if(!$body.children().length){

		const fragmentName = chooseFragmentName();
		// old control panel
		if(oldType!==fragmentName)
			oldType = fragmentName;
		return;
	}
	const requestPacket = toSend ? toSend : new RequestPackt(packetId);

	postObject('/serial/send', requestPacket)
	.done(data=>{
		buisy = false;

		if(!data.answer?.length){
			console.log("data");
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

		if(![packetId, pIdAtenuationSet, pIdGainSet, pIdFrequencySet, pIdMuteControl].includes(packet.header.packetId)){
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
const attenuation = parameterCode('attenuation');
const gain = parameterCode('gain');
const frequency = parameterCode('frequency');
const attenuationRange = parameterCode('attenuation_range');
const gainRange = parameterCode('gain_range');
const frequencyRange = parameterCode('frequency_range');
const mute = parameterCode('mute');
const module = {}
module.fConfig = function(packet){

	if(packet.header.error){
		console.error(packet);
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

	payloads.sort(({parameter:a},{parameter:b})=>b.code - a.code).forEach(pl=>{

		const val = parser(pl.parameter.code)(pl.data);

		switch(pl.parameter.code){

		case attenuationRange:
			attenuationValuePanel.min(val[1]/10*-1);
			attenuationValuePanel.max(val[0]);
			attenuationValuePanel.step(0.1);
			break;

		case gainRange:
			gainValuePanel.min(val[0]/10);
			gainValuePanel.max(val[1]/10);
			gainValuePanel.step(0.1);
			break;

		case frequencyRange:
			freqValuePanel.min(Number(val[0]/1000000n));
			freqValuePanel.max(Number(val[1]/1000000n));
			freqValuePanel.step(0.000001);
			break;

		case attenuation:
			{
				const value = val/10;
				attenuationValuePanel.value(value);
				$attenuationValue.val(value);
			}
			break;

		case gain:
			{
				const value = val/10;
				gainValuePanel.value(value);
				$gainValue.val(value);
			}
			break;

		case mute:
			{
				if(val===''){
					$btnMute.attr('disabled', true);
					break;
				}
				const value = val ? 'Unmute' : 'Mute';
				$btnMute.prop('checked', val).attr('disabled', false).next().text(value);
			}
			break;

		case frequency:
			{
				const value = val/1000000n;
				const remainder = Number(val - value*1000000n)/1000000;
				const result = Number(value)+remainder;
				freqValuePanel.value(result);
				$freqValue.val(result);
			}
			break;

		default:
			console.warn(pl);
		}
	});
}

function onChange(value, id){
	let toSend;
	switch(id){

	case 4:	//Attenuation
	case 6:	// Gain
		toSend = value * 10;
		break;

	case 8:
		const floor = Math.floor(value);
		const remainder = Math.round(value%1*1000000) ;
		toSend = BigInt(floor) * 1000000n + BigInt(remainder);
		break;

	default:
		console.log('To add id = ' + id);
		return;
	}
	const requestPackt = new RequestPackt(id, undefined, toSend);
	sendRequest(requestPackt);
}
function onChangeBtnMute(e){
	const toSend = e.currentTarget.checked ? 1 : 0;	// Mute / Unmute
	const requestPackt = new RequestPackt(pIdMuteControl, undefined, toSend);
	sendRequest(requestPackt);
}
export {type}
