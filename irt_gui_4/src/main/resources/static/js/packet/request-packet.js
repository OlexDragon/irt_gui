import Packet from './packet.js'
import Header from './header.js'
import Payload from './payload.js'
import Parameter from './parameter.js'
import groupId from './packet-properties/group-id.js'
import {payload as alarmPayload, payloads as alarmPayloads} from './parameter/alarm.js'
import {serialPort, baudrate, unitAddress} from '../worker.js'
import {code as configCode} from './parameter/control.js'
import {code as protocolCode} from './parameter/protocol.js'
import {code as irpcCode} from './parameter/irpc.js'
import packetType from './packet-properties/packet-type.js'
import packetId, {toString as pIdToString} from './packet-properties/packet-id.js'
import { shortToBytesR, intToBytes, longToBytes } from './service/converter.js'

export default class RequestPackt{
	constructor(id, linkAddr, value){
		this.id = this.#packetId(id);
		if(Array.isArray(linkAddr)){
			this.unitAddr = unitAddress.unitAddress;
			value = linkAddr;
		}else
			this.unitAddr = linkAddr ? linkAddr : unitAddress.unitAddress;
		const whatNeed = getWhatNeed(id, this.unitAddr, value);
		this.bytes = whatNeed.bytes;
		this.function = whatNeed.function;
		this.timeout = whatNeed.timeout;
		this.serialPort = serialPort;
		this.baudrate = baudrate.baudrate;
	}

	#packetId(id){
		if(typeof id === 'number')
			return id;
		return groupId[id];
	}
}

function getWhatNeed(id, linkAddr, value){

	const need = {};
	let packet;

	switch(id){

// Measurement
	case packetId.measurement: // Measurement All
		packet = new Packet(new Header(packetType.request, id, groupId.measurement), undefined, linkAddr);
		need.function = 'fMeasurement';
		need.timeout = 5000;
		break;

	case packetId.measurementIRPC:
		{
			const payloads = [];
			for(let i=5; i<6; ++i)
				payloads.push(new Payload(i));
			packet = new Packet(new Header(packetType.request, id, groupId.measurement), payloads, linkAddr);
			console.log(packet);
			need.function = 'fMeasurement';
			need.timeout = 5000;
		}
		break;

// Network
	case packetId.network: // get network
		packet = new Packet(new Header(packetType.request, id, groupId.network), new Payload(new Parameter(1)), linkAddr);
		need.function = 'fNetwork';
		need.timeout = 3000;
		break;

	case packetId.networkSet: // set network
		packet = new Packet(new Header(packetType.command, id, groupIdnetwork), new Payload(new Parameter(1), value), linkAddr);
		console.log(packet)
		need.function = 'fNetwork';
		need.timeout = 3000;
		break;

// Alarm
	case packetId.alarmSummary: 
		packet = new Packet(new Header(packetType.request, id, groupId.alarm), alarmPayload('summary status'), linkAddr);
		need.function = 'fSummaryAlarms';
		need.timeout = 1000;
		break;

	case packetId.alarmIDs:
		packet = new Packet(new Header(packetType.request, id, groupId.alarm), alarmPayload('IDs'), linkAddr);
		need.function = 'fAlarms';
		need.timeout = 1000;
		break;

	case packetId.alarmDescription:
		{
			var payloads = alarmPayloads([value], true);
			packet = new Packet(new Header(packetType.request, id, groupId.alarm), payloads, linkAddr);
			need.function = 'fAlarms';
			need.timeout = 5000;
		}
		break;

	case packetId.alarm:
		{
			var payloads = alarmPayloads(value);
			packet = new Packet(new Header(packetType.request, id, groupId.alarm), payloads, linkAddr);
			need.function = 'fAlarms';
			need.timeout = 5000;
		}
		break;

	case packetId.redundancyAll:
		const pls =
		[
			new Payload(configCode('redundancy_enable')),
			new Payload(configCode('redundancy_mode')),
			new Payload(configCode('redundancy_name')),
			new Payload(configCode('redundancy_status'))
		]
		packet = new Packet(new Header(packetType.request, id, groupId.configuration), pls, linkAddr);
		need.function = 'fRedundancy';
		need.timeout = 5000;
		break;

	case packetId.redundancySetOnline:
		packet = new Packet(new Header(packetType.request, id, groupId.configuration), configCode('redundancy_set_online'), linkAddr);
		need.function = 'fRedundancy';
		need.timeout = 1000;
		break;

	case packetId.redundancySetEnable:
		packet = new Packet(new Header(packetType.command, id, groupId.configuration), new Payload(new Parameter(configCode('redundancy_enable')),[1]), linkAddr);
		need.function = 'fRedundancy';
		need.timeout = 1000;
		break;

	case packetId.redundancySetDisable:
		packet = new Packet(new Header(packetType.command, id, groupId.configuration), new Payload(new Parameter(configCode('redundancy_enable')),[0]), linkAddr);
		need.function = 'fRedundancy';
		need.timeout = 1000;
		break;

	case packetId.redundancySetCold:
		packet = new Packet(new Header(packetType.command, id, groupId.configuration), new Payload(new Parameter(configCode('redundancy_mode')),[0]), linkAddr);
		need.function = 'fRedundancy';
		need.timeout = 1000;
		break;

	case packetId.redundancySetHot:
		packet = new Packet(new Header(packetType.command, id, groupId.configuration), new Payload(new Parameter(configCode('redundancy_mode')),[1]), linkAddr);
		need.function = 'fRedundancy';
		need.timeout = 1000;
		break;

	case packetId.redundancySetNameA:
		packet = new Packet(new Header(packetType.command, id, groupId.configuration), new Payload(new Parameter(configCode('redundancy_name')),[1]), linkAddr);
		need.function = 'fRedundancy';
		need.timeout = 1000;
		break;

	case packetId.redundancySetNameB:
		packet = new Packet(new Header(packetType.command, id, groupId.configuration), new Payload(new Parameter(configCode('redundancy_name')),[2]), linkAddr);
		need.function = 'fRedundancy';
		need.timeout = 1000;
		break;

	case packetId.irpc:
		packet = new Packet(new Header(packetType.request, id, groupId.redundancy), undefined, linkAddr);
		need.function = 'f_IRPC';
		need.timeout = 2000;
		break;

	case packetId.irpcSalectSwtchHvr:
		packet = new Packet(new Header(packetType.command, id, groupId.redundancy), new Payload(new Parameter(irpcCode('Switchover Mode')), [value]), linkAddr);
		need.function = 'f_IRPC';
		need.timeout = 500;
		break;

	case packetId.irpcStandBy:
		packet = new Packet(new Header(packetType.command, id, groupId.redundancy), new Payload(new Parameter(irpcCode('Standby Mode')), [value]), linkAddr);
		need.function = 'f_IRPC';
		need.timeout = 500;
		break;

	case packetId.irpcDefault:
	case packetId.irpcHoverA:
	case packetId.irpcHoverB:
		packet = new Packet(new Header(packetType.command, id, groupId.redundancy), new Payload(new Parameter(irpcCode('Switchover')), intToBytes(value)), linkAddr);
		need.function = 'f_IRPC';
		need.timeout = 500;
		break;

	case packetId.configAll: // Control All
		{
			const payloads = [
				new Payload(new Parameter(configCode('gain'))),
				new Payload(new Parameter(configCode('gain_range'))),
				new Payload(new Parameter(configCode('attenuation'))),
				new Payload(new Parameter(configCode('attenuation_range'))),
				new Payload(new Parameter(configCode('frequency'))),
				new Payload(new Parameter(configCode('frequency_range')))					,
				new Payload(new Parameter(configCode('mute')))];
			packet = new Packet(new Header(packetType.request, id, groupId.configuration), payloads, linkAddr);
		}
		break;

	case packetId.atenuationSet:
		packet = new Packet(new Header(packetType.command, id, groupId.configuration), new Payload(new Parameter(configCode('attenuation')), shortToBytesR(value)), linkAddr);
		break;

	case packetId.gainSet:
		packet = new Packet(new Header(packetType.command, id, groupId.configuration), new Payload(new Parameter(configCode('gain')),shortToBytesR(value)), linkAddr);
		break;

	case packetId.frequencySet:
		packet = new Packet(new Header(packetType.command, id, groupId.configuration), new Payload(new Parameter(configCode('frequency')),longToBytes(value)), linkAddr);
		break;

	case packetId.muteControl:
		packet = new Packet(new Header(packetType.command, id, groupId.configuration), new Payload(new Parameter(configCode('mute')),[value]), linkAddr);
		break;

	case packetId.comAll:
		{
			const length = value.length
			const payloads = [];
			for(let i=0; i<length; ++i)
				payloads.push(new Payload(value[i]))
			packet = new Packet(new Header(packetType.request, id, groupId.protocol), payloads, linkAddr);
			need.function = 'fCom';
			need.timeout = 3000;
		}
		break;

	case packetId.comSetAddress :
		packet = new Packet(new Header(packetType.command, id, groupId.protocol), new Payload(new Parameter(protocolCode('address')),value), linkAddr);
		need.function = 'fCom';
		need.timeout = 1000;
		break;

	case packetId.comSetBaudrate :
		packet = new Packet(new Header(packetType.command, id, groupId.protocol), new Payload(new Parameter(protocolCode('baudrate')),value), linkAddr);
		need.function = 'fCom';
		need.timeout = 10;
		break;

	case packetId.conSetRetransmit:
		packet = new Packet(new Header(packetType.command, id, groupId.protocol), new Payload(new Parameter(protocolCode('retransmit')),value), linkAddr);
		need.function = 'fCom';
		need.timeout = 1000;
		break;

	case packetId.comSetStandard:
		packet = new Packet(new Header(packetType.command, id, groupId.protocol), new Payload(new Parameter(protocolCode('tranceiver_mode')),value), linkAddr);
		need.function = 'fCom';
		need.timeout = 1000;
		break;

	case packetId.module:
		packet = new Packet(new Header(packetType.request, id, groupId.control), undefined, linkAddr);
		need.function = 'fCom';
		need.timeout = 1000;
		break;

	case packetId.noAction:
		break

	default:
		console.warn('Have to add ' + pIdToString(id));
	}

	need.bytes = packet?.toSend();

	return need;
}
