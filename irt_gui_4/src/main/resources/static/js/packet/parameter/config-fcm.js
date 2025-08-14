import * as converter from '../service/converter.js'

const config = {};

//	FCM Parameter CODE
config.Gain					 = {}
config.Gain.code			 = 1;
config.Gain.parser			 = bytes=>converter.parseToIrtValue(bytes, 10);

config.Attenuation			 = {}
config.Attenuation.code		 = 2;
config.Attenuation.parser	 = bytes=>converter.parseToIrtValue(bytes, 10);

config.loSet				 = {}
config.loSet.code			 = NaN;
config.loSet.parser			 = bytes=>bytes[0];

config.LO					 = {}
config.LO.code				 = NaN;
config.LO.parser			 = converter.parseToLoFrequency;

config.Frequency			 = {}
config.Frequency.code		 = 3;
config.Frequency.parser		 = converter.parseToBigInt;

config.frequencyRange		 = {}
config.frequencyRange.code	 = 4;
config.frequencyRange.parser = converter.parseToBigIntArray;

config.gainRange			 = {}
config.gainRange.code		 = 5;
config.gainRange.parser		 = converter.parseToShortArray;

config.attenuationRange		 = {}
config.attenuationRange.code = 6;
config.attenuationRange.parser = converter.parseToShortArray;

config.Mute					 = {}
config.Mute.code			 = 7;
config.Mute.parser			 = converter.parseToBoolean;

config.Power				 = {}
config.Power.code			 = 8;
config.Power.parser			 = data=>data.toString();

config.Flags				 = {}
config.Flags.code			 = 9;
config.Flags.parser			 = data=>data.toString();

config['Gain Offset']		 = {}
config['Gain Offset'].code	 = 10;
config['Gain Offset'].parser = data=>data.toString();

config['Gain Offset Range']		 = {}
config['Gain Offset Range'].code	 = 11;
config['Gain Offset Range'].parser = data=>data.toString();

config.ALC					 = {}
config.ALC.code				 = 12;
config.ALC.parser			 = data=>data.toString();

config['ALC Level']			 = {}
config['ALC Level'].code	 = 13;
config['ALC Level'].parser	 = data=>data.toString();

config['ALC Range']			 = {}
config['ALC Range'].code	 = 14;
config['ALC Range'].parser	 = data=>data.toString();

config['ALC Protection']		 = {}
config['ALC Protection'].code	 = 15;
config['ALC Protection'].parser	 = data=>data.toString();

config['ALC Protection Threshold']	 = {}
config['ALC Protection Threshold'].code	 = 16;
config['ALC Protection Threshold'].parser = data=>data.toString();

config['ALC Protection Range']		 = {}
config['ALC Protection Range'].code	 = 17;
config['ALC Protection Range'].parser = data=>data.toString();

config['Ref. Source']		 = {}
config['Ref. Source'].code	 = 18;
config['Ref. Source'].parser = data=>data.toString();

config.capability		 = {}
config.capability.code	 = 19;
config.capability.parser = data=>data.toString();

config['Spectrum Inversion']			 = {}
config['Spectrum Inversion'].code	 = 20;
config['Spectrum Inversion'].parser	 = data=>data.toString();

config['LNB Reference']		 = {}
config['LNB Reference'].code = 21;
config['LNB Reference'].parser = data=>data.toString();

config.loSet				 = {}
config.loSet.code			 = NaN;
config.loSet.parser			 = bytes=>bytes[0];

config.LO					 = {}
config.LO.code				 = NaN;
config.LO.parser			 = converter.parseToLoFrequency;

config.all						 = {}
config.all.code					 = 255;

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

