
const packetType = {};
packetType.spontaneous		= 0x0;			/* Spontaneous message, generated by device. */
packetType.response			= 0x1;			/* Response, generated as response to command or status request. */
packetType.request			= 0x2;			/* Status request. */
packetType.command			= 0x3;			/* Command. */
packetType.error			= 0xFE;			/* Error Packet. */
packetType.acknowledgement	= 0xFF;			/* Layer 2 acknowledgement. */

export function code(name){
	return packetType[name];
}

export function name(code){
	const keys = Object.keys(packetType);
	for(const key of keys){
		if(packetType[key] == code)
			return key;
			}
}

const PACKET_TYPE = {};
PACKET_TYPE[packetType.spontaneous]		 = 'spontaneous';		/* Spontaneous message, generated by device. */
PACKET_TYPE[packetType.response]		 = 'response';			/* Response, generated as response to command or status request. */
PACKET_TYPE[packetType.request]			 = 'request';			/* Status request. */
PACKET_TYPE[packetType.command]			 = 'command';			/* Command. */
PACKET_TYPE[packetType.error]			 = 'error';				/* Error Packet. */
PACKET_TYPE[packetType.acknowledgement]	 = 'acknowledgement';	/* Layer 2 acknowledgement. */

export function description(value){

	if(typeof value === 'number')
		return PACKET_TYPE[value];

	else{

		const code = packetTypeCode(value);
		return PACKET_TYPE[code];
	}
}

export function toString(value){

	if(typeof value === 'number'){

		const name = packetTypeName(value);
		return `packetType: ${name} (${value})`;

	}else{

		const code = packetTypeCode(value);
		return `packetType: ${value} (${code})`;
	}
}