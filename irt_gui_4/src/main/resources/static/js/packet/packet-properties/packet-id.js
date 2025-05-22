
const packetId = Object.freeze(
	[
		'deviceInfo',

		'measurementAll',

		'controlAll',
		'atenuation',
		'atenuationSet',
		'gain',
		'gainSet',
		'frequency',
		'frequencySet',
		'mute_control',

		'network',
		'networkSet',

		'alarmAll',
		'alarm',
		'alarmSummary',
		'alarmIDs',

		'redundancyAll',
		'redundancySetOnline',
		'redundancySetEnable',
		'redundancySetDisable',
		'redundancySetCold',
		'redundancySetHot',
		'redundancySetNameA',
		'redundancySetNameB'
	]);

export function id(name){
	return packetId.indexOf(name);
}

export function name(code){
	return packetId[code];
}

export function toString(value){

	if(typeof value === 'number'){

		const n = name(value);
		return `${n} (${value})`;

	}else{

		const code = id(value);
		return `${value} (${code})`;
	}
}