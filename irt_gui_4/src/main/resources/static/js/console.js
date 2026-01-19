import * as serialPort from './console/csl-serial-port.js';
import Baudrate from './classes/baudrate.js';

$('#unitAddress').parent().hide();
$('#btnStart').parent().hide();
$('#summaryAlarmCard').parent().hide();
$('#fwUpgrade').parent().hide();
$('#btnShowErrors').parent().hide();

const $commandInput = $('#commandInput');
const baudrate = new Baudrate($('#baudrate'));

$('#sendCommandBtn').click(({currentTarget:btn})=>{
	const sp = serialPort.serialPort;
	if(!sp){
		alert('No serial port selected');
		return;
	}
	btn.disabled = true;
	const br = baudrate.baudrate;
	const command = $commandInput.val();
	$.get('/console/rest/send', {sp: sp, br: br, command: command})
	.done((data)=>{
		btn.disabled = false;
		console.log('Command sent', data);
	})
	.fail((err)=>{
		btn.disabled = false;
		console.error(err);
		alert(err.responseText);
	});
});