import * as serialPort from './serial-port.js'
import groupId from './packet/packet-properties/group-id.js'
import packetId from './packet/packet-properties/packet-id.js'
import f_deviceType from './packet/service/device-type.js'
import ComControl from './classes/com-control.js'
import protocol, {parser} from './packet/parameter/protocol.js'


const $card = $('#userCard');
const $body = $('#com-tab-pane');

const action = {packetId: packetId.comAll, groupId: groupId.protocol, data: {}, function: 'f_com'};

let comControl;
let interval;
let delay = 10000;
let type;

export function start(){

	stop()

	if(interval)
		return;

	action.buisy = false;
	type = f_deviceType();
	switch(type){

	case 'CONTROLLER_IRPC':
	case 'CONTROLLER_ODRC':
		action.data.parameterCode = [protocol.address.code, protocol.baudrate.code, protocol.retransmit.code];
		break;

	default:
		console.log(type);
	case 'BAIS':
	case 'CONTROLLER':
		action.data.parameterCode = [protocol.address.code, protocol.baudrate.code, protocol.retransmit.code, protocol.tranceiver_mode.code];
	}

	const name = chooseFragmentName();
	$body.load(`/fragment/com/${name}`, ()=>{
		const $comAddress = $('#comAddress');
		const $comRetransmits = $('#comRetransmits');
		const $comStandard = $('#comStandard');
		const $comBaudrate = $('#comBaudrate');
		comControl = new ComControl($comAddress, $comRetransmits, $comStandard, $comBaudrate);
		comControl.onChange(onChange);

		switch(type){
		case 'CONTROLLER_IRPC':
		case 'CONTROLLER_ODRC':
			$comStandard.parents('.to-hide').addClass('visually-hidden');
		}
//		networkControl.onChange(onChange);
//		networkControl.onNotSaved(onNotSaved);
		run();

		clearInterval(interval) ;
		interval = setInterval(run, delay);
	});
}
export function stop(){
	clearInterval(interval) ;
	interval = undefined;
}
export function disable(){
	comControl?.disable();
}
function chooseFragmentName(){
	const type = f_deviceType();
	switch(type){
	default:
		return 'buc';	
	}
}

function run(){
	if(!serialPort.doRun()){
		stop();
		return;
	}

	if(action.buisy){
		console.log('action.buisy');
		return
	}

	action.buisy = true;

	serialPort.postObject($card, action);
}

action.f_com = function(packet){

	const payloads = packet.payloads;

	comControl.disable = false;

	payloads.forEach(pl=>{

		const value = parser(pl.parameter.code)(pl.data);
		switch(pl.parameter.code){

		case protocol.address.code:
			comControl.address = value;
			serialPort.unitAddrClass.unitAddress = value;
			break;

		case protocol.retransmit.code:
			comControl.retransmits = value;
			break;

		case protocol.baudrate.code:
			comControl.baudrate = value;
			break;

		case protocol.tranceiver_mode.code:
			comControl.standard = value;
			break;

		default:
			console.warn(pl);
		}
	});
}

const actionSet = Object.assign({}, action);
actionSet.data = {};

function onChange(v){

	actionSet.update = true;

	const keys = Object.keys(v);
	if(!keys.length){
		console.warn('something went wrong.')
		return;
	}
	const key = keys[0];
	switch(key){

	case 'address':
		actionSet.packetId = packetId.comSetAddress;
		actionSet.data.value = v.address;
		actionSet.data.parameterCode = protocol.address.code;
		break;

	case 'retransmit':
		actionSet.packetId = packetId.comSetRetransmit;
		actionSet.data.value = v.retransmit;
		actionSet.data.parameterCode = protocol.retransmit.code;
		break;

	case 'standard':
		actionSet.packetId = packetId.comSetStandard;
		actionSet.data.value = v.standard;
		actionSet.data.parameterCode = protocol.tranceiver_mode.code;
		break;

	case 'baudrate':
		actionSet.packetId = packetId.comSetBaudrate;
		actionSet.data.value = v.baudrate;
		actionSet.data.parameterCode = protocol.baudrate.code;
		break;

	default:
		console.warn(v);
		return;
	}

	serialPort.postObject($card, actionSet);
	switch(key){

	case 'baudrate':
		setTimeout(()=>serialPort.baudrate.baudrate = v.baudrate, 100);
	}
}
