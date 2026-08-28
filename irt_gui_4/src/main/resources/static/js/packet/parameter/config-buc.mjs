// config-buc.mjs

import {
    parseToBoolean,
    parseToIrtValue,
    parseToShortArray,
    parseToLoFrequency,
    parseToBigInt,
    parseToBigIntArray,
    parseToInt,
    parseToOnOff,
    parseToCapabilities,
    parseToSingleByte,
    parseToRedStatus
} from '../service/converter.js';

import Parameter from "./parameters.mjs";

export default class ControlBuc extends Parameter {
    constructor() {
        super(config, 'Control BUC');
    }

    get readAllCode() {
        const {
            gainRange,
            attenuationRange,
            frequencyRange,
            gain,
            attenuation,
            frequency,
            loSet,
            lo,
            mute
        } = this.parameters;

        return codesOf({
            gainRange,
            attenuationRange,
            frequencyRange,
            lo,
            gain,
            attenuation,
            frequency,
            loSet,
            mute
        });
    }
}

const codesOf = parameters =>
    Object.fromEntries(
        Object.entries(parameters).map(([name, { code }]) => [name, code])
    );

// ------------------------------------------------------------
// Elegant parameter definition helpers
// ------------------------------------------------------------

const define = (code, parser) => ({ code, parser });

const irt = (code, usedFor) => ({
    code,
    parser: bytes => parseToIrtValue({
        bytes,
        divider: 10,
        usedFor
    })
});

// ------------------------------------------------------------
// BUC Parameter Configuration
// ------------------------------------------------------------

const config = {

    mute: define(2, parseToBoolean),

    gain: irt(3, bytes => bytes.tiString()),
	gainRange: define(5, parseToShortArray),

    attenuation: irt(4, bytes => bytes.tiString()),
    attenuationRange: define(6, parseToShortArray),

	loSet: define(1, bytes => bytes[0]),
    lo: define(7, parseToLoFrequency),

    frequency: define(8, parseToBigInt),
    frequencyRange: define(9, parseToBigIntArray),

    redEnable: define(10, parseToBoolean),
    redMode: define(11, parseToSingleByte),
    redName: define(12, parseToSingleByte),
    redOnline: define(14, data => data),
    redStatus: define(15, parseToRedStatus),

	refSource: define(31, parseToInt),
	refCapability: define(32, parseToCapabilities),

    spectrumInversion: define(20, parseToOnOff)
};

const controlBuc = new ControlBuc();
export { controlBuc, config }; // config left for compatibility