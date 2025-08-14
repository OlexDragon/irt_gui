import {parseToIrtValue, parseToInt} from '../service/converter.js'
import FcmStatus from './value/fcm-status.js'

const measurement = {};

measurement.Source = {};
measurement.Source.code	 = 1;
measurement.Source.parser = source;

measurement['CPU Temperature'] = {};
measurement['CPU Temperature'].code	 = 10;
measurement['CPU Temperature'].parser = bytes=>parseToIrtValue(bytes, 10, ' °C');

measurement['MCU Temperature'] = {}
measurement['MCU Temperature'].code		 = 11;
measurement['MCU Temperature'].parser =bytes=>parseToIrtValue(bytes, 10, ' °C');

measurement['Reference Level'] = {}
measurement['Reference Level'].code		 = 12;
measurement['Reference Level'].parser =bytes=>parseToInt(bytes);

measurement['Reference Status'] = {}
measurement['Reference Status'].code		 = 13;
measurement['Reference Status'].parser =bytes=>new FcmStatus(parseToInt(bytes)).all;;

measurement.Attenuation = {};
measurement.Attenuation.code		 = 20;
measurement.Attenuation.parser = bytes=>parseToInt(bytes)/10 + ' dB';

measurement.all = {}
measurement.all.code				 = 255;

Object.freeze(measurement);
export default measurement;

const names = Object.keys(measurement).reduce((a,key)=>{a[measurement[key].code] = key; return a;}, []);

export function code(name){

	if(typeof name === 'number')
		return name;

	return measurement[name].code;
}

export function name(code){

	return names[code]; 
}

export function toString(value){
	const c = code(value);
	const n = name(c);
	return `measurement: ${n} (${c})`;
}

export function parser(value){
	const n = name(code(value));
	return measurement[n]?.parser;
}

function source(bytes){

	switch(bytes[0]){

	case 1:
		return 'Internal'

	case 2:
		return 'External'

	case 3:
		return 'Autosense'

	default:
		return 'undefined'
	}
}