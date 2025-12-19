import DACController from './controller-dac.js';
import packetId from '../packet/packet-properties/packet-id.js';
import groupId from '../packet/packet-properties/group-id.js';
import {parseToString, intToBytes} from '../packet/service/converter.js'
import Register from '../packet/parameter/value/register.js'

export default class POTsController extends DACController{

	constructor($container){
		super($container, '/fragment/potentiometers/pots');
	}

	/**
     * @param {String} deviceType
     */
	set typeName(deviceType){
		switch(deviceType){
		case 'KA_BIAS':
			this._action.packetId = packetId.POTs_KA_BIAS;
			this._action.groupId = groupId.deviceDebug;
			this._action.data.parameterCode = 2;
			this._action.data.value = [151, 152];
			this._typeName = 'buc'
			break;

		case 'CONVERTER_KA':
			this._action.packetId = packetId.POTs_KA_Converter;
			this._action.data.value = [new Register(30,0), new Register(30,8), new Register(31,0), new Register(31,8)];
			this._typeName = 'converter'
			break;

		default:
			super.typeName = deviceType;
		}
	}

	_onLoad(){
		super._onLoad();
		this._$valueRange.prop('min', 0).prop('max', 255).prop('step', 1);
		this._$container.find('button').click(({currentTarget:{dataset:{register}}})=>{
			const [index, address, value] = register.split(',').map(v=>parseInt(v));
			const shift = this._typeName === 'buc' ? 121 : 0;
			this._actionSet.data.value = new Register(index + shift, address, value);
			this._actionSet.update = true;
			this._onSet(this._actionSet);
		});
	}
	_setInputsData(){
		this._$dacs.each((_, el)=>{

			const text = el.placeholder;
			switch(text){

			case 'UC_VCTRL1':
				el.setAttribute("data-register", this._typeName==='buc' ? '152,8' : '31,8');
				break;

			case 'UC_VCTRL2':
				el.setAttribute("data-register", this._typeName==='buc' ? '152,0' : '31,0');
				break;

			case 'VG_PA':
				el.setAttribute("data-register", this._typeName==='buc' ? '151,8' : '30,8');
				break;

			case 'VC_EQ':
				el.setAttribute("data-register", this._typeName==='buc' ? '151,0' : '30,0');
				break;

			default:
				console.warn('POTsController _onLoad: unexpected DAC name', text);
			}
		});
	}
	_reaction(packet){

		switch(this._typeName){

		case 'buc':
			this._reactionConverter(packet);
			break;

		default:
			super._reaction(packet);
		}
	}

	_reactionConverter(packet){
		packet.payloads.forEach(pl=>{

			const lines = parseToString(pl.data).split(/\r\n|\n|\r/).filter(l=>l.length);
			if(lines.length!==3){
				console.warn('POTsController _reaction: unexpected payload data', pl.data);
				return;
			}
			const added = lines[0].includes('DP1') ? 0 : 2;
			const pls = stringToOayloads(lines[1], added);
			super._reaction({payloads: pls});
		});
	}

	_disableElement(el, disable){
		super._disableElement(el, disable);
		const button = el.parentElement.nextElementSibling;
		disable ? button.setAttribute('disabled', 'disabled') : button.removeAttribute('disabled');
	}
}

function stringToOayloads(line, added){
	const values = line.split(/\s+/).filter(s=>s.startsWith('0x')).map(v=>parseInt(v, 16));
	return [stringToPayload(values[0], 0 + added), stringToPayload(values[1], 1 + added)];
}
function stringToPayload(value, index){
	return {data: intToBytes(index<2 ? 151 : 152).concat(intToBytes(index%2 ? 8 : 0)).concat(intToBytes(value))};
}