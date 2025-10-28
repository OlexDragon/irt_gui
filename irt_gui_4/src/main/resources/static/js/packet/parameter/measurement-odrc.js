import {parseToIrtValue} from '../service/converter.js'

const lnbStatus = ['Not Ready', 'Ready'];
const measurement = {};

// BUC Parameter CODE

measurement.Status = {}
measurement.Status.code		 = 1;
measurement.Status.parser	 = 'do not show';

measurement.Temperature = {}
measurement.Temperature.code		 = 3;
measurement.Temperature.parser		 = bytes=>parseToIrtValue(bytes, 10, ' °C');

measurement['WGS Status'] = {}
measurement['WGS Status'].code		 = 4;
measurement['WGS Status'].parser	 = parseToStatus;

measurement['LNB 1'] = {} // Status
measurement['LNB 1'].code			 = 5;
measurement['LNB 1'].parser			 = bytes=>lnbStatus[bytes[0]];

measurement['LNB 2'] = {} // Status
measurement['LNB 2'].code			 = 6;
measurement['LNB 2'].parser			 = bytes=>lnbStatus[bytes[0]];

measurement['LNB 3'] = {}
measurement['LNB 3'].code			 = 7;
measurement['LNB 3'].parser			 = bytes=>lnbStatus[bytes[0]];

measurement.all = {}
measurement.all.code				 = 255;

Object.freeze(measurement);
export default measurement;

const names = Object.keys(measurement).reduce(toNames, []);
function toNames(a, key){
	a[measurement[key].code] = key;
	return a;
}

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

const statuses = ['UNKNOWN','LNB 1','LNB 2',,,,,,,,,'DEFAULT','PROTECTION A','PROTECTION B'];
function parseToStatus(bytes){
	return statuses[bytes[0]];
}