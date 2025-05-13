import Packet from './packet.js'
import Header from './header.js'
import Payload from './payload.js'
import Parameter from './parameter.js'
import {id as groupId} from './packet-properties/group-id.js'
import {code as packetTypeCode} from './packet-properties/packet-type.js'
import {serialPort, baudrate, unitAddress} from '../worker.js'
import {id as pId, toString as pIdToString} from './packet-properties/packet-id.js'
import {code as configCode} from './parameter/configuration.js'
import {payload as alarmPayload} from './parameter/alarm.js'

export default class RequestPackt{
	constructor(id, linkAddr, value){
		this.id = packetId(id);
		this.unitAddr = linkAddr ? linkAddr : unitAddress;
		const whatNeed = getWhatNeed(id, this.unitAddr, value);
		this.bytes = whatNeed.bytes;
		this.function = whatNeed.function;
		this.timeout = whatNeed.timeout;
		this.serialPort = serialPort;
		this.baudrate = baudrate;
	}
}

function packetId(id){
	if(typeof id === 'number')
		return id;
	return groupId(id);
}

function getWhatNeed(id, linkAddr, value){

	const need = {};
	let packet;

	switch(id){

// Device Info
	case pId('deviceInfo'):	// Device Info
		packet = new Packet(new Header(packetTypeCode('request'), id), undefined, linkAddr);
		need.function = 'fInfo';
		need.timeout = 1000;
		break;

// Measurement
	case pId('measurementAll'): // Measurement All
		packet = new Packet(new Header(packetTypeCode('request'), id, groupId('measurement')), undefined, linkAddr);
		need.function = 'fMeasurement';
		need.timeout = 3000;
		break;

	case pId('controlAll'): // Control All
		{
			const payloads = [
				new Payload(new Parameter(configCode('gain'))),
				new Payload(new Parameter(configCode('gain_range'))),
				new Payload(new Parameter(configCode('attenuation'))),
				new Payload(new Parameter(configCode('attenuation_range')))				,
				new Payload(new Parameter(configCode('frequency')))				,
				new Payload(new Parameter(configCode('frequency_range')))];
			packet = new Packet(new Header(packetTypeCode('request'), id, groupId('configuration')), payloads, linkAddr);
			need.function = 'fConfig';
			need.timeout = 3000;
		}
		break;

// Network
	case pId('networkAll'): // network All
		packet = new Packet(new Header(packetTypeCode('request'), id, groupId('network')), new Payload(new Parameter(1)), linkAddr);
		need.function = 'fNetwork';
		need.timeout = 3000;
		break;

// Alarm
	case pId('alarmSummary'): 
		packet = new Packet(new Header(packetTypeCode('request'), id, groupId('alarm')), alarmPayload('summary status'), linkAddr);
		need.function = 'fAlarms';
		need.timeout = 1000;
		break;

	case pId('alarmIDs'):
		packet = new Packet(new Header(packetTypeCode('request'), id, groupId('alarm')), alarmPayload('IDs'), linkAddr);
		need.function = 'fAlarms';
		need.timeout = 1000;
		break;

	case pId('alarm'):
		need.function = 'fAlarms';
		need.timeout = 5000;
		break;

	default:
		console.warn('Have to add ' + pIdToString(id));
	}

	need.bytes = packet?.toSend();

	return need;
}
