import packetId from '../packet/packet-properties/packet-id.js';
import deviceDebug from '../packet/parameter/device-debug.js';
import STUW81300Controller from './controller-stuw81300.js';
import Register from '../packet/parameter/value/register.js';

export default class STUW81300Converter extends STUW81300Controller{

	constructor($container){
		super(
			$container,
			deviceDebug.readWrite.code,
			[
				new Register(6, 0),
				new Register(6, 1),
				new Register(6, 2),
				new Register(6, 3),
				new Register(6, 4),
				new Register(6, 5),
				new Register(6, 6),
				new Register(6, 7),
				new Register(6, 8),
				//				new Register(6,9),
				new Register(6, 10),
				new Register(6, 11)
			],
			packetId.stuw81300,
			packetId.stuw81300Set);
	}
}