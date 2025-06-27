import {parseToIrtValue, parseToShortArray, parseToBigInt, parseToBigIntArray, parseToBoolean} from '../service/converter.js'
import f_deviceType from '../service/device-type.js'

const control = {};
control.fcm = {};
control.buc = {};
control.lnb = {};

function chooseGroup(){
	let t
	switch(f_deviceType()){

	case 'LNB':
		t = 'lnb'
		break;

	default:
		t = 'buc';
	}
	return control[t];
}

//	FCM Parameter CODE
control.fcm[0] = {}
control.fcm.none			 = 0;
control.fcm[0].description = 'None';
control.fcm[0].parser = data=>data.toString();
control.fcm[1] = {}
control.fcm.gain			 = 1;
control.fcm[1].description = 'Gain';
control.fcm[1].parser = bytes=>parseToIrtValue(bytes, 10) + ' dB';
control.fcm[2] = {}
control.fcm.attenuation		 = 2;
control.fcm[2].description = 'Attenuation';
control.fcm[2].parser = bytes=>parseToIrtValue(bytes, 10) + ' dB';
control.fcm[3] = {}
control.fcm.frequency		 = 3;
control.fcm[3].description = 'Frequency';
control.fcm[3].parser = data=>data.toString();
control.fcm[4] = {}
control.fcm.frequency_range	 = 4;
control.fcm[4].description = 'Frequency Range';
control.fcm[4].parser = data=>data.toString();
control.fcm[6] = {}
control.fcm.attenuation_range = 6;
control.fcm[6].description = 'Attenuation Range';
control.fcm[6].parser = data=>data.toString();
control.fcm[7] = {}
control.fcm.mute_control	 = 7;
control.fcm[7].description = 'Mute Control';
control.fcm[7].parser = data=>data.toString();
control.fcm[8] = {}
control.fcm.lnb_power		 = 8;
control.fcm[8].description = 'Power';
control.fcm[8].parser = data=>data.toString();
control.fcm[9] = {}
control.fcm.flags			 = 9;
control.fcm[9].description = 'Flags';
control.fcm[9].parser = data=>data.toString();
control.fcm[10] = {}
control.fcm.gain_offset		 = 10;
control.fcm[10].description = 'Gain Offset';
control.fcm[10].parser = data=>data.toString();
control.fcm[12] = {}
control.fcm.inputPower		 = 12;
control.fcm[12].alc_enabled = 'ALC';
control.fcm[12].parser = data=>data.toString();
control.fcm[13] = {}
control.fcm.alc_level		 = 13;
control.fcm[13].description = 'ALC Level';
control.fcm[13].parser = data=>data.toString();
control.fcm[14] = {}
control.fcm.alc_range	 = 14;
control.fcm[14].description = 'ALC Range';
control.fcm[14].parser = data=>data.toString();
//PARAMETER_CONFIG_DLRS_WGS_SWITCHOVER							= 14,
//PARAMETER_CONFIG_FCM_ALC_OVERDRIVE_PROTECTION_ENABLED			= 15,
//PARAMETER_CONFIG_FCM_ALC_OVERDRIVE_PROTECTION_THRESHOLD			= 16,
//PARAMETER_CONFIG_FCM_ALC_OVERDRIVE_PROTECTION_THRESHOLD_RANGE	= 17,
//PARAMETER_CONFIG_FCM_LNB_REFERENCE_CONTROL						= 21,
//PARAMETER_CONFIG_BUC_APC_ENABLE                					= 110,     /* APC enable */
//PARAMETER_CONFIG_BUC_OFFSET_RANGE	         					= 103,    
//PARAMETER_CONFIG_BUC_OFFSET_1_TO_MULTI         					= 104,    
//PARAMETER_CONFIG_BUC_APC_LEVEL		          					= 111,     /* APC target power level */
//PARAMETER_CONFIG_BUC_APC_RANGE        		  					= 112,     /* APC target power range */
//PARAMETER_CONFIG_LNB_LO_SELECT                					= 124, 
//PARAMETER_CONFIG_LNB_STATUST                					= 125, 

// BUC Parameter CODE
control['LO Set']		 = {}
control['LO Set'].code	 = 1;
control['LO Set'].parser =  parseToBigInt;

control.Mute		 = {}
control.Mute.code	 = 2;
control.Mute.parser	 =  parseToBoolean;

control.Gain		 = {}
control.Gain.code	 = 3;
control.Gain.parser	 = bytes=>parseToIrtValue(bytes, 1);

control['Gain Range']		 = {}
control['Gain Range'].code	 = 5;
control['Gain Range'].parser = parseToShortArray;

control.Attenuation			 = {}
control.Attenuation.code	 = 4;
control.Attenuation.parser	 = bytes=>parseToIrtValue(bytes, 1);

control['Attenuation Range'] = {}
control['Attenuation Range'].code		 = 6;
control['Attenuation Range'].parser = parseToShortArray;

control.LO			 = {}
control.LO.code		 = 7;
control.LO.parser	 = data=>data.toString();

control.Frequency		 = {}
control.Frequency.codr	 = 8;
control.Frequency.parser = parseToBigInt;

control['Frequency Range']			 = {}
control['Frequency Range'].code		 = 9;
control['Frequency Range'].parser	 = parseToBigIntArray;

control.Redundancy = {}	// Enable
control.Redundancy.code		 = 10;
control.Redundancy.parser = parseToBoolean;

control.Mode		 = {}	// Redundancy
control.Mode.code	 = 11;
control.Mode.parser = data=>data.toString();

control.Name		 = {}	// Redundancy
control.Name.code	 = 12;
control.Name.parser	 = data=>data.toString();

control.Status			 = {}	// Redundancy
control.Status.code		 = 15;
control.Status.parser	 = parseToIrtValue;

control.Online			 = {}	// Redundancy
control.Online.code		 = 14;
control.Online.parser	 = data=>data.toString();

control['Spectrum Inversion']		 = {}
control['Spectrum Inversion'].code	 = 20;
control['Spectrum Inversion'].parser = data=>data.toString();

// LNB Parameter CODE
control.lnb[0] = {}
control.lnb.none					 = 0;
control.lnb[0].description = 'None';

Object.freeze(control);
export default control;

export function code(name){
	if(typeof name === 'number')
		return name;
	const group = chooseGroup();
	return group[name];
}

export function name(code){
	const group = chooseGroup();
	const keys = Object.keys(group);

	for(const key of keys)
		if(group[key] == code)
			return key;
}

export function description(value){
	const c = code(value)
	return chooseGroup()[c].description;
}

export function toString(value){
	const c = code(value)
	const n = name(value)
	return `configuration: ${n} (${c})`;
}

export function parser(value){
	const c = code(value)
	return chooseGroup()[c].parser;
}
