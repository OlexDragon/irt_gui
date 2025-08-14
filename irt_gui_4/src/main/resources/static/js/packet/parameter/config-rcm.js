import * as converter from '../service/converter.js'

const config = {};

config.Source				 = {}
config.Source.code			 = 1;
config.Source.parser		 = bytes=>bytes[0];	// UNDEFINED = 0, INTERNAL = 1, EXTERNAL  = 2, AUTOSENSE = 3

config['DAC Range']			 = {}
config['DAC Range'].code	 = 2;
config['DAC Range'].parser	 = converter.parseToIntArray;

config.DAC					 = {}
config.DAC.code				 = 3;
config.DAC.parser			 = bytes=>converter.parseToInt(bytes);

config['DAC Step Range']		 = {}
config['DAC Step Range'].code	 = 4;
config['DAC Step Range'].parser	 = bytes=>converter.parseToIrtValue(bytes, 10);

config['DAC Step']			 = {}
config['DAC Step'].code		 = 5;
config['DAC Step'].parser		 = converter.parseToInt;

config.Increment		 = {}
config.Increment.code	 = 6;
//config.Increment.parser	 = converter.parseToInt;

config.Decrement			 = {}
config.Decrement.code		 = 7;
//config.Decrement.parser		 = converter.parseToInt;

config['Factory Reset']			 = {}
config['Factory Reset'].code	 = 8;
//config['Factory Reset'].parser		 = converter.parseToBigInt;

config['Factory Value']			 = {}
config['Factory Value'].code	 = 9;
//config['Factory Value'].parser	 = converter.parseToInt;

config.Capabilities	 = {}
config.Capabilities.code = 19;
config.Capabilities.parser = data=>data.toString();

Object.freeze(config);
export default config;

const controlNames = Object.keys(config).reduce((a,k)=>{
		a[config[k].code] = k;
		return a;
	}, []
);

export function code(value){
	if(typeof value === 'number')
		return value;
	return config[value];
}

export function name(value){
	return controlNames[value];
}

export function toString(value){
	const c = code(value)
	const n = name(c)
	return `control: ${n} (${c})`;
}

export function parser(value){
	const n = name(value)
	return config[n].parser;
}

