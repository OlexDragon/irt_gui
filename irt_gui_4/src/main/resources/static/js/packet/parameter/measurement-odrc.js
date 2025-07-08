import {parseToIrtValue, parseToInt, parseToStatus} from '../service/converter.js'

const measurement = {};

// BUC Parameter CODE

measurement['Input Power'] = {}
measurement['Input Power'].code		 = 1;
measurement['Input Power'].parser	 = bytes=>parseToIrtValue(bytes, 10) + ' dBm';

measurement['Otput Power'] = {}
measurement['Otput Power'].code		 = 2;
measurement['Otput Power'].parser	 = bytes=>parseToIrtValue(bytes, 10) + ' dBm';

measurement.Temperature = {}
measurement.Temperature.code		 = 3;
measurement.Temperature.parser		 = bytes=>parseToInt(bytes)/10 + ' °C';

measurement.Status = {}
measurement.Status.code				 = 4;
measurement.Status.parser			 = parseToStatus;

measurement['Reflected Power'] = {} // Status
measurement['Reflected Power'].code	 = 5;
measurement['Reflected Power'].parser = bytes=>parseToIrtValue(bytes, 10) + ' dBm';

measurement['LNB 2'] = {} // Status
measurement['LNB 2'].code			 = 6;
measurement['LNB 2'].parser			 = data=>data;

//measurement['Reflected Power'] = {}
//measurement['Reflected Power'].code	 = 7;
//measurement['Reflected Power'].parser = data=>data;

measurement.Switch = {}
measurement.Switch.code				 = 8;
measurement.Switch.parser			 = data=>data;

measurement.Downlink = {} // Status
measurement.Downlink.code			 = 9;
measurement.Downlink.parser			 = data=>data;

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
	return `measurement: ${name(c)} (${c})`;
}

export function parser(value){
	return measurement[name(code(value))]?.parser;
}
