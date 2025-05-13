import {id} from './group-id.js'
import {description as diDescription, parser as diParser, name as diName, toString as diToSyting} from '../parameter/device-info.js'

const deviceInfo = id('deviceInfo')
const deviceDebug = id('deviceDebug')
const functions = {};

// Device Info

functions[deviceInfo] = {};
functions[deviceDebug] = {};

functions[deviceInfo].name = diName;

export function name(groupId){

	if(typeof groupId === 'string')
		groupId = id(groupId);

	return functions[groupId].name;
}

functions[deviceInfo].description = diDescription;

export function description(groupId){

	if(typeof groupId === 'string')
		groupId = id(groupId);

	return functions[groupId].description;
}

functions[deviceInfo].parser = diParser;

export function parser(groupId){

	if(typeof groupId === 'string')
		groupId = id(groupId);

	return functions[groupId].parser;
}

functions[deviceInfo].toString = diToSyting;

export function toString(groupId){

	if(typeof groupId === 'string')
		groupId = id(groupId);

	return functions[groupId].toString;
}

// Device Debug

//parameterCode[groupId.deviceDebug][deviceDebug.parameter.readWrite] = {};
//parameterCode[groupId.deviceDebug][deviceDebug.parameter.readWrite].description	 = 'Device Debug Register Read/Write'
//parameterCode[groupId.deviceDebug][deviceDebug.parameter.readWrite].parseFunction = parseToIrtRegister; // IRT Register
//
//parameterCode[groupId.deviceDebug][deviceDebug.parameter.debugDump] = {};
//parameterCode[groupId.deviceDebug][deviceDebug.parameter.debugDump].description	 = 'Dump Registers'
//parameterCode[groupId.deviceDebug][deviceDebug.parameter.debugDump].parseFunction = parseToString;
