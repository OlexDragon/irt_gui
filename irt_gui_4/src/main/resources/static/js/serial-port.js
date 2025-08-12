import f_toSend from './to-send.js'
import Packet from './packet/packet.js'
import UnitAddress from './classes/unit-address.js'
import Baudrate from './classes/baudrate.js'
import {start as summaryAlarmStart, stop as summaryAlarmStop, textToStatus, closed} from './panel-summary-alarm.js'
import {status as f_alarmStatus } from './packet/parameter/value/alarm-status.js'
import packetType from './packet/packet-properties/packet-type.js'

export let serialPort;
export let showError;
export const baudrate = new Baudrate($('#baudrate'));
export const unitAddrClass = new UnitAddress($('#unitAddress'));

export function doRun(){
	return $btnStart.prop('checked');
}

const btnStartEvents = [];

export function onStart(cb){
	btnStartEvents.push(cb);
}
export function removeOnStart(cb){
	const index = btnStartEvents.indexOf(cb);
	if(index>=0)
		btnStartEvents.splice(index, 1);
}

export function postObject($card, action){
	f_toSend(action, toSend=>send($card, toSend, action));
}

export function blink($el, bottstrapClass){
	if(!bottstrapClass)
		bottstrapClass = 'connection-ok';
	$el.addClass(bottstrapClass);
	setTimeout(()=>$el.removeClass(bottstrapClass), 1000);
}

export function showToast(title, message, headerClass){

	let $toast = $('<div>', {class: 'toast', role: 'alert', 'aria-live': 'assertive', 'aria-atomic': true});
		$toast.append(
			$('<div>', {class: 'toast-header'})
			.append(
				$('<strong>', {class: 'me-auto', text: title})
			)
			.append(
				$('<button>', {class: 'btn-close', type: 'button', 'data-bs-dismiss': 'toast', 'aria-label': 'Close'})
			)
		)
		.append(
			$('<div>', {class: 'toast-body', text: message})
		)
	.appendTo($toastContainer)
	.on('hide.bs.toast', function(){this.remove();});

	if(headerClass)
		$toast.find('.toast-header').addClass(headerClass);

	new bootstrap.Toast($toast).show();
}

export function stop(){
	if($btnStart.prop('checked'))
		$btnStart.click();
}

const $serialPort = $('select[id=serialPort]').change(portSelected);
const $btnStart =$('#btnStart').change(toggleStart);
const $conections = $('#conections');
const $toastContainer = $('#toastContainer');
const $modal = $('#modal');
const $btnShowErrors = $('#btnShowErrors').change(btnShowErrorsChange);
(()=>{
	showError = Cookies.get('btnShowErrors')==='true';
	$btnShowErrors.prop('checked', showError);
})();

$('#appExit').click(async ()=>{

	clearInterval(interval);
	clearInterval(countInterval);
	try{
		const x = await $.post('/connection/add', {connectionId: sessionId});

		if(x < 2 || confirm(`${x - 1} more connection found.\nAre you sure you want to close this program?`))
			$.get('/exit').always(showExitModal);
	}catch(e){
		showExitModal();
	}
});

const sessionId = 'sessionId' + Math.random().toString(16).slice(2);
function countConnections(){
	$.post('/connection/add', {connectionId: sessionId})
	.done(count=>{
		const text = count + ' conections';
		$conections.text()!==text && $conections.text(text);
	});
}

function showExitModal(){
	summaryAlarmStop(); 
	btnStartEvents.forEach(cb=>cb(false));
	$modal.load('/modal/exit');
	$modal.attr('data-bs-backdrop', 'static');
	$modal.modal('show');
}

function portSelected(e){
	serialPort = e.currentTarget.value;
	Cookies.set('serialPort', serialPort);
	if($btnStart.prop('disabled'))
		$btnStart.attr('disabled', false);
	toggleStart();
}

let interval;
let countInterval;
function toggleStart(){
	clearInterval(interval);

	const $lbl = $btnStart.next();
	const text = $lbl.text();

	switch (text) {
		case 'Start':
			btnStartEvents.forEach(cb => cb(true));
			summaryAlarmStart();
			$lbl.text('Stop');
			$btnStart.attr('checked', true);
			countInterval = setInterval(countConnections, 3000);
			break;

		default:
			btnStartEvents.forEach(cb => cb(false));
			summaryAlarmStop(); 
			$lbl.text('Start');
			$btnStart.attr('checked', false);
			clearInterval(countInterval);
			$conections.empty()

			if(closed())
				return;

			$.post('/serial/close', { spName: serialPort });
			textToStatus('Stopped:The program has stopped accessing the serial port.')
	}
}

function btnShowErrorsChange(e){
	showError = e.currentTarget.checked;
	Cookies.set('btnShowErrors', showError);
	if(showError)
		showToast('Display of error messages is enabled.', 'Error information will be displayed here..');
}

function send($card, toSend, action){
		var json = JSON.stringify(toSend);

		return $.ajax({
			url: '/serial/send',
			type: 'POST',
			contentType: "application/json",
			data: json,
		    dataType: 'json'
		})
				.done(data => {
					action.buisy = false;

					if(data.error){
						textToStatus(data.error);
						return;
					}

					if(!data.answer?.length) {
						console.warn("No answer.", data);
						blink($card, 'connection-wrong');
						return;
					}

					if(!data.function) {
						console.warn("No function name.", data);
						return;
					}

					const packet = new Packet(data.answer, data.unitAddr); // true - packet with LinkHeader

					if(action.packetId !== packet.header.packetId) {
						console.log(action, packet);
						console.warn('Received wrong packet.');
						blink($card, 'connection-wrong');
						return;
					}

					if(packet.header.error) {
	//					console.log(data);
						const packetStr = packet.toString();
						console.error(packetStr);
						blink($card, 'connection-wrong');
						if (showError)
							showToast('Packet Error', packetStr, 'text-bg-danger bg-opacity-50');

						action.packetError = packet.header.error;
						return;
					}

					if(!(packet.payloads?.length || packet.header.groupId === packetType.acknowledgement)){
						if(packet.header.type!==packetType.acknowledgement){
							console.log(action, packet);
							console.warn('Packet does not have payloads.');
						}
						blink($card, 'connection-wrong');
						return;
					}
					blink($card);
//					console.log(data)
//					console.log(action)
					action[data.function](packet);
		})
		.fail((jqXHR)=>{
			action.buisy = false;
			blink($card, 'connection-fail');

			console.log(jqXHR.getAllResponseHeaders());

			if(jqXHR.responseJSON?.message){
				if(showError)
					showToast(jqXHR.responseJSON.error, jqXHR.responseJSON.message, 'text-bg-danger bg-opacity-50');
			}else if(!jqXHR.getAllResponseHeaders())
				textToStatus('Closed:The application is not responding.')
		});
}
(function getPortNames(){
	$.get('/serial/ports')
	.done(ports=>{
		if(!ports)
			return;
		const serialPortCookies = Cookies.get('serialPort');
		ports.forEach(name=>{
			const selected = serialPortCookies === name;
			if(selected)
				serialPort = name;
			$('<option>', {text: name, selected: selected}).appendTo($serialPort);
			if(selected){
				$btnStart.attr('disabled', false);
				toggleStart();
			}else
				interval = setInterval(()=>{
					if($serialPort.val()){
						clearInterval(interval);
						return;
					}
					showToast("Serial port not selected.", "Select the serial port connected to the device.", 'text-bg-warning bg-opacity-50')
				}, 10000);
		});
	})
	.fail((jqXHR)=>{

		if(jqXHR.responseJSON?.message){
			if(showError)
				showToast(jqXHR.responseJSON.error, jqXHR.responseJSON.message, 'text-bg-danger bg-opacity-50');
		}else{
			const status = f_alarmStatus('Closed');
			$('#summaryAlarmTitle').text(status.text);
		}

	});
})();
