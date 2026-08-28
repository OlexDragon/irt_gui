// converter.js
import translator from '../../helper/text-translator.mjs';
import { status as alarmStatus } from '../parameter/value/alarm-status.js'
import IrtValue from '../parameter/value/irt-value.js'

export function shortToBytes(value, bigEndian = false) {
    const bytes = toBytes(value, 2);
    return bigEndian ? bytes : bytes.reverse();
}

export const shortToBytesR = (value) => shortToBytes(value, true);
export const intToBytes = value => toBytes(value, 4);
export const longToBytes = value => toBytes(value, 8);

export const intArrayToBytes = (...values) =>
    values.flatMap(intToBytes);

export function toBytes(value, minBytes = 0) {
    const bytes = [];

    if (typeof value === 'bigint') {
        do {
            bytes.unshift(Number(value & 0xffn));
            value >>= 8n;
        } while (value);
    } else {
        do {
            bytes.unshift(value & 0xff);
            value >>>= 8;
        } while (value);
    }

    while (bytes.length < minBytes)
        bytes.unshift(0);

    return bytes;
}

export const UTF8_DECODER = new TextDecoder();

export function parseToString(bytes) {
    if (!bytes)
        return null;

    const end = bytes.indexOf?.(0) ?? -1;
    const data = end >= 0 ? bytes.slice(0, end) : bytes;

    return UTF8_DECODER.decode(new Uint8Array(data));
}

export function parseToInt(bytes, unsigned) {

    let index = bytes.length - 1;
    let intValue = !unsigned && index == 0 ? bytes[index] << 24 >> 24 : bytes[index] & 0xff;

    for (let i = 1;index > 0 && i < bytes.length;i++) {
        const shift = i * 8;
        let v

        if (unsigned)
            v = bytes[--index] & 0xff;
        else if (index == 1)
            v = bytes[--index] << 24 >> 24;
        else
            v = bytes[--index];

        intValue |= v << shift;
    }
    return intValue;
};

function byteToHex(b) {
    return (b + 0x100).toString(16).slice(-2).toUpperCase();
}

function bytesToHexString(bytes) {
    return bytes.map(byteToHex).join('');
}

export function parseToBigInt(bytes) {
    return BigInt('0x' + bytesToHexString(bytes));
}

export function parseToBigIntArray(bytes) {
    const ints = [];
    const b = [...bytes];
    //	for(let i=0; b.length && i<3; i++){
    while (b.length)
        ints.push(parseToBigInt(b.splice(0, 8)));
    return ints;
}

export function parseToIntUnsigned(bytes) {
    return parseToInt(bytes, true);
};

export function parseToIntArray(bytes) {
    return parseToArray(bytes, 4);
}

export function parseToIntSequence(bytes) {
    return parseToArray(bytes, 4).join('.');
}

export function parseToShortArray(bytes) {
    return parseToArray(bytes, 2);
}

function parseToArray(bytes, size) {
    const ints = [];
    const b = [...bytes];
    while (b.length)
        ints.push(parseToInt(b.splice(0, size)));

    return ints;
}

const specialValues = {
    frequency: {
        0: 'UNDEFINED'
    },
    gain: {
        [-0x8000]: 'UNDEFINED'
    },
    temperature: {
        [-0x8000]: 'UNDEFINED'
    },
    current: {
        [-0x8000]: 'UNDEFINED'
    },
    attenuation: {
        [-0x8000]: 'UNDEFINED'
    },
    power: {
        [-0x8000]: 'UNDEFINED',
        [0x7FFF]: 'OUT_OF_RANGE'
    }
};
const prefixes = [, '', '<', '>', 'N/A']
export function parseToIrtValue({ bytes, divider, postfix, usedFor }) {

    if (!Array.isArray(bytes)) {
        console.warn('bytes is not iterable', { bytes });
        return;
    }

    const b = [...bytes];
    if (!b?.length)
        return new IrtValue();

    let prefix;
    if (b.length === 3) {
        const index = b.splice(0, 1)[0] & 7;
        if (index === 0)
            return new IrtValue('UNDEFINED');
        prefix = prefixes[index];
        if (index === 4)
            return new IrtValue(prefix);
    }
    const intValue = parseToInt(b);

    const special = specialValues[usedFor]?.[intValue];
    if (special)
        return new IrtValue(special);

    return new IrtValue(intValue, prefix, postfix, divider);
}

export function parseToFreqyency(bytes) {
    const b = [...bytes]
    return parseToBigInt(b) / 1000000n;
}

const
    MINUTE = 60,
    HOUR = 60 * MINUTE,
    DAY = 24 * HOUR;

export function parseToTimeStr(bytes) {
    const time = parseToInt(bytes);
    const days = Math.floor(time / DAY);
    const hours = Math.floor(time % DAY / HOUR);
    const minutes = Math.floor(time % HOUR / MINUTE);
    const sec = time % MINUTE;
    return [days, hours, minutes, sec].map(t => t.toString().padStart(2, '0')).join(':');
}

const statusBits = {
    buc: [
        { mask: 0x01, values: { 0x01: 'mute' } },
        { mask: 0x06, values: { 0x02: 'locked', 0x04: 'unlocked' } },
        {
            mask: 0x30,
            values: {
                0x10: 'internal',
                0x20: 'external',
                0x30: 'autosense'
            }
        }
    ]
};

export function translate(prefix, key) {
	
	const byKey = `${prefix}.${key}`;
    const translated = translator.translate(byKey);
    if (translated !== byKey)
        return translated;
    console.warn(`The key "${byKey}" is missing`);
	return key;
}

export function parseToStatus(bytes, type = 'buc') {
    const value = parseToInt(bytes);

    return statusBits[type]
        .map(({ mask, values }) => values[value & mask])
        .filter(Boolean);
}

export function parseToAlarmStatus(bytes) {
    return alarmStatus(bytes);
}

export function parseToAlarmString(bytes) {
    const b = [...bytes];
    return {
        id: parseToInt(b.splice(0, 2)),
        string: parseToString(b)
    };
}

const REF_SOURCE = [, 'INTERNAL', 'EXTERNAL', 'AUTOSENSE']// value: 1 - INTERNAL, 2 - EXTERNAL, 3 - AUTOSENSE
export function parseRefSource(bytes) {
    return REF_SOURCE[parseToInt(bytes)];
}

export function parseRefSourceInfo(bytes) {
    const code = parseToInt(bytes);
    return {
        code,
        status: REF_SOURCE[code]
    };
}

export function parseToSingleByte(bytes) {
    return bytes[0];
}

export function parseToBoolean(bytes) {
    return !!parseToInt(bytes);
}

export function parseToOnOff(bytes) {
    const b = parseToInt(bytes);

    if (!b)
        return undefined;

    return b === 1;
}

export function parseToOnOffStatus(bytes) {
    switch (parseToInt(bytes)) {
        case 2: return true;
        case 3: return false;
        default: return undefined;
    }
}

export function parseToLoFrequency(bytes) {
    const b = [...bytes];
    const result = [];

    while (b.length) {
        const index = b.shift() & 0xff;

        if (!b.length)
            break;

        const frequency = parseToBigInt(b.splice(0, 8)) / 1000000n;

        result.push({
            value: index,
            name: `${frequency} MHz`
        });
    }

    return result;
}

const capabilities = [, 'Internal', 'External', 'Autosense'];
export function parseToCapabilities(bytes) {
    const intVal = parseToInt(bytes);

    // Bit #0 means refSource is not supported.
    if (intVal & 1)
        return [];

    const result = [];

    for (let bit = 1;bit <= 3;bit++) {
        if (intVal & (1 << bit))
            result.push({
                value: bit,
                name: capabilities[bit]
            });
    }

    return result;
}

const status = ['UNKNOWN', 'Online', 'Standby']
export function parseToRedStatus(bytes) {
    const value = parseToInt(bytes)

    return {
        value,
        isOnline: value === 1,
        isStandby: value === 2,
        text: status[value] ?? ''
    }
}
