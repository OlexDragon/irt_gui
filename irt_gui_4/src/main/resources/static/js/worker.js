
import {start as piStart, stop as piStop, onTypeChange} from './panel-info.js'
import {start as measStart, stop as measStop} from './panel-measurement.js'
import {start as controlStart, stop as controlStop} from './panel-control.js'
import {start as userStart, stop as userStop} from './user-panels.js'
import {start as summaryAlarmStart, stop as summaryAlarmStop, onStatusChange} from './panel-summary-alarm.js'

const $modal = $('#modal');
const $serialPort = $('select[id=serialPort]').change(portSelected);
const $btnStart =$('#btnStart').change(toggleStart);
const $baudrate = $('#baudrate').change(baudrateChange);
$('#btnShowErrors').change(btnShowErrorsChange);

$('#unitAddress').change(unitAddressChange);

let serialPort;
let baudrate;
let unitAddress = 254;
let run;
let showError;

let cookies =  Cookies.get('baudrate');
if(!cookies)
	cookies = 115200;
$baudrate.val(cookies);

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
	});
}

function portSelected(e){
	serialPort = e.currentTarget.value;
	Cookies.set('serialPort', serialPort);
	if($btnStart.prop('disabled'))
		$btnStart.attr('disabled', false);
	toggleStart();
}

function baudrateChange(e){
	baudrate = e.currentTarget.value;
	Cookies.set('baudrate', e.currentTarget.value);
}

function unitAddressChange(e){
	unitAddress = e.currentTarget.value;
}

function btnShowErrorsChange(e){
	showError = e.currentTarget.checked;
	if(showError)
		showToast('Display of error messages is enabled.', 'Error information will be displayed here..');
}

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
		break;

	default:
		summaryAlarmStop(), piStop(); measStop(); controlStop(); userStop();
		$lbl.text('Start');
		$btnStart.attr('checked', false);
		showToast('The GUI stopped accessing the serial port.', 'The serial port will be released in 20 seconds.');
		
	}
}

$('#appExit').click(()=>{

	run = false;
	$btnStart.attr('checked', false);

	$.get('/exit').done(()=>{
		$modal.load('/modal/exit');
		$modal.attr('data-bs-backdrop', 'static');
		$modal.modal('show');
	})
	.fail((jqXHR)=>{
		if(!jqXHR.responseText){
			if($btnStart.prop('checked'))
				$btnStart.click();
			alert('It looks like the IRT GUI is closed.');
		}
	});

});

onStatusChange(sunnaryAlarm=>{

	if(!sunnaryAlarm){
		piStop(); measStop(); controlStop(); userStop();
		return;
	}

	switch(sunnaryAlarm.severities){

		case 'NO_ALARM':
		case 'CRITICAL':
		case 'INFO':
		case 'WARNING':
		case 'MINOR':
		case 'MAJOR':
		piStart(); measStart();
		break;

	default:
		piStop(); measStop(); controlStop(); userStop();
		console.warn(sunnaryAlarm);
	}
});

onTypeChange(()=>{
	controlStart(); userStart();
})

export {serialPort, baudrate, unitAddress, run, showError}