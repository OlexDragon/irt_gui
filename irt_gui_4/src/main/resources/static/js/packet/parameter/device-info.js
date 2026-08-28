import { createIdMap } from '../packet-properties/helper/id-map.mjs';
import {
    parseToString,
    parseToTimeStr,
    parseToIntSequence
} from '../service/converter.js';
import Payload from '../payload.js';
import translator from '../../helper/text-translator.mjs';

const deviceInfo = createIdMap({
    type: 1,
    firmwareVersion: 2,
    firmwareBuild: 3,
    uptimeCounter: 4,
    serialNumber: 5,
    description: 6,
    partNumber: 7,
    all: 255
});

export const id = deviceInfo.id;
export const name = deviceInfo.name;
export const toString = deviceInfo.toString;
export const info = deviceInfo.info;

export default deviceInfo.map;


// Show order

const sequence = {
    [id('description')]: 0,
    [id('serialNumber')]: 1,
    [id('partNumber')]: 2,
    [id('type')]: 3,
    [id('firmwareVersion')]: 4,
    [id('firmwareBuild')]: 5,
    [id('uptimeCounter')]: 6
};

export function order(value) {

    const code = typeof value === 'number'
        ? value
        : id(value);

    return sequence[code];
}

export function comparator(index1, index2) {

    if (index1 instanceof Payload) {
        index1 = index1.parameter.code;
        index2 = index2.parameter.code;
    }

    return sequence[index1] - sequence[index2];
}


// Description

const strings = {
    [id('description')]: 'Description',
    [id('serialNumber')]: 'Serial Number',
    [id('partNumber')]: 'Part Number',
    [id('type')]: 'Type',
    [id('firmwareVersion')]: 'FW Version',
    [id('firmwareBuild')]: 'FW Build',
    [id('uptimeCounter')]: 'Counter'
};

export function description(value) {

    const code = typeof value === 'number'
        ? value
        : id(value);

        const key = `info.${code}`;
        const translated = translator.translate(key);
        if (key !== translated)
            return translated;
        console.warn(`The key ${key} is missing`)

    return strings[code];
}


// Device Info parse functions

const parsers = {
    [id('description')]: parseToString,
    [id('serialNumber')]: parseToString,
    [id('partNumber')]: parseToString,
    [id('type')]: parseToIntSequence,
    [id('firmwareVersion')]: parseToString,
    [id('firmwareBuild')]: parseToString,
    [id('uptimeCounter')]: parseToTimeStr
};

export function parser(value) {

    const code = typeof value === 'number'
        ? value
        : id(value);

    return parsers[code] ?? String(value);
}