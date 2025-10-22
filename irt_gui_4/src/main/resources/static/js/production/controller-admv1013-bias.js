import ADMV1013Controller from './controller-admv1013.js'
import packetId from '../packet/packet-properties/packet-id.js'
import deviceDebug from '../packet/parameter/device-debug.js'
import Register from '../packet/parameter/value/register.js'
import Admv1013DampParser from './admv1013/admv1013-damp-parser.js'

export default class ADMV1013Bias extends ADMV1013Controller{

	constructor($container){
		super($container, deviceDebug.debugDump.code, 128, packetId.admv1013Bias, packetId.admv1013BiasSet);
	}

	_reaction(packet){
		const regs = packet.payloads.map(pl=>deviceDebug.debugDump.parser(pl.data)).map(str => new Admv1013DampParser(str).parse());
		super._reaction(regs.flat());
	}

	_btnClick() {
		if (!super._btnClick())
			return;

		this._sendCommand(new Register(141, 5, 1), true);
	}
}

