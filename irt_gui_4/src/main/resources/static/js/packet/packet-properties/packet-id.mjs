import { createIdMap } from './helper/id-map.mjs'

const ids = createIdMap([
    'deviceInfo',
    'measurement',
    'measurementIRPC',

    'configAll',
    'attenuation',
    'attenuationSet',
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
    'redundancySet',

    'irpc',
    'irpcSalectSwtchHvr',
    'irpcStandBy',
    'irpcDefault',
    'irpcHoverA',
    'irpcHoverB',

    'odrc',
    'odrcSetMode',
    'odrcLNBSelect',

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

    'lnbSetMode',
    'lnbOverSet',

    'lnbRegisters',
    'lnbRegistersSet',

    'lnbBand',
    'lnbBandSet',

    'dacRcm',
    'dacSetRcm',

    'admv1013',
    'admv1013Set',

    'admv1013Bias',
    'admv1013BiasSet',

    'stuw81300',
    'stuw81300Set',

    'stuw81300Bias',
    'stuw81300BiasSet',

    'dump',
    'dumpHelp',

    'noAction',

    'saveConfig',

    'rcmSourceSet',
    'rcmDacSet',
    'rcmDacSave',
    'rcmDacDefault',

    'POTs_KA_BIAS',
    'POTs_KA_Converter',

    'dumpADC',

    'gainOffsetSet',
    'alcLevelSet',
    'alcOnOff',
    'scanIp',

    'refSource',
    'refSourceSet'
]);

export const id = ids.id;
export const name = ids.name;
export const toString = ids.toString;
export const info = ids.info;

export default ids.map;
