import ControllerValue from './controller-value.js'
import deviceDebug, {parser} from '../packet/parameter/device-debug.js'
import {intArratToBytes} from '../packet/service/converter.js'

export default class Register extends ControllerValue{

	#buisy;
	#packetIdSet

	constructor(name, $card, packetIdGet, packetIdSet){
		super(name, $card, parser, packetIdGet)

		this._packetsInUse.push(packetIdSet);
		this._fields.$index = $card.find(`input.reg-index`).attr('data-input-name', `${name}-indrx`);
		this._fields.$addr = $card.find(`input.reg-addr`).attr('data-input-name', `${name}-addr`);
		this._fields.$min = $card.find(`input.reg-min`).attr('data-input-name', `${name}-min`).change(this.#minChange.bind(this));
		this._fields.$max = $card.find(`input.reg-max`).attr('data-input-name', `${name}-max`).change(this.#maxChange.bind(this));

		if(!(this._fields.$index.length && this._fields.$addr.length && this._fields.$min.length && this._fields.$max.length))
			throw new Error(name + ': There cannot be empty fields.')

		this.#packetIdSet = packetIdSet;
		this.change = this.send.bind(this);
		this._action.packetIDs.push(packetIdSet);
	}

	ready(){
		const index = this._fields.$index.val();
		const addr = this._fields.$addr.val();
		return index && addr;
	}

	send(v){

		const index = this._fields.$index.val();
		const addr = this._fields.$addr.val();
		if(!(index && addr))
			return;

		if(this.#buisy){
			console.log(ithis._fields.$index.prop('id') + ' Buisy');
			return;
		}

		const value = {};
		value.parameterCode = deviceDebug.parameter.readWrite;
		if(v===undefined)
			value.bytes = intArratToBytes(+index, +addr);
		else{
			value.bytes = intArratToBytes(+index, +addr, v);
			value.packetId = this.#packetIdSet;
		}

		super.send(value);
	}

	/**
	 * @param {number[]} v
	 */
	set value(v){
		super.value = v[2];
	}

	#minChange(e){
		const {currentTarget: {value}} = e;
		value!=='' && (super.min = value);
	}

	#maxChange(e){
		const {currentTarget: {value}} = e;
		value!=='' && (super.max = value);
	}
}