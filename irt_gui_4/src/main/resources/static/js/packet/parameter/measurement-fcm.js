import {parseToIrtValue, parseToInt} from '../service/converter.js'
import FcmStatus from './value/fcm-status.js'

const measurement = {};

//	FCM Parameter CODE
measurement.None = {};
measurement.None.code			 = 0;
measurement.parser = data=>data;

measurement['Summary Alarm'] = {};
measurement['Summary Alarm'].code	 = 1;
measurement['Summary Alarm'].parser = data=>data.toString();

measurement.Status = {};
measurement.Status.code			 = 2;
measurement.Status.parser = data=>new FcmStatus(parseToInt(data)).all;

measurement['Input Power'] = {};
measurement['Input Power'].code	 = 4;
measurement['Input Power'].parser = bytes=>parseToIrtValue(bytes, 10) + ' dBm';

measurement['Otput Power'] = {};
measurement['Otput Power'].code		 = 5;
measurement['Otput Power'].parser = bytes=>parseToIrtValue(bytes, 10) + ' dBm';

measurement.Temperature = {};
measurement.Temperature.code	 = 3;
measurement.Temperature.parser = bytes=>parseToInt(bytes)/10 + ' °C';

measurement['5.5V'] = {};
measurement['5.5V'].code			 = 6;
measurement['5.5V'].parser = bytes=>parseToInt(bytes)/1000;

measurement['13.2V'] = {};
measurement['13.2V'].code			 = 7;
measurement['13.2V'].parser = bytes=>parseToInt(bytes)/1000;

measurement['-13.2V'] = {};
measurement['-13.2V'].code		 = 8;
measurement['-13.2V'].parser = bytes=>parseToInt(bytes)/1000;

measurement.Current = {};
measurement.Current.code			 = 9;
measurement.Current.parser = bytes=>parseToInt(bytes)/1000 + ' mA';

measurement['CPU Temperature'] = {};
measurement['CPU Temperature'].code	 = 10;
measurement['CPU Temperature'].parser = bytes=>parseToInt(bytes)/10 + ' °C';

//measurement['Input Power'] = {}
//measurement['Input Power'].code		 = 11;
//measurement['Input Power'].parser = data=>data.toString();

measurement.Attenuation = {};
measurement.Attenuation.code		 = 20;
measurement.Attenuation.parser = data=>data;

measurement.Reference = {};
measurement.Reference.code	 = 21;
measurement.Reference.parser = data=>data

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
