import {status as alarmStatus} from '../parameter/value/alarm-status.js'

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

export function longToBytes(val){
	return numberToBytes(val, 8);
}

export function numberToBytes(val, minBytes){
	let hex = val.toString(16);
	const hexArray = [];
	while(hex.length){
		const start = hex.length-2;
		if(start>=0){
			const substring = hex.substring(start, start+2);
			hexArray.push(substring);
			hex = hex.substring(0, start);
		}else{
			hexArray.push(hex);
			hex = '';
		}
	}
	const bytes = [];
	hexArray.forEach(h=>bytes.unshift(parseInt(h, 16)));
	if(minBytes)
		while(minBytes>bytes.length)
			bytes.unshift(0);
    return bytes;
}

export function parseToString(bytes){
	if(!bytes)
		return '';

	const last = bytes.length - 1;
	if(bytes[last]==0)
		bytes.splice(last, 1);
		
	return String.fromCharCode.apply(String, bytes);
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
	for(let i=0; b.length && i<3; i++){
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

export function parseToShortArray(bytes){
	return parseToArray(bytes, 2);
}

function parseToArray(bytes, size){
	const ints = [];
	const b = [...bytes];
	for(let i=0; b.length; i++){
		const fourBytes = b.splice(0, size);
		ints.push(parseToInt(fourBytes));
	}
	return ints;
}
export function parseToBoolean(bytes){
	if(!bytes?.length)
		return '';
	return bytes[0]>0;
}

const prefixes = ['UNDEFINED', '', '<', '>']
export function parseToIrtValue(bytes, divider){

	if(!bytes?.length)
		return 'N/A';

	if(bytes.length>2){
		const index = bytes.splice(0,1)&3;
		const prefix = prefixes[index];
		const value = divider ? parseToInt(bytes)/divider : parseToInt(bytes);
		return prefix + value;
	}else
		return divider ? parseToInt(bytes)/divider : parseToInt(bytes);
}

const statusBits = {};
statusBits.buc = {};
statusBits.buc.mute = {};
statusBits.buc.mute.value = 1;
statusBits.buc.mute.bitmask = 1;
statusBits.buc.pll_unknown = {};
statusBits.buc.pll_unknown.value = 0;
statusBits.buc.pll_unknown.bitmask = 6;
statusBits.buc.locked = {};
statusBits.buc.locked.value = 2;
statusBits.buc.locked.bitmask = 6;
statusBits.buc.unlocked = {};
statusBits.buc.unlocked.value = 4;
statusBits.buc.unlocked.bitmask = 6;
statusBits.buc.internal = {};
statusBits.buc.internal.value = 16;
statusBits.buc.internal.bitmask = 16;


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
	value.id = parseToIrtValue(bytes.splice(0,2));
	value.string = parseToString(bytes);
	return value;
}

