import Packet from './packet.js'
import Header from './header.js'
import Payload from './payload.js'
import Parameter from './parameter.js'
import {payload as alarmPayload, payloads as alarmPayloads} from './parameter/alarm.js'
import {id as groupId} from './packet-properties/group-id.js'
import {serialPort, baudrate, unitAddress} from '../worker.js'
import {code as configCode} from './parameter/configuration.js'
import {code as packetTypeCode} from './packet-properties/packet-type.js'
import {id as f_packetId, toString as pIdToString} from './packet-properties/packet-id.js'
import { shortToBytesR, longToBytes } from './service/converter.js'

export default class RequestPackt{
	constructor(id, linkAddr, value){
		this.id = packetId(id);
		if(Array.isArray(linkAddr)){
			this.unitAddr = unitAddress;
			value = linkAddr;
		}else
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

const idDeviceInfo = f_packetId('deviceInfo');
const idMeasurementAll = f_packetId('measurementAll');
const idControlAll = f_packetId('controlAll');
const idNetwork = f_packetId('network');
const idNetworkSet = f_packetId('networkSet');
const idAlarmSummary = f_packetId('alarmSummary');
const idAlarmIDs = f_packetId('alarmIDs');
const idAlarmAll = f_packetId('alarmAll');
const idAlarm = f_packetId('alarm');
const idRedundancyAll = f_packetId('redundancyAll');
const idRedundancySetOnline = f_packetId('redundancySetOnline');
const idRedundancySetEnable = f_packetId('redundancySetEnable');
const idRedundancySetDisable = f_packetId('redundancySetDisable');
const idRedundancySetCold = f_packetId('redundancySetCold');
const idRedundancySetHot = f_packetId('redundancySetHot');
const idRedundancySetNameA = f_packetId('redundancySetNameA');
const idRedundancySetNameB = f_packetId('redundancySetNameB');
const idAtenuationSet = f_packetId('atenuationSet');
const idGainSet = f_packetId('gainSet');
const idFrequencySet = f_packetId('frequencySet');
const idMuteControl = f_packetId('mute_control');
function getWhatNeed(id, linkAddr, value){

	const need = {};
	let packet;

	switch(id){

// Device Info
	case idDeviceInfo:	// Device Info
		packet = new Packet(new Header(packetTypeCode('request'), id), undefined, linkAddr);
		need.function = 'fInfo';
		need.timeout = 2000;
		break;

// Measurement
	case idMeasurementAll: // Measurement All
		packet = new Packet(new Header(packetTypeCode('request'), id, groupId('measurement')), undefined, linkAddr);
		need.function = 'fMeasurement';
		need.timeout = 3000;
		break;

// Network
	case idNetwork: // get network
		packet = new Packet(new Header(packetTypeCode('request'), id, groupId('network')), new Payload(new Parameter(1)), linkAddr);
		need.function = 'fNetwork';
		need.timeout = 3000;
		break;

	case idNetworkSet: // set network
		packet = new Packet(new Header(packetTypeCode('command'), id, groupId('network')), new Payload(new Parameter(1), value), linkAddr);
		console.log(packet)
		need.function = 'fNetwork';
		need.timeout = 3000;
		break;

// Alarm
	case idAlarmSummary: 
		packet = new Packet(new Header(packetTypeCode('request'), id, groupId('alarm')), alarmPayload('summary status'), linkAddr);
		need.function = 'fSummaryAlarms';
		need.timeout = 1000;
		break;

	case idAlarmIDs:
		packet = new Packet(new Header(packetTypeCode('request'), id, groupId('alarm')), alarmPayload('IDs'), linkAddr);
		need.function = 'fAlarms';
		need.timeout = 1000;
		break;

		case idAlarmAll:

			var payloads = alarmPayloads(value, true);

		case idAlarm:

			if(!payloads)
				var payloads = alarmPayloads(value);

			packet = new Packet(new Header(packetTypeCode('request'), id, groupId('alarm')), payloads, linkAddr);
			need.function = 'fAlarms';
			need.timeout = 5000;
		break;

	case idRedundancyAll:
		const pls =
		[
			new Payload(configCode('redundancy_enable')),
			new Payload(configCode('redundancy_mode')),
			new Payload(configCode('redundancy_name')),
			new Payload(configCode('redundancy_status'))
		]
		packet = new Packet(new Header(packetTypeCode('request'), id, groupId('configuration')), pls, linkAddr);
		need.function = 'fRedundancy';
		need.timeout = 5000;
		break;

	case idRedundancySetOnline:
		packet = new Packet(new Header(packetTypeCode('command'), id, groupId('configuration')), configCode('redundancy_set_online'), linkAddr);
		need.function = 'fRedundancy';
		need.timeout = 1000;
		break;

	case idRedundancySetEnable:
		packet = new Packet(new Header(packetTypeCode('command'), id, groupId('configuration')), new Payload(new Parameter(configCode('redundancy_enable')),[1]), linkAddr);
		need.function = 'fRedundancy';
		need.timeout = 1000;
		break;

	case idRedundancySetDisable:
		packet = new Packet(new Header(packetTypeCode('command'), id, groupId('configuration')), new Payload(new Parameter(configCode('redundancy_enable')),[0]), linkAddr);
		need.function = 'fRedundancy';
		need.timeout = 1000;
		break;

	case idRedundancySetCold:
		packet = new Packet(new Header(packetTypeCode('command'), id, groupId('configuration')), new Payload(new Parameter(configCode('redundancy_mode')),[0]), linkAddr);
		need.function = 'fRedundancy';
		need.timeout = 1000;
		break;

	case idRedundancySetHot:
		packet = new Packet(new Header(packetTypeCode('command'), id, groupId('configuration')), new Payload(new Parameter(configCode('redundancy_mode')),[1]), linkAddr);
		need.function = 'fRedundancy';
		need.timeout = 1000;
		break;

	case idRedundancySetNameA:
		packet = new Packet(new Header(packetTypeCode('command'), id, groupId('configuration')), new Payload(new Parameter(configCode('redundancy_name')),[1]), linkAddr);
		need.function = 'fRedundancy';
		need.timeout = 1000;
		break;

	case idRedundancySetNameB:
		packet = new Packet(new Header(packetTypeCode('command'), id, groupId('configuration')), new Payload(new Parameter(configCode('redundancy_name')),[2]), linkAddr);
		need.function = 'fRedundancy';
		need.timeout = 1000;
		break;

		case idControlAll: // Control All
			{
				const payloads = [
					new Payload(new Parameter(configCode('gain'))),
					new Payload(new Parameter(configCode('gain_range'))),
					new Payload(new Parameter(configCode('attenuation'))),
					new Payload(new Parameter(configCode('attenuation_range'))),
					new Payload(new Parameter(configCode('frequency'))),
					new Payload(new Parameter(configCode('frequency_range')))					,
					new Payload(new Parameter(configCode('mute')))];
				packet = new Packet(new Header(packetTypeCode('request'), id, groupId('configuration')), payloads, linkAddr);
				need.function = 'fConfig';
				need.timeout = 3000;
			}
			break;

	case idAtenuationSet:
		packet = new Packet(new Header(packetTypeCode('command'), id, groupId('configuration')), new Payload(new Parameter(configCode('attenuation')), shortToBytesR(value)), linkAddr);
		need.function = 'fConfig';
		need.timeout = 1000;
		break;

	case idGainSet:
		packet = new Packet(new Header(packetTypeCode('command'), id, groupId('configuration')), new Payload(new Parameter(configCode('gain')),shortToBytesR(value)), linkAddr);
		need.function = 'fConfig';
		need.timeout = 1000;
		break;

	case idFrequencySet:
		packet = new Packet(new Header(packetTypeCode('command'), id, groupId('configuration')), new Payload(new Parameter(configCode('frequency')),longToBytes(value)), linkAddr);
		need.function = 'fConfig';
		need.timeout = 1000;
		break;

	case idMuteControl:
		packet = new Packet(new Header(packetTypeCode('command'), id, groupId('configuration')), new Payload(new Parameter(configCode('mute')),[value]), linkAddr);
		need.function = 'fConfig';
		need.timeout = 1000;
		break;

	default:
		console.warn('Have to add ' + pIdToString(id));
	}

	need.bytes = packet?.toSend();

	return need;
}
