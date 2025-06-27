import packetId, {id as f_packetId, toString as f_packetIdToString} from './packet/packet-properties/packet-id.js'
import packetType from './packet/packet-properties/packet-type.js'
import groupId from './packet/packet-properties/group-id.js'
import {serialPort, baudrate, unitAddrClass} from './serial-port.js'
import Packet, {Header, Payload, Parameter} from './packet/packet.js'
import {shortToBytesR, intToBytes, longToBytes} from './packet/service/converter.js'

export default async function (action, callBack){

	const addr = unitAddrClass.unitAddress;
	if(action.update || action.toSend?.id!==action.packetId || action.toSend?.unitAddr!==addr){

		if(!action?.packetId===undefined)
			throw new Error('The variable "action" must have packetId');

		if(!action?.groupId)
			throw new Error('The variable "action" must have groupId');

		if(!action?.function)
			throw new Error('The variable "action" must have function');

		action.toSend = {};
		action.toSend.id = action.packetId;
		action.toSend.unitAddr = addr;
		action.toSend.timeout = action.timeout ?? 2000;
		action.toSend.function = action.function;

		const rest = await getRest(action);
		Object.assign(action.toSend, rest);
	}

	action.toSend.serialPort = serialPort;
	action.toSend.baudrate = baudrate.baudrate;

	callBack(action.toSend);
}

async function getRest(action){

	const toSend = {};

	switch(action.toSend.id){

	case packetId.deviceInfo:
	case packetId.measurement:
	case packetId.alarmSummary: 
	case packetId.alarmIDs:
	case packetId.network: // get network
		{
			const packet = new Packet(new Header(packetType.request, action.toSend.id, action.groupId), new Payload(new Parameter(action.data.parameterCode)), action.toSend.unitAddr);
			toSend.bytes = packet.toSend();
		}
		break;

	case packetId.measurementIRPC:
	case packetId.irpc:
		{
			const packet = new Packet(new Header(packetType.request,  action.toSend.id, action.groupId), undefined, action.toSend.unitAddr);
			toSend.bytes = packet.toSend();
		}
		break;

	// All modules
	case packetId.module:
		{
			const packet = new Packet(new Header(packetType.request,  action.toSend.id, groupId.control), new Payload(new Parameter(action.value)), action.toSend.unitAddr);
			toSend.bytes = packet.toSend();
			toSend.timeout = 1000;
		}
		break;

	case packetId.moduleSet:
		{
			const packet =  await import('./packet/parameter/config-buc.js')

				.then(m=>m.default)
				.then(parameter=>new Packet(new Header(packetType.command,  action.toSend.id, groupId.control), new Payload(new Parameter(parameter.activeModule), [action.value]), action.toSend.unitAddr));

			toSend.bytes = packet.toSend();
			toSend.timeout = 1000;
		}
		break;

	case packetId.atenuationSet:
	case packetId.gainSet:
		{
			const packet = new Packet(new Header(packetType.command,  action.toSend.id, action.groupId), new Payload(new Parameter(action.data.parameterCode),shortToBytesR(action.data.value)), action.toSend.unitAddr);
			toSend.bytes = packet.toSend();
		}
		break;

	case packetId.frequencySet:
	case packetId.comSetBaudrate :
		{
			const packet = new Packet(new Header(packetType.command,  action.toSend.id, action.groupId), new Payload(new Parameter(action.data.parameterCode), longToBytes(action.data.value)), action.toSend.unitAddr);
			toSend.bytes = packet.toSend();
		}
		break;

	case packetId.register:
	case packetId.register1:
	case packetId.register2:
	case packetId.register3:
	case packetId.register4:
		{
			const packet = new Packet(new Header(packetType.request,  action.toSend.id, groupId.deviceDebug), new Payload(new Parameter(action.value.parameterCode), action.value.bytes), action.toSend.unitAddr);
			toSend.bytes = packet.toSend();
			toSend.timeout = 5000;
			toSend.function = 'f_handler';
		}
		break;

	case packetId.registerSet:
		{
			const packet = new Packet(new Header(packetType.command,  action.toSend.id, groupId.deviceDebug), new Payload(new Parameter(action.value.parameterCode), action.value.bytes), action.toSend.unitAddr);
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
		{
			const packet = new Packet(new Header(packetType.command, action.toSend.id, action.groupId), new Payload(new Parameter(action.data.parameterCode), [action.data.value]), action.toSend.unitAddr);
			toSend.bytes = packet.toSend();
		}
		break;

	case packetId.alarm:
	case packetId.alarmDescription:
		{
			const pls = action.data.value.map(v=>new Payload(new Parameter(action.data.parameterCode), shortToBytesR(v)));
			const packet = new Packet(new Header(packetType.request, action.toSend.id, action.groupId), pls, action.toSend.unitAddr);
			toSend.bytes = packet.toSend();
		}
		break;

	case packetId.networkSet: // set network
		{
			const packet = new Packet(new Header(packetType.command, action.toSend.id, action.groupId), new Payload(new Parameter(action.data.parameterCode), action.data.value), action.toSend.unitAddr);
			toSend.bytes = packet.toSend();
		}
		break;

	case packetId.irpcDefault:
	case packetId.irpcHoverA:
	case packetId.irpcHoverB:
		{
			const packet = new Packet(new Header(packetType.command, action.toSend.id, action.groupId), new Payload(new Parameter(action.data.parameterCode), intToBytes(action.data.value)), action.toSend.unitAddr);
			toSend.bytes = packet.toSend();
		}
		break;

	case packetId.configAll:
	case packetId.comAll:
		{
			const pls = action.data.parameterCode.map(pc=>new Payload(pc));
			const packet = new Packet(new Header(packetType.request, action.toSend.id, action.groupId), pls, action.toSend.unitAddr);
			toSend.bytes = packet.toSend();
		}
		break;

			case packetId.module:
				packet = new Packet(new Header(packetType.request, id, groupId.control), undefined, linkAddr);
				need.function = 'fCom';
				need.timeout = 1000;
				break;

	default:
		console.warn(f_packetIdToString(action.toSend.id));
	}
	return toSend;
}