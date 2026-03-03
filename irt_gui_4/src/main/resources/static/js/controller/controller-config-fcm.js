import ControllerConfig from './controller-config.js';
import packetId from '../packet/packet-properties/packet-id.js';
import groupId from '../packet/packet-properties/group-id.js';

export default class ControllerControlFcm extends ControllerConfig{

	_frequencyRange(val){
		const set = new Set(val);
		if(set.size!=val.length){
			this._min(val[0]);
			const m = val[val.length-1];
			this._max(m);
			if(set.size===2)
				this._step(m - val[0]);
			else
				this._tickMarks(set);
		}else
			super._frequencyRange(val);
	}
	_onLoad(_, statusText){
		super._onLoad(undefined, statusText);
		$('#fcmSaveConfig').click(()=>this._sendChange(packetId.saveConfig, 0, 1, groupId.control)).parent().removeClass('visually-hidden');
	}
}
