import Baudrate from '../classes/baudrate.js'

export let serialPort; 

$('#serialPort').next().text(txtSerialPort232)
$('#unitAddress').parent().hide();
$('#btnStart').parent().hide();
$('#summaryAlarmCard').parent().hide().next().hide();
$('#fwUpgrade').parent().hide();

const $serialPort = $('#serialPort').change(serialPortChange);
const $baudrate = $('#baudrate');
const $connections = $('#connections');
const $modal = $('#modal');

export const baudrate = new Baudrate($baudrate);

const sessionId = 'sessionId' + Math.random().toString(16).slice(2);
let conCheckInterval;
(function getPortNames(){
	countConnections();
	conCheckInterval = setInterval(countConnections, 20000);
	$.get('/serial/ports')
	.done(ports=>{
		if(!ports?.length){
//			coverButSerial(true);
			return;
		}
		const serialPortCookies = Cookies.get('cslSerialPort');
		ports.forEach(name=>{
			const selected = serialPortCookies === name;
			if(selected)
				serialPort = name;
			$('<option>', {text: name, selected: selected}).appendTo($serialPort);
			if(selected){
//				bthStartDisable(false);
				$serialPort.change();
			}
		});
//		if($serialPort.val())
//			coverButSerial();
//		else
//			coverButSerial(true);
	})
	.fail((jqXHR)=>{

		if(jqXHR.responseJSON?.message){
			if(showError)
				showToast(jqXHR.responseJSON.error, jqXHR.responseJSON.message, 'text-bg-danger bg-opacity-50');
		}else{
			const status = f_alarmStatus('Closed');
			if(txtStatus[status.text])
				$('#summaryAlarmTitle').text(txtStatus[status.text]);
			else
				$('#summaryAlarmTitle').text(status.text);
		}

	});
})();
let failsCount = 0;
function countConnections(){
	$.post('/connection/add', {connectionId: sessionId})
	.done(count=>{
		failsCount = 0;
		const text = (count + ' ' + txtConnection) + (count===1 ? '' : 's');
		$connections.text()!==text && $connections.text(text);
	})
	.fail(()=>{
		if(failsCount > 3)
			location.reload();
		failsCount++;
	});
}
function serialPortChange(){
	serialPort = $serialPort.val();
	Cookies.set('cslSerialPort', serialPort, {expires: 365, path: '/console'});
}
$('#appExit').click(async ()=>{

	try{
		const x = await $.post('/connection/add', {connectionId: sessionId});

		if(x < 2 || confirm(`${x - 1} more connection found.\nAre you sure you want to close this program?`))
			$.get('/exit').always(showExitModal);

		}catch(e){
		showExitModal();
	}
});
function showExitModal(){
	$modal.load('/modal/exit');
	$modal.attr('data-bs-backdrop', 'static');
	$modal.modal('show');
	clearInterval(conCheckInterval);
}
