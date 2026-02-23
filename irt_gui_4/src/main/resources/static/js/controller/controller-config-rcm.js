import Controller from './controller.js'
import packetId from '../packet/packet-properties/packet-id.js'
import groupId from '../packet/packet-properties/group-id.js'
import ControllerValue from '../classes/controller-value.js'

export default class ControllerConfigRcm extends Controller{
	static url = '/fragment/control/rcm';

	#$capabilities;
	#$rcmProfileValue;

	#dacController;
	#mask;
	#onChangeEvents = [];

	constructor($card) {
		super($card);
		this._$card.load(ControllerConfigRcm.url, this.#onLoad.bind(this));
	}
	destroy(){
		console.log('***** destroy() *****')
		super.destroy();
	}
	get groupId(){
		return groupId.configuration;
	}

	/**
	 * @param {Object[]} pls
	 */
	set update(pls){
		pls.forEach(pl=>{
			const parameterCode = pl.parameter.code;
			const parameters = this.parametersClass.parameters;

			switch(parameterCode){

			case parameters.Capabilities.code:{
					delete this._toRead.Capabilities;
					const val = parameters.Capabilities.parser(pl.data);
					if(!val){
						this.#$capabilities.parent().hide();
						delete this._toRead.Source;
						return;
					}
					val.forEach(v=>{
						const key = Object.keys(v)[0];
						this.#$capabilities.append($('<option>', {value: v[key], text: key}));	// value: 1 - INTERNAL, 2 - EXTERNAL, 3 - AUTOSENSE
					});
				}
				break;

			case parameters.Source.code:{
					const val = parameters.Source.parser(pl.data);
					this.#$capabilities.val(val);
				}
				break;

			case parameters['DAC Range'].code:{
					delete this._toRead.range;
					const val = parameters['DAC Range'].parser(pl.data);
					this.#dacController.min = val[0];
					this.#dacController.max = val[1];
					this.#dacController.step = 1;
					this.#createMask(val[1]);
				}
				break;

			case parameters.DAC.code:{
					const val = parameters.DAC.parser(pl.data);
					this.#dacController.value = val & this.#mask;
					this.#$rcmProfileValue.val(val * 16);
				}
				break;

			case parameters['Factory Reset'].code:
				break;

			default:
				console.warn('Unknown parameter code: ', parameterCode);
				const key =  Object.entries(this._toRead).find(([_,v])=>v.code === parameterCode);
				delete this._toRead[key[0]];
			}
		});	
	}

	/**
	 * @param {method} e
	 */
	set change(e){
		this.#onChangeEvents.push(e);
	}

	#onLoad(_, statusText){
		if(statusText !== 'success'){
			console.warn(statusText);
			return;
		}
		$('#rcmDacSave').click(()=>this.#sendChange(packetId.saveConfig, 0, 1, groupId.control));
		$('#rcmDacDefault').click(()=>this.#sendChange(packetId.rcmDacDefault, 0, this.parametersClass.parameters['Factory Reset'].code));
		this.#$capabilities = this._$card.find('#capabilities').change(this.#capabilitiesChange.bind(this));
		this.#$rcmProfileValue = this._$card.find('#rcmProfileValue');
		this.#dacController =  new ControllerValue('dacValue', this._$card);
		this.#dacController.change = this.#onValueChange.bind(this);
	}
	#onValueChange(object){
		const toSend = object.dacValue;
		this.#sendChange(packetId.rcmDacSet, toSend, this.parametersClass.parameters.DAC.code);
	}

	#sendChange(){
		this.#onChangeEvents.forEach(cb=>cb(...arguments));
	}
	#createMask(max){
		const n = Math.floor(Math.log2(max)) + 1;
		this.#mask = (1 << n) - 1;
	}
	#capabilitiesChange({currentTarget:{value}}){
		this.#sendChange(packetId.rcmSourceSet, value, this.parametersClass.parameters.Source.code);
	}
}
