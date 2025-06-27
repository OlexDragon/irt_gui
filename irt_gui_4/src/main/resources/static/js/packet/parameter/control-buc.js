import * as converter from '../service/converter.js'

const control = {};

// BUC Parameter CODE
control.loSet				 = {}
control.loSet.code			 = 1;
control.loSet.parser		 = bytes=>bytes[0];

control.Mute				 = {}
control.Mute.code			 = 2;
control.Mute.parser			 = converter.parseToBoolean;

control.Gain				 = {}
control.Gain.code			 = 3;
control.Gain.parser			 = bytes=>converter.parseToIrtValue(bytes, 1);

control.gainRange			 = {}
control.gainRange.code		 = 5;
control.gainRange.parser	 = converter.parseToShortArray;

control.Attenuation			 = {}
control.Attenuation.code	 = 4;
control.Attenuation.parser	 = bytes=>converter.parseToIrtValue(bytes, 1);

control.attenuationRange	 = {}
control.attenuationRange.code = 6;
control.attenuationRange.parser	 = converter.parseToShortArray;

control.LO					 = {}
control.LO.code				 = 7;
control.LO.parser			 = converter.parseToLoFrequency;

control.Frequency			 = {}
control.Frequency.code		 = 8;
control.Frequency.parser	 = converter.parseToBigInt;

control.frequencyRange		 = {}
control.frequencyRange.code = 9;
control.frequencyRange.parser = converter.parseToBigIntArray;

control.Redundancy			 = {}
control.Redundancy.code		 = 10;
control.Redundancy.parser	 = converter.parseToBoolean;

control.Mode				 = {}	// Redundancy mode
control.Mode.code			 = 11;
control.Mode.parser			 = data=>data.toString();

control.Name				 = {}	// Redundancy name
control.Name.code			 = 12;
control.Name.parser			 = data=>data.toString();

control.Status				 = {}	// Redundancy status
control.Status.code			 = 15;
control.Status.parser		 = converter.parseToIrtValue;

control.Online				 = {}	// Redundancy online
control.Online.code			 = 14;
control.Online.parser		 = data=>data.toString();

control.spectrumInversion	 = {}
control.spectrumInversion.code = 20;
control.spectrumInversion.parser = data=>data.toString();

Object.freeze(control);
export default control;

const controlNames = Object.keys(control).reduce((a,k)=>{
		a[control[k].code] = k;
		return a;
	}, []
);

export function code(value){
	if(typeof value === 'number')
		return value;
	return control[value];
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
	return control[n].parser;
}

