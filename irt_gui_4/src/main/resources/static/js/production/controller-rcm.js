import DACController from './controller-dac.js';
import packetId from '../packet/packet-properties/packet-id.js';
import groupId from '../packet/packet-properties/group-id.js';
import {parser} from '../packet/parameter/config-rcm.js'

export default class DACsController extends DACController{

	constructor($container){
		super($container, '/fragment/rcm/rcm');
		this._typeName = 'rcm';
		this._action.name = 'RCM DAC';
		this._actionSet.name = 'SET RCM DAC';
		this._action.packetId = packetId.dacRcm;
		this._actionSet.packetId = packetId.dacSetRcm;
		this._action.groupId = this._actionSet.groupId = groupId.configuration;
		this._action.data.parameterCode = [2,3];
		this._action.data.value = 'Not Used';
		this.action.f_reaction = this._reaction.bind(this);
	}

	/**
     * @param {string} deviceType
     */
	set typeName(deviceType){

		if(deviceType!=='REFERENCE_BOARD')
			throw new Error('This controler can not have this type - ' + deviceType);

	}

	_reaction(packet){
		packet.payloads.forEach(pl=>{
			const code = pl.parameter.code;
			const value = parser(code)(pl.data);
			switch(code){

			case 2:
				if(value?.length===2 && this._$dacRange.prop('max')!==`${value[1]}`){
					this._$dacRange.prop('max', value[1]);
					this._action.data.parameterCode.splice(0,1);
					this._action.update = true;
				}
				break;

			case 3:
				const valStr = `${value}`;
				if(this._selected){
					if(this._selected.value!==valStr)
						this._selected.value = valStr;
				}else if(this._$dacs.val()!==`${value}`){
					this._$dacs.val(value);
					this._$dacRange.val(value);
					if(this._$dacs.prop('disabled'))
						this._$dacs.prop('disabled', false);
				}
				break;

			default:
				console.warn(pl);
			}
		});
	}

	_sendCommand({dataset:{rcm}, value}){
		this._actionSet.data = {};
		this._actionSet.data.parameterCode = +rcm;
		this._actionSet.data.value = +value;
		this._actionSet.update = true;
		this._onSet(this._actionSet);
	}
}

