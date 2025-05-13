import {parseToIrtValue, parseToString, parseToInt, parseToStatus} from '../service/converter.js'
import {type} from '../service/device-type.js'

const measurwmwnt = {};
measurwmwnt.fcm = {};
measurwmwnt.buc = {};
measurwmwnt.lnb = {};

function chooseGrout(){
	let t
	switch(type){

	case 'LNB':
		t = 'lnb'
		break;

	default:
		t = 'buc';
	}
	return measurwmwnt[t];
}

//	FCM Parameter CODE
measurwmwnt.fcm[0] = {}
measurwmwnt.fcm.none			 = 0;
measurwmwnt.fcm[0].description = 'None';
measurwmwnt.fcm[0].parser = data=>data.toString();
measurwmwnt.fcm[1] = {}
measurwmwnt.fcm.summaryAlarm	 = 1;
measurwmwnt.fcm[1].description = 'Summary Alarm';
measurwmwnt.fcm[1].parser = data=>data.toString();
measurwmwnt.fcm[2] = {}
measurwmwnt.fcm.status			 = 2;
measurwmwnt.fcm[2].description = 'Status';
measurwmwnt.fcm[2].parser = data=>data.toString();
measurwmwnt.fcm[4] = {}
measurwmwnt.fcm.inputPoweFcm	 = 4;
measurwmwnt.fcm[4].description = 'Input Power';
measurwmwnt.fcm[4].parser = parseToString;
measurwmwnt.fcm[5] = {}
measurwmwnt.fcm.outputPower		 = 5;
measurwmwnt.fcm[5].description = 'Otput Power';
measurwmwnt.fcm[5].parser = parseToString;
measurwmwnt.fcm[3] = {}
measurwmwnt.fcm.unitTemperature	 = 3;
measurwmwnt.fcm[3].description = 'Temperature';
measurwmwnt.fcm[3].parser = data=>data.toString();
measurwmwnt.fcm[6] = {}
measurwmwnt.fcm.v5_5			 = 6;
measurwmwnt.fcm[6].description = '5.5V';
measurwmwnt.fcm[6].parser = data=>data.toString();
measurwmwnt.fcm[7] = {}
measurwmwnt.fcm.v13_2			 = 7;
measurwmwnt.fcm[7].description = '13.2V';
measurwmwnt.fcm[7].parser = data=>data.toString();
measurwmwnt.fcm[8] = {}
measurwmwnt.fcm.v13_2_neg		 = 8;
measurwmwnt.fcm[8].description = '-13.2V';
measurwmwnt.fcm[8].parser = data=>data.toString();
measurwmwnt.fcm[9] = {}
measurwmwnt.fcm.current			 = 9;
measurwmwnt.fcm[9].description = 'Current';
measurwmwnt.fcm[9].parser = data=>data.toString();
measurwmwnt.fcm[10] = {}
measurwmwnt.fcm.cpu_temperature	 = 10;
measurwmwnt.fcm[10].description = 'Temperature';
measurwmwnt.fcm[10].parser = data=>data.toString();
measurwmwnt.fcm[11] = {}
measurwmwnt.fcm.inputPower		 = 11;
measurwmwnt.fcm[11].description = 'Input Power';
measurwmwnt.fcm[11].parser = data=>data.toString();
measurwmwnt.fcm[20] = {}
measurwmwnt.fcm.attenuation		 = 20;
measurwmwnt.fcm[20].description = 'Attenuation';
measurwmwnt.fcm[20].parser = data=>data.toString();
measurwmwnt.fcm[21] = {}
measurwmwnt.fcm.referenceSource	 = 21;
measurwmwnt.fcm[21].description = 'Reference';
measurwmwnt.fcm[21].parser = data=>data.toString();

// BUC Parameter CODE
measurwmwnt.buc[0] = {}
measurwmwnt.buc.none					 = 0;
measurwmwnt.buc[0].description = 'None';
measurwmwnt.buc[0].parser = data=>data.toString();
measurwmwnt.buc[1] = {}
measurwmwnt.buc.inputPower				 = 1;
measurwmwnt.buc[1].description = 'Input Power';
measurwmwnt.buc[1].parser = bytes=>parseToIrtValue(bytes, 10) + ' dBm';
measurwmwnt.buc[2] = {}
measurwmwnt.buc.outputPower				 = 2;
measurwmwnt.buc[2].description = 'Otput Power';
measurwmwnt.buc[2].parser =  bytes=>parseToIrtValue(bytes, 10) + ' dBm';
measurwmwnt.buc[3] = {}
measurwmwnt.buc.unitTemperature			 = 3;
measurwmwnt.buc[3].description = 'Temperature';
measurwmwnt.buc[3].parser = bytes=>parseToInt(bytes)/10 + ' °C';
measurwmwnt.buc[4] = {}
measurwmwnt.buc.status					 = 4;
measurwmwnt.buc[4].description = 'Status';
measurwmwnt.buc[4].parser = parseToStatus;
measurwmwnt.buc[5] = {}
measurwmwnt.buc.lnb1Status				 = 5;
measurwmwnt.buc[5].description = 'LNB 1';
measurwmwnt.buc[5].parser = data=>data.toString();
measurwmwnt.buc[6] = {}
measurwmwnt.buc.lnb2Status				 = 6;
measurwmwnt.buc[6].description = 'LNB 2';
measurwmwnt.buc[6].parser = data=>data.toString();
measurwmwnt.buc[7] = {}
measurwmwnt.buc.reflectedPower			 = 7;
measurwmwnt.buc[7].description = 'Reflected Power';
measurwmwnt.buc[7].parser = data=>data.toString();
measurwmwnt.buc[8] = {}
measurwmwnt.buc.downlinkWaveguideSwitch	 = 8;
measurwmwnt.buc[8].description = 'Switch';
measurwmwnt.buc[8].parser = data=>data.toString();
measurwmwnt.buc[9] = {}
measurwmwnt.buc.downlinkStatus			 = 9;
measurwmwnt.buc[9].description = 'Status';
measurwmwnt.buc[9].parser = data=>data.toString();

// LNB Parameter CODE
measurwmwnt.lnb[0] = {}
measurwmwnt.lnb.none					 = 0;
measurwmwnt.lnb[0].description = 'None';
measurwmwnt.lnb[0].parser = data=>data.toString();
measurwmwnt.lnb[1] = {}
measurwmwnt.lnb.inputPower				 = 1;
measurwmwnt.lnb[1].description = 'Input Power';
measurwmwnt.lnb[1].parser = data=>data.toString();
measurwmwnt.lnb[2] = {}
measurwmwnt.lnb.outputPower				 = 2;
measurwmwnt.lnb[2].description = 'Otput Power';
measurwmwnt.lnb[2].parser = data=>data.toString();
measurwmwnt.lnb[3] = {}
measurwmwnt.lnb.unitTemperature			 = 3;
measurwmwnt.lnb[3].description = 'Temperature';
measurwmwnt.lnb[3].parser = data=>data.toString();
measurwmwnt.lnb[4] = {}
measurwmwnt.lnb.status					 = 4;
measurwmwnt.lnb[4].description = 'Status';
measurwmwnt.lnb[4].parser = data=>data.toString();
measurwmwnt.lnb[5] = {}
measurwmwnt.lnb.lnbAStatus				 = 5;
measurwmwnt.lnb[5].description = 'LNB A';
measurwmwnt.lnb[5].parser = data=>data.toString();
measurwmwnt.lnb[6] = {}
measurwmwnt.lnb.lnbBStatus				 = 6;
measurwmwnt.lnb[6].description = 'LNB B';
measurwmwnt.lnb[6].parser = data=>data.toString();
measurwmwnt.lnb[7] = {}
measurwmwnt.lnb.lnbSStatus				 = 7;
measurwmwnt.lnb[7].description = 'LNB S';
measurwmwnt.lnb[7].parser = data=>data.toString();
measurwmwnt.lnb[8] = {}
measurwmwnt.lnb.downlinkWaveguideSwitch	 = 8;
measurwmwnt.lnb[8].description = 'Switch';
measurwmwnt.lnb[8].parser = data=>data.toString();
measurwmwnt.lnb[9] = {}
measurwmwnt.lnb.downlinkStatus			 = 9;
measurwmwnt.lnb[9].description = 'Status';
measurwmwnt.lnb[9].parser = data=>data.toString();

export function code(name){
	if(typeof name === 'number')
		return name;
	const group = chooseGrout();
	return group[name];
}

export function name(code){
	const group = chooseGrout();
	const keys = Object.keys(group);

	for(const key of keys)
		if(deviceInfo[key] == code)
			return key;
}

export function description(value){
	const c = code(value)
	return chooseGrout()[c].description;
}

export function toString(value){
	const c = code(value)
	const name = name(value)
	return `measurement: ${name} (${c})`;
}

export function parser(value){
	const c = code(value)
	return chooseGrout()[c].parser;
}
