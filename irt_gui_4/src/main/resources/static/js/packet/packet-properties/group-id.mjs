import { createIdMap } from './helper/id-map.mjs';

const groupId = createIdMap({
	alarm: 1,
	configuration: 2,
	filetransfer: 3,
	measurement: 4,
	reset: 5,
	deviceInfo: 8,
	control: 9,
	protocol: 10,
	network: 11,
	redundancy: 12,
	deviceDebug: 61,
	production: 100,
	developer: 120
});

export const id = groupId.id;
export const name = groupId.name;
export const toString = groupId.toString;

export const info = groupId.info;

export default groupId.map;
