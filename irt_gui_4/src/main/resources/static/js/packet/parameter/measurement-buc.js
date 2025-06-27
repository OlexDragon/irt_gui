import {parseToIrtValue, parseToInt, parseToStatus} from '../service/converter.js'

const measurement = {};
measurement.lnb = {};

// BUC Parameter CODE
measurement.None = {}
measurement.None.code				 = 0;
measurement.None.parser = data=>data;

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

// LNB Parameter CODE
measurement.lnb[0] = {}
measurement.lnb.none					 = 0;
measurement.lnb[0].description = 'None';
measurement.lnb[0].parser = data=>data.toString();
measurement.lnb[1] = {}
measurement.lnb.inputPower				 = 1;
measurement.lnb[1].description = 'Input Power';
measurement.lnb[1].parser = data=>data.toString();
measurement.lnb[2] = {}
measurement.lnb.outputPower				 = 2;
measurement.lnb[2].description = 'Otput Power';
measurement.lnb[2].parser = data=>data.toString();
measurement.lnb[3] = {}
measurement.lnb.unitTemperature			 = 3;
measurement.lnb[3].description = 'Temperature';
measurement.lnb[3].parser = data=>data.toString();
measurement.lnb[4] = {}
measurement.lnb.status					 = 4;
measurement.lnb[4].description = 'Status';
measurement.lnb[4].parser = data=>data.toString();
measurement.lnb[5] = {}
measurement.lnb.lnbAStatus				 = 5;
measurement.lnb[5].description = 'LNB A';
measurement.lnb[5].parser = data=>data.toString();
measurement.lnb[6] = {}
measurement.lnb.lnbBStatus				 = 6;
measurement.lnb[6].description = 'LNB B';
measurement.lnb[6].parser = data=>data.toString();
measurement.lnb[7] = {}
measurement.lnb.lnbSStatus				 = 7;
measurement.lnb[7].description = 'LNB S';
measurement.lnb[7].parser = data=>data.toString();
measurement.lnb[8] = {}
measurement.lnb.downlinkWaveguideSwitch	 = 8;
measurement.lnb[8].description = 'Switch';
measurement.lnb[8].parser = data=>data.toString();
measurement.lnb[9] = {}
measurement.lnb.downlinkStatus			 = 9;
measurement.lnb[9].description = 'Status';
measurement.lnb[9].parser = data=>data.toString();

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
