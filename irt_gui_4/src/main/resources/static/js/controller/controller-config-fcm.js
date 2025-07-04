import ControllerConfig from './controller-config.js'

export default class ControllerControlFcm extends ControllerConfig{

	_frequencyRange(val){
		const set = new Set(val);
		if(set.size!=val.length){
			this._min(val[0]);
			const m = val[val.length-1];
			this._max(m);
			this._step(m - val[0]);
		}else
			super._frequencyRange(val);
	}
}
