import Controller from './controller.js'
import packetId from '../packet/packet-properties/packet-id.js'
import groupId from '../packet/packet-properties/group-id.js'
import deviceDebug from '../packet/parameter/device-debug.js'
import Register from '../packet/parameter/value/register.js'
import ValueControl from './value-control.js'
import packetType from '../packet/packet-properties/packet-type.js'

export default class DACController extends Controller{

	_action = {name:'DACs', packetId: packetId.dacs, groupId: groupId.deviceDebug, data: {packetType: packetType.request, parameterCode: deviceDebug.readWrite.code}, function: 'f_reaction', f_reaction: this._reaction.bind(this)};
	_actionSet = Object.assign({}, this._action);
	_values = new Map();
	_onSet;

	#$container;
	_$dacs;
	#$dacSaved;
	#$dacStep;
	_$dacRange
	#$logs;

	_selected;
	_typeName;

	constructor($container, url){
		super();
		this.#$container = $container;
		$container.load(url, this.#onLoad.bind(this));
		this._actionSet.packetId = packetId.dacsSet;
		this._actionSet.data = {};
		this._actionSet.data.packetType = packetType.command;
		this._actionSet.data.parameterCode = deviceDebug.readWrite.code;
	}

	get action(){
		return this._action;
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
		case 'CONVERTER_KA':
			this._action.data.value = [new Register(1,0), new Register(2,0), new Register(30,0), new Register(30,8)];
			this._typeName = 'converter'
			break;

		default:
			this._action.data.value = [new Register(100,1), new Register(100,2), new Register(100,3), new Register(100,4)];
			this._typeName = 'buc'
		}
	}

	#onLoad(){
		this._$dacs = this.#$container.find('.dac').focus(this.#focuse.bind(this));
		this.#$dacSaved = this.#$container.find('#dacSaved');
		this.#$dacStep = this.#$container.find('#dacStep').change(this.#onStepChange.bind(this));
		this._$dacRange = this.#$container.find('#dacRange').prop('min', 0).prop('max', 4095).prop('step', 1);
		this.#$logs = $('#productionLogst');
	}

	_reaction(packet){
		packet.payloads.forEach(pl=>{
			const reg = Register.parseRegister(pl.data);
			let element
			this._$dacs.each((_, el)=>{
				const split = el.dataset[this._typeName].split(',');
				if(reg.index===+split[0] && reg.address===+split[1]){
					element = el;
					if (element.hasAttribute('disabled')) {
					  element.removeAttribute('disabled');
					}
					return false;
				}
			});
			let now = new Date();
			let hours = now.getHours();
			let minutes = now.getMinutes();
			let seconds = now.getSeconds();

			if(this._selected?.is($(element))){
				if(this._selected.value !== reg.value){
					this._selected.value = reg.value;
					this.#$logs.append($('<div>', {class: 'row'}).append($('<div>', {class: 'col', text: `${hours}:${minutes}:${seconds}`})).append($('<div>', {class: 'col', text: reg.toString()})));
				}
			}else if (element){
				if(element.value!==`${reg.value}`){
					element.value = reg.value;
					this.#$logs.append($('<div>', {class: 'row'}).append($('<div>', {class: 'col', text: `${hours}:${minutes}:${seconds}`})).append($('<div>', {class: 'col', text: reg.toString()})));
				}
			}else
				console.warn(pl);
		})
	}

	#focuse({currentTarget: el}){
		this._selected?.focusOut();
		this._selected = new ValueControl(this.#$dacSaved, $(el), this._$dacRange, this.#$dacStep);
		this._selected.onChange(this._sendCommand.bind(this));
	}

	#onStepChange({currentTarget:el}){

		const value = el.value;
		if(value){
			let step = parseInt(value);
			const max = +this._$dacRange.prop('max');
			if(step > max)
				step = max;
			else if(!step || step<1)
				step = 1;
			if(step !== +value)
				el.value = step;
			this._$dacs.prop('step', step);
		}else
			this._$dacs.prop('step', false);
	}

	_sendCommand(el){
		const split = el.dataset[this._typeName].split(',');
		this._actionSet.data.value = new Register(+split[0], +split[1], +el.value);
		this._actionSet.update = true;
		this._onSet(this._actionSet);
	}
}

