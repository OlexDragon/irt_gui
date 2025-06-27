import packetType, { name } from './packet-properties/packet-type.js'
import { id, toString } from './packet-properties/group-id.js'
import { PACKET_ERROR } from './error.js'
import { shortToBytes } from './service/converter.js'
import { toString as idToString } from './packet-properties/packet-id.js'

export default class Header{
	constructor(type, packetId, groupId, error){
		// From bytes
		if(Array.isArray(type)){
			const bytes = type;
			this.type 		= bytes[0]&0xff;						// byte	type;		0
			this.packetId 	= (bytes[2]&0xff) * 256 + (bytes[1]&0xff);		// short packetId;	1,2 

			if(bytes.length>=HEADER_SIZE && this.type != packetType.acknowledgement){
				this.groupId 	= bytes[3]&0xff;											// byte groupId;	3
				this.reserved	= 0;														// short reserved;	4,5
				this.error		= (packetId == undefined ? bytes[6]&0xff : packetId);		// byte errorCode;	6
			}
			return;	
		}

		this.type 		= (type == undefined ? packetType.request : type);								// byte	type;		0
		this.packetId 	= (packetId == undefined ? Math.floor(Math.random() * 32767 ) : packetId);		// short packetId;	1,2

		if(this.type==packetType.acknowledgement)
			return;

		this.groupId 	= (groupId == undefined ? id('deviceInfo') : (typeof groupId == "number") ? groupId : undefined);	// byte groupId;	3; 
		this.reserved	= 0;															// short reserved;	4,5
		this.error		= ((typeof groupId === 'string') ? groupId : error === undefined ? 0 : error);						// byte errorCode;	6
	}
	toBytes(){
		const id = shortToBytes(this.packetId);
		if(this.type == packetType.acknowledgement)
			return [this.type, id[0], id[1]];
		const reserved = shortToBytes(this.reserved);
		return [this.type, id[0], id[1], this.groupId, reserved[0], reserved[1], this.error];
	}
	toBytesAcknowledgement(){
		const id = shortToBytes(this.packetId);
		return [this.type, id[0], id[1]];
	}
	toString(){
		if(this.type == packetType.acknowledgement)
			return 'type = ' + name(this.type) + ', ID = ' + this.packetId;

		const grId = toString(this.groupId);
		return 'type = ' + name(this.type) + ', ID = ' + idToString(this.packetId) + ', groupId = ' + (grId ? grId : this.groupId) + ', error = ' + ((typeof this.error !== "number") ? this.error : PACKET_ERROR[this.error]);
	}
}

const HEADER_SIZE = 7;