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

const action = {packetId: packetId.alarmSummary, groupId: groupId.alarm, data: {parameterCode: alarmCode['summary status']}, function: 'f_SummaryAlarms', onError: onError};

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

	$modal?.modal('hide');

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

function tripleClick({detail, ctrlKey, shiftKey}){

	if(detail!=3)
		return;

	if(shiftKey && ctrlKey){
		unitAddressScan();
		return;
	}

	const page = ctrlKey ? 'upgrade' : 'production';
	const url = new URL(page, window.location.href)
	setTimeout(()=>{ window.open(url, '_blank'); }, 100);
}

let $modal;
function onError(error) {

	if($modal)
		return;

	switch (error) {
		case "The port is locked.":{
			$modal = $('<div>', {class: 'modal fade', 'data-bs-backdrop': 'static', tabindex: '-1'})
			.append(
				$('<div>', {class: 'modal-dialog modal-dialog-centered'})
				.append(
					$('<div>', {class: 'modal-content'})
					.append(
						$('<div>', { class: 'modal-header' })
						.append($('<h5>', { class: 'modal-title', text: 'Serial Port Locked' })))
						.append(
							$('<div>', { class: 'modal-body' })
							.append($('<p>', { text: 'The serial port is locked. Unlocking it may take several minutes.' }))
							.append($('<p>', { text: 'If it remains locked, please press the "Stop" button to break the connection.' })))
					.append(
						$('<div>', { class: 'modal-footer' })
						.append($('<button>', { type: 'button', class: 'btn btn-primary', text: 'Stop' }).click(()=>{
							serialPort.stop();
							$modal.modal('hide');
						}))))
					).appendTo('body');
			new bootstrap.Modal($modal);
			$modal.on('hidden.bs.modal', ()=>{
				$modal.remove();
				$modal = null;
			});
			$modal.modal('show');
			break;
		}
	}
}
function unitAddressScan(){
	stop();
	const $body = showModal();
	action.timeout = 50;
	action.function = 'f_scanResponse';
	if(!action.f_scanResponseOriginal)
		action.f_scanResponse = function(packet){
	                                 if(packet.header.packetId===action.packetId)
										 $body.find('#colId-' + packet.linkHeader.unitAddr[0]).removeClass('text-danger').addClass('btn btn-success btn-sm').click(addressClick);
								 };
	scanAddress($body, 1, 255, action);
}
function addressClick({currentTarget:el}){
	$(el).parents('.modal').modal('hide');
	$('#unitAddress').val(el.innerText).change();
}
function scanAddress($body, start, end){
	if(start>end || !action.timeout)
		return;
	if(action.buisy){
		setTimeout(()=>scanAddress($body, start, end), 50);
		return;
	}
	$body.append($('<div>', { id: 'colId-' + start, class: 'col text-danger text-end', text: start }));
	action.buisy = true;
	action.unitAddr = start;
	serialPort.postObject($card, action);
	setTimeout(()=>scanAddress($body, ++start, end), 100);
}
function showModal(){
	const $body = $('<div>', { class: 'row' });
	const $modal = $('<div>', {class: 'modal fade', tabindex: '-1'})
					.append(
						$('<div>', {class: 'modal-dialog modal-dialog-centered'})
						.append(
							$('<div>', {class: 'modal-content'})
							.append(
								$('<div>', { class: 'modal-header' })
								.append(
									$('<h5>', { class: 'modal-title', text: 'Unit Address Scan' }))
								.append(
									$('<button>', { type: 'button', class: 'btn-close', 'data-bs-dismiss': 'modal', 'aria-label': 'Close' })))
							.append(
								$('<div>', { class: 'modal-body' }).append($body))))
					.on('hidden.bs.modal', ()=>{
						$modal.remove();
					    setDefault();
						start();
				    });
	new bootstrap.Modal($modal).show();
	return $body;
}
function setDefault(){
	action.buisy = false;
	action.update = true;
	action.unitAddr = undefined;
	action.timeout = undefined;
	action.function = 'f_SummaryAlarms';
}
action.onFail = jqXHR=>{
	console.log(jqXHR.getAllResponseHeaders());
}