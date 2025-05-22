import {parseToIrtValue, parseToShortArray, parseToBigInt, parseToBigIntArray, parseToBoolean} from '../service/converter.js'
import {type} from '../service/device-type.js'

const control = {};
control.fcm = {};
control.buc = {};
control.lnb = {};

function chooseGrout(){
	let t
	switch(type){

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
control.buc[0] = {}
control.buc.none					 = 0;
control.buc[0].description = 'None';
control.buc[0].parser = data=>data.toString();
control.buc[1] = {}
control.buc.lo_set					 = 1;
control.buc[1].description = 'LO Set';
control.buc[1].parser =  data=>data.toString();
control.buc[2] = {}
control.buc.mute					 = 2;
control.buc[2].description = 'Mute';
control.buc[2].parser =  parseToBoolean;
control.buc[3] = {}
control.buc.gain					 = 3;
control.buc[3].description = 'Gain';
control.buc[3].parser = bytes=>parseToIrtValue(bytes, 1);
control.buc[5] = {}
control.buc.gain_range				 = 5;
control.buc[5].description = 'Gain Range';
control.buc[5].parser = parseToShortArray;
control.buc[4] = {}
control.buc.attenuation				 = 4;
control.buc[4].description = 'Attenuation';
control.buc[4].parser = bytes=>parseToIrtValue(bytes, 1);
control.buc[6] = {}
control.buc.attenuation_range		 = 6;
control.buc[6].description = 'Attenuation Range';
control.buc[6].parser = parseToShortArray;
control.buc[7] = {}
control.buc.lo_frequencies			 = 7;
control.buc[7].description = 'LO';
control.buc[7].parser = data=>data.toString();
control.buc[8] = {}
control.buc.frequency				 = 8;
control.buc[8].description = 'Frequency';
control.buc[8].parser = parseToBigInt;
control.buc[9] = {}
control.buc.frequency_range			 = 9;
control.buc[9].description = 'Frequency Range';
control.buc[9].parser = parseToBigIntArray;
control.buc[10] = {}
control.buc.redundancy_enable		 = 10;
control.buc[10].description = 'Redundancy';
control.buc[10].parser = parseToBoolean;
control.buc[11] = {}
control.buc.redundancy_mode			 = 11;
control.buc[11].description = 'Mode';
control.buc[11].parser = data=>data.toString();
control.buc[12] = {}
control.buc.redundancy_name			 = 12;
control.buc[12].description = 'Name';
control.buc[12].parser = data=>data.toString();
control.buc[15] = {}
control.buc.redundancy_status		 = 15;
control.buc[15].description = 'Status';
control.buc[15].parser = parseToIrtValue;
control.buc[14] = {}
control.buc.redundancy_set_online	 = 14;
control.buc[14].description = 'Online';
control.buc[14].parser = data=>data.toString();
control.buc[20] = {}
control.buc.spectrum_inversion		 = 20;
control.buc[20].description = 'Spectrum Inversion';
control.buc[20].parser = data=>data.toString();

// LNB Parameter CODE
control.lnb[0] = {}
control.lnb.none					 = 0;
control.lnb[0].description = 'None';

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
		if(group[key] == code)
			return key;
}

export function description(value){
	const c = code(value)
	return chooseGrout()[c].description;
}

export function toString(value){
	const c = code(value)
	const n = name(value)
	return `configuration: ${n} (${c})`;
}

export function parser(value){
	const c = code(value)
	return chooseGrout()[c].parser;
}
