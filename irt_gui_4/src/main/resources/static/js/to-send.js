import packetId, {toString as f_packetIdToString} from './packet/packet-properties/packet-id.js';
import packetType from './packet/packet-properties/packet-type.js';
import groupId from './packet/packet-properties/group-id.js';
import {serialPort, baudrate, unitAddrClass} from './serial-port.js';
import Packet, {Header, Payload} from './packet/packet.js';
import {shortToBytesR, intToBytes, longToBytes} from './packet/service/converter.js';

export default async function (action, callBack){

	const addr = unitAddrClass.unitAddress;
	if(action.update || action.toSend?.id!==action.packetId || action.toSend?.unitAddr!==addr || (action.unitAddr && action.unitAddr!==action.toSend?.unitAddr)){

		if(!action?.packetId===undefined)
			throw new Error('The variable "action" must have packetId');

		if(!action?.groupId)
			throw new Error('The variable "action" must have groupId');

		if(!action?.function)
			throw new Error('The variable "action" must have function');

		action.toSend = {};
		if(action.name)
			action.toSend.name = action.name;
		action.toSend.id = action.packetId;
		if(action.unitAddr)
			action.toSend.unitAddr = action.unitAddr;
		else
			action.toSend.unitAddr = addr;
		action.toSend.timeout = action.timeout ?? 2000;
		action.toSend.function = action.function;
		action.toSend.command = action.command ?? false;

		const rest = await getRest(action);
		Object.assign(action.toSend, rest);
	}

	action.toSend.serialPort = serialPort;
	action.toSend.baudrate = baudrate.baudrate;

	if(action.toSend.bytes === undefined){
		console.warn('No data to send.', action);
		action.toSend = undefined;
		action.buisy = false;
		return;
	}
	callBack(action.toSend);
}

async function getRest(action){

	const toSend = {};

	switch(action.toSend.id){

	case packetId.measurementIRPC:
	case packetId.irpc:
	case packetId.odrc:
		{
			const packet = new Packet(new Header(packetType.request,  action.toSend.id, action.groupId), undefined, action.toSend.unitAddr);
			toSend.bytes = packet.toSend();
		}
		break;

	case packetId.deviceInfo:
	case packetId.measurement:
	case packetId.alarmSummary: 
	case packetId.alarmIDs:
	case packetId.network: // get network
	case packetId.module:	// All modules
	case packetId.calMode:
		{
			const packet = new Packet(new Header(packetType.request, action.toSend.id, action.groupId), new Payload(action.data.parameterCode), action.toSend.unitAddr);
			toSend.bytes = packet.toSend();
		}
		break;

	case packetId.dump:
	case packetId.dumpHelp:
	case packetId.stuw81300Bias:
	case packetId.admv1013Bias:
		{
			const packet = new Packet(new Header(packetType.request, action.toSend.id, action.groupId), new Payload(action.data.parameterCode, intToBytes(action.data.value)), action.toSend.unitAddr);
			toSend.bytes = packet.toSend();
		}
		break;

	case packetId.POTs_KA_BIAS:
		{
			const pls = action.data.value.map(v=>new Payload(action.data.parameterCode, intToBytes(v)));
			const packet = new Packet(new Header(packetType.request, action.toSend.id, action.groupId), pls, action.toSend.unitAddr);
			toSend.bytes = packet.toSend();
		}
		break;

	case packetId.atenuationSet:
	case packetId.gainSet:
		{
			const packet = new Packet(new Header(packetType.command,  action.toSend.id, action.groupId), new Payload(action.data.parameterCode,shortToBytesR(action.data.value)), action.toSend.unitAddr);
			toSend.bytes = packet.toSend();
		}
		break;

	case packetId.irpcDefault:
	case packetId.irpcHoverA:
	case packetId.irpcHoverB:
	case packetId.calModeSet:
	case packetId.dacSetRcm:
		{
			const packet = new Packet(new Header(packetType.command, action.toSend.id, action.groupId), new Payload(action.data.parameterCode, intToBytes(action.data.value)), action.toSend.unitAddr);
			toSend.bytes = packet.toSend();
		}
		break;

	case packetId.frequencySet:
	case packetId.comSetBaudrate :
		{
			const packet = new Packet(new Header(packetType.command,  action.toSend.id, action.groupId), new Payload(action.data.parameterCode, longToBytes(action.data.value)), action.toSend.unitAddr);
			toSend.bytes = packet.toSend();
		}
		break;

	case packetId.configAll:
	case packetId.comAll:
	case packetId.redundancyAll:
	case packetId.dacRcm:
		{
			const pls = action.data.parameterCode.map(pc=>new Payload(pc));
			const packet = new Packet(new Header(packetType.request, action.toSend.id, action.groupId), pls, action.toSend.unitAddr);
			toSend.bytes = packet.toSend();
		}
		break;

	case packetId.alarm:
	case packetId.alarmDescription:
		{
			const pls = action.data.value.map(v=>new Payload(action.data.parameterCode, shortToBytesR(v)));
			const packet = new Packet(new Header(packetType.request, action.toSend.id, action.groupId), pls, action.toSend.unitAddr);
			toSend.bytes = packet.toSend();
		}
		break;

	case packetId.register:
	case packetId.register1:
	case packetId.register2:
	case packetId.register3:
	case packetId.register4:
		{
			const packet = new Packet(new Header(packetType.request,  action.toSend.id, action.groupId), new Payload(action.data.parameterCode, action.data.value), action.toSend.unitAddr);
			toSend.bytes = packet.toSend();
		}
		break;

	case packetId.registerSet:
		{
			const packet = new Packet(new Header(packetType.command,  action.toSend.id, groupId.deviceDebug), new Payload(action.value.parameterCode, action.value.bytes), action.toSend.unitAddr);
			toSend.bytes = packet.toSend();
			toSend.timeout = 3000;
			toSend.function = 'f_handler';
		}
		break;

	case packetId.muteSet:
	case packetId.loSet:
	case packetId.comSetAddress :
	case packetId.comSetRetransmit:
	case packetId.comSetStandard:
	case packetId.irpcSalectSwtchHvr:
	case packetId.irpcStandBy:
	case packetId.moduleSet:
	case packetId.odrcSetMode:
	case packetId.lnbSetMode:
	case packetId.odrcLNBSelect :
	case packetId.lnbOverSet :
		{
			const packet = new Packet(new Header(packetType.command, action.toSend.id, action.groupId), new Payload(action.data.parameterCode, [action.data.value]), action.toSend.unitAddr);
			toSend.bytes = packet.toSend();
		}
		break;

	case packetId.networkSet: // set network
		{
			const packet = new Packet(new Header(packetType.command, action.toSend.id, action.groupId), new Payload(action.data.parameterCode, action.data.value), action.toSend.unitAddr);
			toSend.bytes = packet.toSend();
		}
		break;

		// request with multiple payloads
	case packetId.dacs:
	case packetId.admv1013:
	case packetId.stuw81300:
	case packetId.POTs_KA_Converter:
	case packetId.lnbRegisters:
		{
			const pls = action.data.value.map(v=>new Payload(action.data.parameterCode, v.toBytes()));
			const packet = new Packet(new Header(packetType.request, action.toSend.id, action.groupId), pls, action.toSend.unitAddr);
			toSend.bytes = packet.toSend();
		}
		break;

		// command with multiple payloads
	case packetId.admv1013Set:
	case packetId.admv1013BiasSet:
		{
			const pls = action.data.value.map(v=>new Payload(action.data.parameterCode, v.toBytes()));
			const packet = new Packet(new Header(packetType.command, action.toSend.id, action.groupId), pls, action.toSend.unitAddr);
			toSend.bytes = packet.toSend();
			
		}
		break;

		// command with single payload
	case packetId.dacsSet:
	case packetId.stuw81300Set:
	case packetId.lnbRegistersSet:
		{
			const pl = new Payload(action.data.parameterCode, action.data.value.toBytes());
			const packet = new Packet(new Header(packetType.command, action.toSend.id, action.groupId), pl, action.toSend.unitAddr);
			toSend.bytes = packet.toSend();
		}
		break;

	default:
		console.warn(f_packetIdToString(action.toSend.id), action);
	}
	return toSend;
}