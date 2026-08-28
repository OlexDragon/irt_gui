// alarm.js

import Payload from '../payload.js'
import Parameter from '../parameter.js'
import { parseToAlarmStatus, parseToAlarmString, parseToShortArray, shortToBytes } from '../service/converter.js'
import { createIdMap } from '../packet-properties/helper/id-map.mjs'

const alarms = Object.freeze([
    'none',
    'number of alarms',
    'IDs',
    'summaryStatus',
    'config',
    'status',
    'description',
    'name'
])

const parsers = Object.freeze([
    ,
    bytes => bytes[0],
    parseToShortArray,
    parseToAlarmStatus,
    ,
    parseToAlarmStatus,
    parseToAlarmString,
    parseToAlarmString
])

const alarmDescription = Object.freeze([
    'none - Not Implemented',
    'number of alarms',
    'IDs - Presented alarm codes.',
    'summary status',
    'config - I don\'t know.',
    'status - Alarm status.',
    'description',
    'name'
])

const alarmIds = createIdMap(alarms, { prefix: 'alarm' })
export const {
    map,
    info,
    id,
    name } = alarmIds

// Compatibility
export const alarmCode = alarmIds.map

export function code(value) {
    return alarmIds.id(value)
}

//export function name(value) {
//    return alarmIds.name(value)
//}

export function description(value) {
    const c = code(value)
    return alarmDescription[c]
}

export function toString(value) {
    return alarmIds.toString(value)
}

export function parser(value) {
    const c = code(value)
    return parsers[c]
}

export default class Alarm {
    constructor(pl) {
        this.code = code(pl.parameter.code)
        this.name = name(this.code)
        this.description = description(this.code)
        this.parser = parser(this.code)
    }
}

export function payload(id, value) {
    const c = code(id)
    const parameter = new Parameter(c)
    const bytes = shortToBytes(value)
    const array = [bytes[1], bytes[0]]

    return new Payload(parameter, array)
}

export function payloads(ids, withName) {
    const array = []

    ids.forEach(id => {
        if (withName)
            array.push(payload(code('description'), id))
        else
            array.push(payload(code('status'), id))
    })

    return array
}