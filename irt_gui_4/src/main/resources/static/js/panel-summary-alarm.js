import * as serialPort from './serial-port.js'
import packetId from './packet/packet-properties/packet-id.js'
import { alarmCode, parser } from './packet/parameter/alarm.js'
import { status as f_alarmStatus } from './packet/parameter/value/alarm-status.js'
import groupId from './packet/packet-properties/group-id.js'

const $card = $('#summaryAlarmCard');
const $title = $('#summaryAlarmTitle').click(tripleClick);

const statusChangeEvent = [];

let oldValue;
let interval;

const action = {packetId: packetId.alarmSummary, groupId: groupId.alarm, data: {parameterCode: alarmCode['summary status']}, function: 'f_SummaryAlarms'};

export function start(){
	oldValue = undefined;
	action.buisy = false;
	run();
	clearInterval(interval);
	interval = setInterval(run, 2000);
}

export function stop(){
	clearInterval(interval);
}

export function onStatusChange(e){
	statusChangeEvent.push(e);
}

export function textToStatus(text){
	const status = f_alarmStatus(text);
	setStatus(status);
}

export function closed(){
	return oldValue?.severities === 'Closed'
}

function run(){
	if(!serialPort.doRun()){
		stop();
		return;
	}

	if(action.buisy){
		serialPort.blink($card, 'connection-buisy');
		console.warn('Buisy');
		return
	}

	action.buisy = true;

	serialPort.postObject($card, action);
}

function removeClasses(){
	const classes = $title.attr('class').split(' ').filter(c=>c.startsWith('text-bg-')).join(' ');
	$title.removeClass(classes);
}

action.f_SummaryAlarms = function(packet){

	packet.payloads?.forEach(pl=>{
		const value = parser(pl.parameter.code)(pl.data);
		setStatus(value);
	});
}

function setStatus(value){

	if(value.severities!=oldValue?.severities){
		serialPort.showError && serialPort.showToast(value.severities, value.text, (value.boorstrapClass ?? 'text-bg-danger') + ' bg-opacity-50');
		statusChangeEvent.forEach(e=>e(value));

		if(oldValue)
			$title.removeClass(oldValue.boorstrapClass);
		else
			removeClasses();

		value.boorstrapClass && $title.addClass(value.boorstrapClass);
		$title.text(value.severities);
		$title.attr('title', value.text);

		oldValue = value;
		switch(value.severities){
		case 'SP Error':
		case 'Closed':
			serialPort.stop();
		}
	}
}

function tripleClick({detail}){
	if(detail!=3)
		return;

	const url = new URL('production', window.location.href)
	window.open(url, '_blank');
}