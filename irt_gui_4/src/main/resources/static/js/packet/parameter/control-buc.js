import * as converter from '../service/converter.js'

const config = {};

// BUC Parameter CODE
config.loSet				 = {}
config.loSet.code			 = 1;
config.loSet.parser		 = bytes=>bytes[0];

config.Mute				 = {}
config.Mute.code			 = 2;
config.Mute.parser			 = converter.parseToBoolean;

config.Gain				 = {}
config.Gain.code			 = 3;
config.Gain.parser			 = bytes=>converter.parseToIrtValue(bytes, 10);

config.gainRange			 = {}
config.gainRange.code		 = 5;
config.gainRange.parser	 = converter.parseToShortArray;

config.Attenuation			 = {}
config.Attenuation.code	 = 4;
config.Attenuation.parser	 = bytes=>converter.parseToIrtValue(bytes, 10);

config.attenuationRange	 = {}
config.attenuationRange.code = 6;
config.attenuationRange.parser	 = converter.parseToShortArray;

config.LO					 = {}
config.LO.code				 = 7;
config.LO.parser			 = converter.parseToLoFrequency;

config.Frequency			 = {}
config.Frequency.code		 = 8;
config.Frequency.parser		 = converter.parseToBigInt;

config.frequencyRange		 = {}
config.frequencyRange.code = 9;
config.frequencyRange.parser = converter.parseToBigIntArray;

config.Redundancy			 = {}
config.Redundancy.code		 = 10;
config.Redundancy.parser	 = converter.parseToBoolean;

config.Mode				 = {}	// Redundancy mode
config.Mode.code			 = 11;
config.Mode.parser			 = data=>data.toString();

config.Name				 = {}	// Redundancy name
config.Name.code			 = 12;
config.Name.parser			 = data=>data.toString();

config.Status				 = {}	// Redundancy status
config.Status.code			 = 15;
config.Status.parser		 = converter.parseToInt;

config.Online				 = {}	// Redundancy online
config.Online.code			 = 14;
config.Online.parser		 = data=>data.toString();

config.spectrumInversion	 = {}
config.spectrumInversion.code = 20;
config.spectrumInversion.parser = data=>data.toString();

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

