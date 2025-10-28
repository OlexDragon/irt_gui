import {parseToInt, parseToIntArray} from '../service/converter.js'

const config = [];
config[14] = 'Switchover';  /* Switch the unit to be online */
config[15] = 'Status';		/* config Unit status */
config[124] = 'Band Select';/* Frequency range select  */
config[125] = 'Mode Select';/* Mode select. Not saving parameters, that is AUTO on startup. Possible options: AUTO/MANUAL/UNKNOWN */
config[131] = 'LNB1 Band Select';/* LNB1 Frequency range select */
config[132] = 'LNB2 Band Select';/* LNB2 Frequency range select */
Object.freeze(config);

export function code(name){

	if(typeof name === 'number')
		if(name>=0 && name<config.length)
			return  name;
		else
			throw new Error('Wrong index - ' + name);

	const index = config.indexOf(name);
	if(index<0)
		throw new Error('Wrong mane - ' + name);

	return index
}

export function name(code){

	if(typeof code === 'string')
		if(config.includes(code))
			return code;
		else
			throw new Error('Wrong mane - ' + code);

	if(code<0 || code>=config.length)
		throw new Error('Wrong index - ' + name);
	else
		return config[code];
}

export function parser(value){
	const n = name(value);
	switch(n){

	case 'Mode Select':
			return bytes => {
				const modes = [,'AUTO', 'MANUAL'];
				const b = bytes[0];
				return {key: b, name: modes[b]};
			};

	default:
		console.warn('No parser for irpc config parameter ' + value);
		return b=>b;
	}
}

export function toString(value){
	const c = code(value)
	const n = name(value)
	return `irpc: ${n} (${c})`;
}