import Register from '../../packet/parameter/value/register.js';
import packetId from '../../packet/packet-properties/packet-id.js';
import groupId from '../../packet/packet-properties/group-id.js';
import deviceDebug from '../../packet/parameter/device-debug.js';

export default class PLLRegister {

	static indexConverter = 6;
	static indexBias = 127;

	#index;
    #address;
	#mask

	_elements = [];
	#$registerValue;

	#$btnReset;
	#$btnSet;

	#onSetEvent;

	#actionSet = {
		packetId: packetId.stuw81300Set,
		groupId: groupId.deviceDebug,
		data: {parameterCode: deviceDebug.readWrite.code},
		function: 'f_reaction',
		f_reaction: this._reaction.bind(this)
	};

	constructor($container, index, address, mask) {
		this.#mask = mask;
		this.#index = index;
		this.#address = address;
		this.#$registerValue = $container.find('.register-value');
		this.#$btnReset = $container.find('.btn-reset').prop('disabled', true).click(this.#btnResetClick.bind(this));
		this.#$btnSet = $container.find('.btn-set').prop('disabled', true).click(this.#btnSetClick.bind(this));
	}

	get value() {
		return parseInt(this.#$registerValue.text(), 16);
	}

	set value(value) {

		this.#$registerValue.text(toHex(value));

		if (value === this.savedValue){
			this.#disable();
		}else{
			this.#enable();
		}
	}

	#enable(){
		this.#$registerValue.addClass('text-danger');
		this.#$btnReset.prop('disabled', false);
		this.#$btnSet.prop('disabled', false).removeClass('btn-outline-warning').addClass('btn-outline-danger');
	}
	#disable(){
		this.#$registerValue.removeClass('text-danger');
		this.#$btnReset.prop('disabled', true);
		this.#$btnSet.prop('disabled', true).removeClass('btn-outline-danger').addClass('btn-outline-warning');
    }		
	get savedValue() {
		const title = this.#$registerValue.prop('title');
		return title.length ? parseInt(title) : NaN;
	}

	set savedValue(payloads) {

		if(!payloads?.length){
			console.warn('No payloads to set savedValue in PLLRegister');
			return;
		}

		let intValue;
		if(payloads[0] instanceof Register){
			const registers = payloads.find(reg=>reg.index === this.#index && reg.address === this.#address);

			if (!registers){
				console.warn(`No register found for index ${this.#index} address ${this.#address} in PLLRegister`);
				return;
			}

			intValue = registers.value & this.#mask;
        }else{
			let data = payloads
						.map(pl=>pl.data)
						.find(data =>{
							return data[3] === this.#index && data[7] === this.#address;
						});

			if (!data?.length)
				return;

			const register = Register.parseRegister(data);
			intValue = register.value & this.#mask;
	    }

		if (this.savedValue === intValue)
			return;

		this.#$registerValue.prop('title', intValue);
		this.#splitValue(intValue);
		this.value = intValue;
	}

	/**
	 * @param {function(Register)} callback
	 */
	set onSet(callback) {
		this.#onSetEvent = callback;
	}

	addElement($element) {
		this._elements.push($element.change(this.#elementChange.bind(this)));
	}

	_reaction(packet){
//		console.log('Reaction not implemented in PLLRegister', packet);
	}
	#splitValue(intValue) {
		this._elements.forEach($el=>{
			const type = $el.attr('type') ?? $el.prop('tagName');
			switch(type) {

			case 'checkbox': 
				setCheckbox($el, intValue);
				break;

			case 'SELECT':
			case 'number':
				setNumber($el, intValue);
				break;

			default:
				console.warn('Unknown element type: ' + type);
			}
		});
	}

	#elementChange() {
		this.value = this._elements.map(mapToValue).reduce((a, b) => a | b, 0);
	}

	#btnResetClick() {
		this.#splitValue(this.value = this.savedValue);
	}
	#btnSetClick() {
		const register = new Register(this.#index, this.#address, this.value);
		this.#actionSet.data.value = register;
		this.#actionSet.update = true;
		this.#onSetEvent?.(this.#actionSet);
		this.#$btnSet.prop('disabled', true);
	}
}

function toHex(value) {
	return '0x' + value.toString(16).toUpperCase().padStart(8, '0');
}

function setCheckbox($target, value) {
	const mask = parseInt($target.data('mask'), 2);
	$target.prop('checked', (value & mask) > 0);
}

function setNumber($target, value) {

	const mask = parseInt($target.data('mask'), 2);
	const shift = +$target.data('shift');
	const divider = getDivider($target);
	const val = (value & mask) >>> shift;
	$target.val(val*divider);
}

function getDivider($target) {
	const divider = parseFloat($target.prop('step'));
	return divider > 0 ? divider : 1;
}

function mapToValue($el) {

	const type = $el.attr('type') ?? $el.prop('tagName');
	const mask = parseInt($el.data('mask'), 2);

	switch (type) {

		case 'checkbox':
			return $el.prop('checked') ? mask : 0;

		case 'SELECT':
		case 'number':

			const divider = getDivider($el);
			let value = +$el.val();
			if (!value)
				return 0;

			value /= divider;

			const shift = +$el.data('shift');
			const v = value & (mask >>> shift);
			if(value !== v)
				$el.val(v);
				
			return v << shift;

		default:
			console.warn('Unknown element type: ' + type);
			return 0;
	}
}
