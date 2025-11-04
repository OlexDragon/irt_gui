import * as serialPort from './serial-port.js'
import groupId from './packet/packet-properties/group-id.js'
import packetId from './packet/packet-properties/packet-id.js'
import f_deviceType from './packet/service/device-type.js'
import NetworkControl from './network/network-control.js'

const $card = $('#userCard');
const $body = $('#network-tab-pane');

const action = {packetId: packetId.network, groupId: groupId.network, data: {parameterCode: 1}, function: 'f_network'};

let networkControl;
let interval;
let delay = 10000;

export function start(){

		if(interval)
		return;

	action.buisy = false;

	if(!networkControl){
		const name = chooseFragmentName();
		$body.load(`/fragment/network/${name}`, ()=>{
			networkControl = new NetworkControl($body);
			networkControl.onChange(onChange);
			networkControl.onNotSaved(onNotSaved);
			run();
		});
	}else
		run();

	clearInterval(interval);
	interval = setInterval(run, delay);
}
export function stop(){
	clearInterval(interval) ;
	interval = undefined;
}
export function disable(){
	networkControl?.disable();
}

function chooseFragmentName(){
	const type = f_deviceType();
	switch(type){
	default:
		return 'buc';	
	}
}

//const packetIdSet = f_packetId('networkSet');

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

action.f_network = function(packet){

	packet.payloads.forEach(pl=>{
		switch(pl.parameter.code){

		case 1:
			parseNetwork(pl);
			break;

		default:
			console.warn(pl);
		}
	});
}

function parseNetwork(pl){

	if(pl.data?.length!=13){
		console.warn('Wrong data size.');
		return;
	}

	networkControl.value = pl.data;
}

const actionSet = Object.assign({}, action);
actionSet.packetId = packetId.networkSet;
//actionSet.data = {};
actionSet.toSend = {};

function onChange(ipAddress){
	actionSet.update = true;
	actionSet.data.value = ipAddress.bytes;
	serialPort.postObject($card, actionSet);
}

function onNotSaved(e){

	serialPort.showToast("Network settings are not saved.", e.currentValue.toString(), 'text-bg-warning bg-opacity-50');
	console.log(e);
}
