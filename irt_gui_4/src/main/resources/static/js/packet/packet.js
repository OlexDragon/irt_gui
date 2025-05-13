import Header from './header.js'
import Payload from './payload.js'
import LinkHeader from './Link-header.js'
import Parameter from './parameter.js'
import {code, description} from './packet-properties/packet-type.js'
import {checksumToBytes} from './service/checksum.js'

// Default InfoPacket
export default class Packet{
	// Default constuctor converter INFO Packet
	constructor(header, payloads, unitAddr){
		// From bytes
		if(Array.isArray(header)){
			if(header[0]==FLAG_SEQUENCE)
				header.splice(0,1)
			const flagseqIndex = header.indexOf(FLAG_SEQUENCE);
			const bytes = byteStuffing(flagseqIndex<0 ? header : header.splice(0, flagseqIndex));
//			console.log(bytes);
			const packetArray = bytes.splice(0,bytes.length-2);
			const chcksm = checksumToBytes(packetArray);
			if(chcksm[0]==(bytes[0]&0xff) && chcksm[1]==(bytes[1]&0xff)){
				if(payloads){	// Has Link Header
					const linkHeaderArray = packetArray.splice(0, LINK_HEADER_SIZE);
					this.linkHeader = new LinkHeader(linkHeaderArray);
				}
				const headerArray = packetArray.length==ACKNOWLEDGEMENT_HEADER_SIZE ? packetArray.splice(0) : packetArray.splice(0, HEADER_SIZE);
				this.header = new Header(headerArray);
				if(packetArray.length>=PARAMETER_SIZE)
					this.payloads = this.parsePayloads(packetArray);
				else if(packetArray.length)
					console.error('Byte parsing error.');
			}else{
				this.header = new Header(code('error'), packetArray[2] * 256 + packetArray[1], 'The packet checksum is incorrect');
				console.warn('The packet checksum is incorrect; received: ' + header[header.length-2] +',' + header[header.length-1] + '; calculated: ' + chcksm + '; bytes: ' + header);
			}
//			console.log(this);
			return;
		}
		this.header = (header == undefined ? new Header() : header);
		if(this.header.type!=code('acknowledgement'))
			this.payloads = payloads == undefined ? [new Payload()] : Array.isArray(payloads) ? payloads : [payloads];

		if(unitAddr!=undefined)
			this.linkHeader = new LinkHeader(unitAddr);

//		console.log(this);
	}
	getAcknowledgement(){
		const header = new Header(code('acknowledgement'), this.header.packetId);
		return new Packet(header, undefined, this.linkHeader);
	}
	toBytesAcknowledgement(){
		return this.header.toBytesAcknowledgement();
	}
	parsePayloads(bytes){
		if(!bytes)
			return [];

		const pl = []
		while(bytes.length>0){
			const p = bytes.splice(0, PARAMETER_SIZE);
			const parameter = new Parameter(p);
			const d = bytes.splice(0, parameter.size);
			const payload = new Payload(parameter,d);
			pl.push(payload);
		}
		return pl;
	}
	toBytes(){
		const linkHeaderrBytes = this.linkHeader?.toBytes();
		const headerBytes = this.header.toBytes();
		const payloadBytes = this.payloadsToBytes();
		return linkHeaderrBytes ? linkHeaderrBytes.concat(headerBytes).concat(payloadBytes) : headerBytes.concat(payloadBytes);
	}
	toSend(){
		return packetToSend(this);
	}
	payloadsToBytes(){

		let bytes = [];
		if(!this.payloads)
			return bytes;

		this.payloads.forEach(pl=>bytes=bytes.concat(pl.toBytes()));

		return bytes;
	}
	toString(){
		const linkHeader = this.linkHeader ? 'linkHeader: ' + this.linkHeader.toString() + ', ' : '';
		return linkHeader + this.header.toString() + (this.payloads ? ', ' + this.payloads.map(pl=>pl.toString(this.header.groupId)) : '');
	}
	getData(parameterCode){
		if(parameterCode)
			return this.payloads?.filter(pl=>(pl.parameter.code&0xff)==parameterCode).map(pl=>pl.getData(this.header.groupId));
		if(this.payloads?.length)
			return this.payloads[0].getData(this.header.groupId);
	}
}

const FLAG_SEQUENCE	= 0x7E;
const CONTROL_ESCAPE = 0x7D;
const LINK_HEADER_SIZE = 4;
const HEADER_SIZE = 7;
const PARAMETER_SIZE= 3;
const PAYLOAD_MIN_SIZE = PARAMETER_SIZE;
const ACKNOWLEDGEMENT_SIZE = 5; // 3 bytes - packet type and packet ID plus 2 byte checksum
const ACKNOWLEDGEMENT_HEADER_SIZE = 4; // 3 bytes - packet type and packet ID

function byteStuffing(bytes){

	if(!byteStuffing)
		return byteStuffing;

	let result = [];

	for(let i=0; i<bytes.length; i++){
		if(bytes[i]==CONTROL_ESCAPE){
			result.push(bytes[++i]^0x20);
		}else
			result.push(bytes[i]);
	}
	return result;
}
function controlEscape(bytes){
	let result = [];
	for(let i=0; i<bytes.length; i++){
		if(bytes[i]==FLAG_SEQUENCE || bytes[i]==CONTROL_ESCAPE){
			result.push(CONTROL_ESCAPE);
			result.push(bytes[i]^0x20);
		}else
			result.push(bytes[i]);
	}
	return result;
}

function packetToSend(packet){
	const bytes = packet.toBytes();
	const checksum = checksumToBytes(bytes);
	return [FLAG_SEQUENCE].concat(controlEscape(bytes.concat(checksum))).concat(FLAG_SEQUENCE);
}
