import Baudrate from './classes/baudrate.js';
import ControllerStm32 from './controller/controller-stm32.js';
import FlashAnswer from './flash/flash-answer.js';

let spInterval;
let doLock = true;
const requestPacket = {name: 'Flash', id: 555, command: true, unitAddr: 0, timeout: 2000, function: 'f_parseData', f_parseData: f_parseData};

const $nav = $('nav');
const $content = $('#content').css('marginTop', $nav.height()+16);	// 16px is $nav top + bottom margin
$(window).resize(()=>$content.css('marginTop', $nav.height()+16));
const $selectSP = $('#selectSP').change(spChange).keydown(spKeyDown);
const $selectBR = $('#selectBR').keydown(spKeyDown);
const baudrate = new Baudrate($selectBR);
const $cbFlashConnect = $('#cbFlashConnect').keydown(spKeyDown).change(connectChange);
const $tdPortStatus = $('#tdPortStatus');
const $connectionStatus = $('#connectionStatus');
const $tdProcessor = $('#tdProcessor');

const controllerFlash = new ControllerStm32();

(()=>{
	const sp = Cookies.get('serialPort');
	if(sp)
		$selectSP.val(sp).change();
})();
function spChange({currentTarget:{value}}) {
	if(!value || value==='Select Serial Port'){
		$cbFlashConnect.attr('disabled', true);
		spInterval = clearInterval(spInterval);
		return;
	}
	$cbFlashConnect.attr('disabled', false);
	requestPacket.serialPort = value;
	doLock = true;
	sendLock(value);
	clearInterval(spInterval);
	spInterval = setInterval(()=>sendLock(value), 5000);
	Cookies.set('serialPort', value, {expires: 365, path: ''});
}
function sendLock(sp) {
	$.post('/flash/rest/lock', {sp, lock: doLock}, spIsOpen => {
		if(!spIsOpen && !$selectSP.val())
			spInterval = clearInterval(spInterval);
		const portStatus = spIsOpen ? 'Open' : "Close";
		if($tdPortStatus.text()!=portStatus)
			$tdPortStatus.text(portStatus);
	})
	.fail((jqXHR, textStatus, errorThrown) => {
		console.error('Error sending lock request:', textStatus, errorThrown);
		spKeyDown({key: 'Escape'});
	});
}
function spKeyDown({key}){
	if(!spInterval)
		return;

	switch(key){

	case 'Escape':
		doLock = false;
		$selectSP.find('option:first').prop('selected', true);
		sendLock(requestPacket.serialPort, false);
		$cbFlashConnect.attr('disabled', true);
		$selectSP.change();
		break;

	default:
		console.log("Key pressed: ", key)
	}
}
function connectChange({currentTarget:{checked}}){
	if(checked){
		requestPacket.baudrate = baudrate.baudrate;
		controllerFlash.connect(requestPacket);
		$cbFlashConnect.attr('disabled', true);
		$connectionStatus.text('');
	}
}
function f_parseData(data){
	console.log(data);

	if(data.error){
		$connectionStatus.text(requestPacket.error).css('color', 'red');
		$cbFlashConnect.prop('checked', false).attr('disabled', false);
		requestPacket.error = '';
		return;
	}

	switch(data.commandName){

	case 'GET_ID':
		$tdProcessor.text(data.message);
		break;

	default:
		console.warn(data);
	}
}
