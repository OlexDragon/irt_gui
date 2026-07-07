import {status as alarmStatus} from '../parameter/value/alarm-status.js'
import IrtValue from '../parameter/value/irt-value.js'

export function shortToBytes(val, reverse){

	if(reverse)
		return val ? numberToBytes(val, 2) : [0, 0];

		const bytes = [0,0];
	for ( let index = 0; index < bytes.length; index++ ) {
        let byte = val & 0xff;
        bytes[index] = byte;
        val = (val - byte) / 256 ;
    }
    return bytes;
}

export function shortToBytesR(val){
    return shortToBytes(val, true);
}

export function intToBytes(val){
	return numberToBytes(val, 4);
}

export function intArrayToBytes(...val){
	const result = []
	val.forEach(v=>result.push(intToBytes(v)));
	return result.flat();
}

export function longToBytes(val){
	return numberToBytes(val, 8);
}

export function numberToBytes(value, minBytes = 0) {
    const bytes = [];

    do {
        bytes.unshift(value & 0xff);
        value >>>= 8;
    } while (value);

    while (bytes.length < minBytes)
        bytes.unshift(0);

    return bytes;
}

export function parseToString(bytes){
	if(!Array.isArray(bytes))
		return null;

	const b = [...bytes];

	if (b.at(-1) === 0)
	    b.pop();
		
	return String.fromCharCode.apply(String, b);
}

export function parseToInt(bytes, unsigned) {

	let index = bytes.length-1;
	let intValue = !unsigned && index==0 ? bytes[index]<<24>>24 : bytes[index]&0xff;

	for(let i=1; index>0 && i<bytes.length;i++){
		const shift = i*8;
		let v

		if(unsigned)
			v = bytes[--index]&0xff;
		else if(index==1)
			v = bytes[--index]<<24>>24;
		else
			v = bytes[--index];

		intValue |= v<<shift;
	}
	return intValue;
};

function byteToHex(b){
	return (b + 0x100).toString(16).substr(-2).toUpperCase();
}
function bytesToHexString(bytes){
	return bytes.map(byteToHex).join('');
}

export function parseToBigInt(bytes){
	return BigInt('0x' + bytesToHexString(bytes));
}

export function parseToBigIntArray(bytes){
	const ints = [];
	const b = [...bytes];
	//	for(let i=0; b.length && i<3; i++){
	for(let i=0; b.length; i++){
		const fourBigInt = b.splice(0, 8);
		ints.push(parseToBigInt(fourBigInt));
	}
return ints;
}

export function parseToIntUnsigned(bytes) {
	return parseToInt(bytes, true);
};

export function parseToIntArray(bytes){
	return parseToArray(bytes, 4);
}

export function parseToIntSequence(bytes){
	return parseToArray(bytes, 4).join('.');
}

export function parseToShortArray(bytes){
	return parseToArray(bytes, 2);
}

function parseToArray(bytes, size){
	const ints = [];
	const b = [...bytes];
	while(b.length){
		const fourBytes = b.splice(0, size);
		ints.push(parseToInt(fourBytes));
	}
	return ints;
}
export function parseToBoolean(bytes){
	if(!bytes?.length)
		return '';
	return bytes[bytes.length-1]>0;
}

const prefixes = [, '', '<', '>', 'N/A']
export function parseToIrtValue(bytes, divider, postfix){

	if(!bytes?.length)
		return new IrtValue();

	let prefix;
	if(bytes.length===3){
		const index = bytes.splice(0,1)[0] & 7;
		if(index===0)
			return new IrtValue('UNDEFINED');
		prefix = prefixes[index];
		if(index===4)
			return new IrtValue(prefix);
	}
	return new IrtValue(parseToInt(bytes), prefix, postfix, divider);
}

export function parseToLoFrequency(bytes){
	const b = [...bytes]
	const lo = [];
	while(b.length){
		const index = b.splice(0,1)[0]&0xff;
		let value
		if(b.length)
			value = (parseToBigInt(b.splice(0,8))/1000000n) + ' MHz';
		lo[index] = value;
	}
	return lo;
}

export function parseToFreqyency(bytes){
	const b = [...bytes]
	return parseToBigInt(b)/1000000n;
}

const
	MINUTE = 60,
	HOUR	= 60*MINUTE,
	DAY		= 24*HOUR;
	
export function parseToTimeStr(bytes){
	const time = parseToInt(bytes);
	const days = Math.floor(time / DAY);
	const hours = Math.floor(time%DAY / HOUR);
	const minutes = Math.floor(time%HOUR / MINUTE);
	const sec = time%MINUTE;
	return [days, hours, minutes, sec].map(t=>t.toString().padStart(2,'0')).join(':');
}

const statusBits = {
    buc: {
        mute: {
            value: 1,
            bitmask: 1
        },
        locked: {
            value: 2,
            bitmask: 6
        },
        unlocked: {
            value: 4,
            bitmask: 6
        },
        internal: {
            value: 16,
            bitmask: 16
        }
    }
};

export function parseToStatus(value, type){
	let status;

	switch(type){
	default:
		status = statusBits.buc;
	}

	const result = []
	const keys =Object.keys(status);
	const v = parseToInt(value);

	for(let key of keys)
		if((v&status[key].bitmask)==status[key].value)
			result.push(key);

	return result;
}

export function parseToAlarmStatus(bytes){
	return alarmStatus(bytes);
}
export function parseToAlarmString(bytes){
	const value = {};
	value.id = parseToInt(bytes.splice(0,2));
	value.string = parseToString(bytes);
	return value;
}
const capabilities = [undefined, 'Internal', 'External', 'Autosense'];
export function parseToCapabilities(bytes){
	const intVal = parseToInt(bytes);
	const result = [];
	for(let i=1; i<capabilities.length; i++){
		const bitmask = 1<<i;
		if(intVal & bitmask)
			result.push({[capabilities[i]]:i});
	}
	return result;
}

