import Controller from './controller.js'
import packetId from '../packet/packet-properties/packet-id.js'
import groupId from '../packet/packet-properties/group-id.js'
import deviceDebug from '../packet/parameter/device-debug.js'
import Register from '../packet/parameter/value/register.js'
import ValueControl from './value-control.js'
import packetType from '../packet/packet-properties/packet-type.js'
import {calMode} from './cal-mode.js'

export default class DACController extends Controller{

	_action = {name:'DACs', packetId: packetId.dacs, groupId: groupId.deviceDebug, data: {packetType: packetType.request, parameterCode: deviceDebug.readWrite.code}, function: 'f_reaction', f_reaction: this._reaction.bind(this)};
	_actionSet = Object.assign({}, this._action);
	_values = new Map();
	_onSet;

	_$container;
	_$dacs;
	#$savedValue;
	#$valueStep;
	_$valueRange
	#$logs;

	_selected;
	_typeName;

	constructor($container, url){
		super();
		this._$container = $container;
		$container.load(url, this._onLoad.bind(this));
		this._actionSet.packetId = packetId.dacsSet;
		this._actionSet.data = {};
		this._actionSet.data.packetType = packetType.command;
		this._actionSet.data.parameterCode = deviceDebug.readWrite.code;
	}

	get action(){
		return this._action;
	}

	get name(){
		return super.name;
	}

	/**
     * @param {string} n
     */
	set name(n){
		super.name = n;
		this._action.name = n;
	}

	/**
     * @param {(action: any) => void} cb
     */
	set onSet(cb){
		this._onSet = cb;
	}

	get typeName(){
		return this._typeName;
	}

	set typeName(deviceType){
		switch(deviceType){

		case 'CONVERTER':
			this._action.data.value = [new Register(1,0), new Register(2,0), new Register(3,0), new Register(4,8)];
			this._typeName = 'converter'
			this._action.timeout = 3000;
			break;

		case 'CONVERTER_KA':
			this._action.data.value = [new Register(1,0), new Register(2,0), new Register(30,0), new Register(30,8)];
			this._typeName = 'converter'
			this._action.timeout = 3000;
			break;

		case 'BAIS_LOW_POWER':
			this._action.data.value = [new Register(100,1), new Register(100,2)];
			this._typeName = 'buc'
			break;

		default:
			this._action.data.value = [new Register(100,1), new Register(100,2), new Register(100,3), new Register(100,4)];
			this._typeName = 'buc'
		}

		this._setInputsData();
	}

	_setInputsData(){
		if(!this._$dacs || !this._action.data.value)
			return;
		this._$dacs.each((_, el)=>{
			const index = el.id.replace(/[^0-9]/g, '') - 1;
			const register = this._action.data.value[index];
			el.setAttribute("data-register", `${register.index},${register.address}`);
		});
	}
	_onLoad(){
		this._$dacs = this._$container.find('.dac').focus(this.#focuse.bind(this));
		this.#$savedValue = this._$container.find('#savedValue');
		this.#$valueStep = this._$container.find('#valueStep').change(this.#onStepChange.bind(this));
		this._$valueRange = this._$container.find('#valueRange').prop('min', 0).prop('max', 4095).prop('step', 1);
		this.#$logs = $('#productionLogst');
		const valueStep = Cookies.get('dac-step');
		if(valueStep)
			this.#$valueStep.val(valueStep).change();
		this._setInputsData();
	}

	_reaction(packet){
		console.log('DACController _reaction calMode:', calMode);
		packet.payloads.forEach(pl=>{
			const reg = Register.parseRegister(pl.data);
			let element
			this._$dacs.each((_, el)=>{
				const {dataset:{register}} = el;
				if(!register){
					console.warn(el.id, 'DACController _reaction: no data-register attribute');
					return;
				}
				const split = register.split(',');
				if(reg.index===+split[0] && reg.address===+split[1]){
					element = el;
					this._disableElement(el, !calMode);

					return false;
				}
			});
//			const now = new Date();
//			const hours = now.getHours();
//			const minutes = now.getMinutes();
//			const seconds = now.getSeconds();

			if(this._selected?.is($(element))){
				if(this.#$savedValue.val()!==`${reg.value}`)
					this.#$savedValue.val(reg.value);
				if(this._selected.value !== reg.value){
					this._selected.value = reg.value;
//					this.#$logs.append($('<div>', {class: 'row'}).append($('<div>', {class: 'col', text: `${hours}:${minutes}:${seconds}`})).append($('<div>', {class: 'col', text: reg.toString()})));
				}
			}else if (element){
				if(element.value!==`${reg.value}`){
					element.value = reg.value;
//					this.#$logs.append($('<div>', {class: 'row'}).append($('<div>', {class: 'col', text: `${hours}:${minutes}:${seconds}`})).append($('<div>', {class: 'col', text: reg.toString()})));
				}
			}else
				console.warn(pl);
		});
	}

	_disableElement(el, disable){
		disable ? el.setAttribute('disabled', 'disabled') : el.removeAttribute('disabled');
	}
	
	#focuse({currentTarget: el}){
		console.log('DAC focuse:', el.id);
		this._selected?.focusOut();
		this._selected = new ValueControl(this.#$savedValue, $(el), this._$valueRange, this.#$valueStep);
		this._selected.onChange(this._sendCommand.bind(this));
	}

	#onStepChange({currentTarget:el}){

		const value = el.value;
		if(value){
			let step = parseInt(value);
			const max = +this._$valueRange.prop('max');
			if(step > max)
				step = max;
			else if(!step || step<1)
				step = 1;
			if(step !== +value)
				el.value = step;
			this._$dacs.prop('step', step);
		}else
			this._$dacs.prop('step', false);
		Cookies.set('dac-step', el.value, {expires: 365, path: '/production'});
	}

	_sendCommand(el){
		const split = el.dataset.register.split(',');
		this._actionSet.data.value = new Register(+split[0], +split[1], +el.value);
		this._actionSet.update = true;
		this._onSet(this._actionSet);
	}
}

