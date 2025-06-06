
import {start as piStart, stop as piStop, onTypeChange} from './panel-info.js'
import {start as measStart, stop as measStop} from './panel-measurement.js'
import {start as controlStart, stop as controlStop, disable as contrilDisable} from './panel-control.js'
import {start as userStart, stop as userStop, disable as userDisable} from './user-panels.js'
import {start as summaryAlarmStart, stop as summaryAlarmStop, onStatusChange} from './panel-summary-alarm.js'
import {status as f_alarmStatus} from './packet/parameter/value/alarm-status.js'
import UnitAddress from './classes/unit-address.js'
import Baudrate from './classes/baudrate.js'

const $modal = $('#modal');
const $serialPort = $('select[id=serialPort]').change(portSelected);
const $btnStart =$('#btnStart').change(toggleStart);
const $conections = $('#conections');
$('#btnShowErrors').change(btnShowErrorsChange);

const baudrate = new Baudrate($('#baudrate'));
const unitAddress = new UnitAddress($('#unitAddress'));

let serialPort;
let run;
let showError;

const sessionId = 'sessionId' + Math.random().toString(16).slice(2);
function countConnections(){
	$.post('/connection/add', {connectionId: sessionId})
	.done(count=>{
		const text = count + ' conections';
		$conections.text()!==text && $conections.text(text);
	});
}

const interval = setInterval(()=>showToast("Serial port not selected.", "Select the serial port connected to the device.", 'text-bg-warning bg-opacity-50'), 10000);
getPortNames();
function getPortNames(){
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
			}
		});
	})
	.fail((jqXHR)=>{

		if(jqXHR.responseJSON?.message){
			if(showError)
				showToast(jqXHR.responseJSON.error, jqXHR.responseJSON.message, 'text-bg-danger bg-opacity-50');
		}else{
			const status = f_alarmStatus('Closed');
			$('#summaryAlarmTitle').text(status.text);
			setAlarm(status);
		}

	});
}

function portSelected(e){
	serialPort = e.currentTarget.value;
	Cookies.set('serialPort', serialPort);
	if($btnStart.prop('disabled'))
		$btnStart.attr('disabled', false);
	toggleStart();
}

function btnShowErrorsChange(e){
	showError = e.currentTarget.checked;
	if(showError)
		showToast('Display of error messages is enabled.', 'Error information will be displayed here..');
}

let countInterva;
function toggleStart(){

	clearInterval(interval);
	const $lbl = $btnStart.next();
	const text = $lbl.text();

	switch(text){
	case 'Start':
		summaryAlarmStart()
//		, piStart(); measStart(); controlStart(); userStart();
		$lbl.text('Stop');
		$btnStart.attr('checked', true);
		run = true;
		countInterva = setInterval(countConnections, 3000);
		break;

	default:
		summaryAlarmStop(), piStop(); measStop(); controlStop(); userStop();
		$lbl.text('Start');
		$btnStart.attr('checked', false);
		clearInterval(countInterva);
		$conections.empty()
		$.post('/serial/close', {spName: serialPort})
	}
}

$('#appExit').click(async ()=>{

	const x = await $.post('/connection/add', {connectionId: sessionId}).fail(showExit);

	if(x < 2 || confirm(`${x - 1} more connection found.\nAre you sure you want to close this program?`))
		$.get('/exit').done(showExit).fail(showExit);
});

function showExit(){
	run = false;
	summaryAlarmStop();
	stop();
	$modal.load('/modal/exit');
	$modal.attr('data-bs-backdrop', 'static');
	$modal.modal('show');
}

onStatusChange(setAlarm);
function setAlarm(sunnaryAlarm){

	if(!sunnaryAlarm){
		stop();
		return;
	}

	switch(sunnaryAlarm.severities){

	case 'NO_ALARM':
	case 'CRITICAL':
	case 'INFO':
	case 'WARNING':
	case 'MINOR':
	case 'MAJOR':
		piStart();
		break;

	case 'TIMEOUT':
		contrilDisable();
		stop();
		break;

	case 'Closed':
	case 'SP Error':
		$btnStart.prop('checked') && $btnStart.click();
		contrilDisable();
		stop();
		break;

	default:
	case 'UNKNOWN':
		console.warn(sunnaryAlarm);
		if(sunnaryAlarm.text==='Closed' && $btnStart.prop('checked')){
			$btnStart.click();
			contrilDisable();
		}else
			stop();
	}
}
function stop(){
	piStop(); measStop(); controlStop(); userStop();
}

onTypeChange(()=>{
	measStart(); controlStart(); userStart();
})

export {serialPort, baudrate, unitAddress, run, showError}