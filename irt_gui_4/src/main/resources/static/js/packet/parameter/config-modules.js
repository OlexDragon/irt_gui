// config-modules.js

import { createIdMap } from '../packet-properties/helper/id-map.mjs';
import { parseToString, parseToInt } from '../service/converter.js';

const config = createIdMap({
    saveProfile: 1,
    reset: 2,
    restor: 3,
    activeModule: 10,
    moduleList: 11
}, {
    prefix: 'configuration'
});

export const {
    code,
    name,
	info,
    toString
} = config;

export default config.map;

export function parser(codeId){

	if(typeof codeId === 'string')
		codeId = config.code(codeId);

	switch(codeId){

	case config.map.moduleList:
		return parseModuleList;

	case config.map.activeModule:
		return parseToInt;

	default:
		return b=>b;
	}
}

function parseModuleList(bytes){
	if(!bytes?.length)
		return;

	const modules = {};
	const b = [...bytes];
	while(b.length){

		const moduleId = b.splice(0,1)&0xff;
		const length = b.indexOf(0) + 1;
		const name = parseToString(b.splice(b, length || b.length));
		modules[name] = moduleId;
	}

	return modules;
}
