import Controller from './controller.js'
import packetId from '../packet/packet-properties/packet-id.js'
import groupId from '../packet/packet-properties/group-id.js'
import deviceDebug from '../packet/parameter/device-debug.js'
import Register from '../packet/parameter/value/register.js'
import ValueControl from './value-control.js'
import packetType from '../packet/packet-properties/packet-type.js'

export default class DACsController extends Controller{

	#action = {packetId: packetId.dacs, groupId: groupId.deviceDebug, data: {packetType: packetType.request, parameterCode: deviceDebug.readWrite.code}, function: 'f_reaction', f_reaction: this.#reaction.bind(this)};
	#actionSet = Object.assign({}, this.#action);
	#onSet;

	#$container;
	#$dacs;
	#$dacSaved;
	#$dacStep;
	#$dacRange
	#$logs;

	#selected;
	#typeName;

	constructor($container){
		super();
		this.#$container = $container;
		$container.load('/fragment/dacs/dacs', this.#onLoad.bind(this));
		this.#actionSet.packetId = packetId.dacsSet;
		this.#actionSet.data = {};
		this.#actionSet.data.packetType = packetType.command;
		this.#actionSet.data.parameterCode = deviceDebug.readWrite.code;
	}

	get action(){
		return this.#action;
	}

	/**
     * @param {string} n
     */
	set name(n){
		super.name = n;
		this.#action.name = n;
	}

	/**
     * @param {(action: any) => void} cb
     */
	set onSet(cb){
		this.#onSet = cb;
	}

	get typeName(){
		return this.typeName;
	}

	set typeName(deviceType){
		switch(deviceType){

		case 'CONVERTER':
		case 'CONVERTER_KA':
			this.#action.data.value = [new Register(1,0), new Register(2,0), new Register(30,0), new Register(30,8)];
			this.#typeName = 'converter'
			break;

		default:
			this.#action.data.value = [new Register(100,1), new Register(100,2), new Register(100,3), new Register(100,4)];
			this.#typeName = 'buc'
		}
	}

	#onLoad(){
		this.#$dacs = this.#$container.find('.dac').focus(this.#focuse.bind(this));
		this.#$dacSaved = this.#$container.find('#dacSaved');
		this.#$dacStep = this.#$container.find('#dacStep').change(this.#onStepChange.bind(this));
		this.#$dacRange = this.#$container.find('#dacRange').prop('min', 0).prop('max', 4095).prop('step', 1);
		this.#$logs = $('#productionLogst');
	}

	#reaction(packet){
		this.#$dacs.prop('disabled', false);
		packet.payloads.forEach(pl=>{
			const reg = Register.parseRegister(pl.data);
			let element
			this.#$dacs.each((_, el)=>{
				const split = el.dataset[this.#typeName].split(',');
				if(reg.index===+split[0] && reg.address===+split[1]){
					element = el;
					return false;
				}
			});
			let now = new Date();
			let hours = now.getHours();
			let minutes = now.getMinutes();
			let seconds = now.getSeconds();

			if(this.#selected?.is($(element))){
				if(this.#selected.value !== reg.value){
					this.#selected.value = reg.value;
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
		this.#selected?.focusOut();
		this.#selected = new ValueControl(this.#$dacSaved, $(el), this.#$dacRange, this.#$dacStep);
		this.#selected.onChange(this.#sendCommand.bind(this));
	}

	#onStepChange({currentTarget:el}){

		const value = el.value;
		if(value){
			let step = parseInt(value);
			const max = +this.#$dacRange.prop('max');
			if(step > max)
				step = max;
			else if(!step || step<1)
				step = 1;
			if(step !== +value)
				el.value = step;
			this.#$dacs.prop('step', step);
		}else
			this.#$dacs.prop('step', false);
	}

	#sendCommand(el){
		const split = el.dataset[this.#typeName].split(',');
		this.#actionSet.data.value = new Register(+split[0], +split[1], +el.value);
		this.#actionSet.update = true;
		this.#onSet(this.#actionSet);
	}
}

