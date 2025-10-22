import ADMV1013Controller from './controller-admv1013.js'
import packetId from '../packet/packet-properties/packet-id.js'
import Register from '../packet/parameter/value/register.js'
import deviceDebug from '../packet/parameter/device-debug.js'

export default class ADMV1013Converterr extends ADMV1013Controller{

	constructor($container){
		super(
			$container,
			deviceDebug.readWrite.code,
			[
				new Register(7,0),
				new Register(7,1),
				new Register(7,2),
				new Register(7,3),
				new Register(7,5),
				new Register(7,6),
				new Register(7,7),
				new Register(7,8),
				new Register(7,9),
				new Register(7,10)
			],
			packetId.admv1013,
			packetId.admv1013Set
		);
	}

	_reaction(packet){
		const regs = packet.payloads.map(pl=>Register.parseRegister(pl.data));
		super._reaction(regs);
	}

	_btnClick() {
		if (!super._btnClick())
			return;

		this._sendCommand(new Register(20, 5, 1), true);
	}
}

