import * as serialPort from './console/csl-serial-port.js';
import Baudrate from './classes/baudrate.js';

$('#btnShowErrors').parent().hide();
$('#clearText').click(clearText);

const $commandInput = $('#commandInput').change(sendCommand);
const $sendCommandBtn = $('#sendCommandBtn').click(sendCommand);
const $taConsole = $('#taConsole').on('select', onSelect);
const $commandHistory = $('#commandHistory').on('select', onSelect);
const baudrate = new Baudrate($('#baudrate'));
const history = new Set();

let path;

function sendCommand(){
	const sp = serialPort.serialPort;
	if(!sp){
		alert('No serial port selected');
		return;
	}
	$sendCommandBtn.prop('disabled', true);
	const br = baudrate.baudrate;
	const command = $commandInput.val().trim();
	history.add(command);
	const str = Array.from(history).join("\n");
	$commandHistory
	$commandHistory.val(str);
	resizeHistory();
	$.get('/console/rest/send', {sp: sp, br: br, command: command})
	.done((data)=>{
//		$commandInput.val('')
		$sendCommandBtn.prop('disabled', false);
		const answer = String.fromCharCode.apply(null, data.answer);
		if(!answer){
			path = '>'
		}
		$taConsole.val($taConsole.val() + answer);
		$taConsole.scrollTop($taConsole[0].scrollHeight);
		const split = answer.split('\n');
		if(split.length)
			path = split[length-1];
		else
			path = amswer;
	})
	.fail((err)=>{
		$sendCommandBtn.prop('disabled', false);
		console.error(err);
		alert(err.responseText);
	});
}
function onSelect({currentTarget:el}){
	console.log(el.selectionStart, el.selectionEnd)
	$commandInput.val(el.value.substring(el.selectionStart, el.selectionEnd).trim());
}
function clearText(){
	$taConsole.val('');
}
function resizeHistory(){
	const ta = $commandHistory[0];
	ta.style.height = "auto"; // Reset height
 	ta.style.height = ta.scrollHeight + "px"; // Set new height
}