
const packetIdArray = Object.freeze(
	[
		'deviceInfo',

		'measurement',
		'measurementIRPC',

		'configAll',
		'atenuation',
		'atenuationSet',
		'gain',
		'gainSet',
		'frequency',
		'frequencySet',
		'muteSet',
		'loSet',

		'network',
		'networkSet',

		'alarmDescription',
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
		'redundancySetNameB',

		'irpc',
		'irpcSalectSwtchHvr',
		'irpcStandBy',
		'irpcDefault',
		'irpcHoverA',
		'irpcHoverB',

		'comAll',
		'comSetAddress',
		'comSetRetransmit',
		'comSetStandard',
		'comSetBaudrate',

		'module',
		'moduleSet',

		'register',
		'register1',
		'register2',
		'register3',
		'register4',
		'registerSet',

		'calMode',
		'calModeSet',

		'dacs',
		'dacsSet',

		'dacRcm',
		'dacSetRcm',

		'admv1013',
		'admv1013Set',

		'dump',
		'dumpHelp',

		'noAction'
	]);

const packetId = Object.freeze(packetIdArray.reduce((a, v, i)=>({...a, [v]: i}), {}));
export default packetId;
//console.log(packetId);

export function id(name){
	if(typeof name === 'number')
		return name;
	return packetId[name];
}

export function name(code){
	return packetIdArray[code];
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