import {parseToIntArray} from '../service/converter.js'

const deviceDebug = {};

deviceDebug.parameter = {};
deviceDebug.parameter.debugInfo = 1;		/* device information: parts, firmware and etc. */
deviceDebug.parameter.debugDump = 2;		/* dump of registers for specified device index */
deviceDebug.parameter.readWrite = 3;		/* registers read/write operations */
deviceDebug.parameter.index		= 4;		/* device index information print */
deviceDebug.parameter.calibrationMode = 5;	/* calibration mode */
deviceDebug.parameter.environmentIo = 10;	/* operations with environment variables */
deviceDebug.parameter.devices	= 30;

Object.freeze(deviceDebug);

const deviceDebugNames = Object.keys(deviceDebug).reduce((a,k)=>{

		a[deviceDebug[k].code] = k;
		return a;
	}, []
);
Object.freeze(deviceDebugNames);

export default deviceDebug;

export function code(name){
	if(typeof name === 'number')
		return name;

	return deviceDebug[name];
}

export function name(code){
	if(typeof code === 'string')
		return deviceDebugNames.includes(code) ? code : undefined;

	return deviceDebugNames[code];
}

const parser = parseToIntArray;

export { parser };