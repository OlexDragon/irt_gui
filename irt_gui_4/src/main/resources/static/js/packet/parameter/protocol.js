import {parseToInt} from '../service/converter.js'

const protocol = Object.freeze([, , , 'address', 'baudrate',	 'retransmit', 'tranceiver_mode']);
const parsers =	 Object.freeze([, , , b=>b,		  parseToInt,  b=>b,			b=>b]);

export function code(name){
	if(typeof name === 'number')
		if(name>=0 && name<protocol.length)
			return name;
		else
			throw new Error(name + " - Unknown parameter code.");

	return protocol.indexOf(name);
}

export function name(code){

	if(typeof code === 'string')
		if(protocol.includes(code))
			return code;
		else
			throw new Error( code + " - Unknown parameter name.");

	if(code>=0 && code<protocol.length)
		return protocol[code];
	else
		throw new Error(name + " - Unknown parameter code.");
}

export function toString(value){
	const c = code(value)
	const n = name(value)
	return `protocol: ${n} (${c})`;
}

export function parser(value){
	const c = code(value)
	return parsers[c];
}
