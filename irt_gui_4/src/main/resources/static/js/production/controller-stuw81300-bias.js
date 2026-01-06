import packetId from '../packet/packet-properties/packet-id.js';
import deviceDebug from '../packet/parameter/device-debug.js';
import STUW81300Controller from './controller-stuw81300.js';
import Stuw81300DampParser from './stuw81300/stuw81300-damp-parser.js';

export default class STUW81300Bias extends STUW81300Controller{

	constructor($container){
		super($container, deviceDebug.debugDump.code, 127, packetId.stuw81300Bias, packetId.stuw81300BiasSet);
	}

	_reaction(packet){
		const regs = packet.payloads.map(pl=>deviceDebug.debugDump.parser(pl.data)).map(str => new Stuw81300DampParser(str).parse());
		super._reaction({payloads: regs.flat()});
	}
}