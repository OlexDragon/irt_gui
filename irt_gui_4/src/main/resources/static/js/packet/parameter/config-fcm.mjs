// config-fcm.mjs
import {
    parseToIrtValue,
    parseToBigInt,
    parseToBigIntArray,
    parseToShortArray,
    parseToBoolean,
    parseToOnOffStatus,
    parseToOnOff,
    parseToCapabilities,
    parseToInt
} from '../service/converter.js';

import Parameter from "./parameters.mjs";

export default class ControlFcm extends Parameter {
    constructor() {
        super(config, 'Control FCM');
    }

    get all() {
        const {
            gainRange,
            attenuationRange,
            frequencyRange,
            gain,
            attenuation,
            Frequency,
            mute
        } = this.parameters;

        return { gainRange, attenuationRange, frequencyRange, gain, attenuation, Frequency, mute };
    }
}

// ------------------------------------------------------------
// Elegant parameter definition helper
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
// FCM Parameter Configuration (clean & consistent)
// ------------------------------------------------------------

// !!! Do this to change the parameter name.: config.gain.name = 'bucGain
const config = {

    attenuation: irt(2, bytes=>bytes.toString()),
	attenuationRange: define(6, parseToShortArray),

    frequency: define(3, parseToBigInt),
    frequencyRange: define(4, parseToBigIntArray),

	gain: irt(1,  'gain' ),
    gainRange: define(5, parseToShortArray),

	gainOffset: irt(10,  bytes=>bytes.toString()),
	gainOffsetRange: define(11, parseToShortArray),

    mute: define(7, parseToBoolean),

    powerToLnb: define(8, parseToOnOffStatus),
    flags: define(9, data => data.toString()),



    alcOnOff: define(12, bytes => !!bytes[0]),
    alcLevel: irt(13,  bytes=>bytes.toString()),
    alcLevelRange: define(14, parseToShortArray),
	
    alcProtectionOnOff: define(15, data => data.toString()),
    alcProtectionThreshold: define(16, data => data.toString()),
    alcProtectionRange: define(17, data => data.toString()),

    refSource: define(18, parseToInt),
    refCapability: define(19, parseToCapabilities),

    spectrumInversion: define(20, parseToOnOff),

    referenceToLnb: define(21, parseToOnOff),

    // Special "all" parameter
    all: define(255, () => null)
};

export { config };
