import Register from '../../packet/parameter/value/register.js'
import RegisterController from './registe-controllerr.js'
import FieldController from './field-controler.js'

export default class LoAmpIControl extends RegisterController{

	#$regImageRejection;
	#$regMixerGateVoltage;

	#mageRejectionController;
	#mixerGateVoltageController;

	constructor($card){
		super($card)
		this.#$regImageRejection = $card.find('#regImageRejectionI');
		this.#mageRejectionController = new FieldController(this.#$regImageRejection, 0x3F80, 7);
		this.#mageRejectionController.change = this.#onChange.bind(this);

		this.#$regMixerGateVoltage = $card.find('#regMixerGateVoltage');
		this.#mixerGateVoltageController = new FieldController(this.#$regMixerGateVoltage, 0x7f, 0);
		this.#mixerGateVoltageController.change = this.#onChange.bind(this);
	}

	/**
     * @param {Register} reg
     */
	set register(reg){
		if(this.equals(reg))
			return;

		super.register = reg;
		const value = reg.value;
		this.#imageRejection(value);
		this.#mixerGateVoltage(value);
	}

	_reset(){
		super._reset();
		const value = this._register.value;
		this.#imageRejection(value);
		this.#mixerGateVoltage(value);
	}

	#imageRejection(val){
		this.#mageRejectionController.value = val;
	}

	#mixerGateVoltage(val){
	this.#mixerGateVoltageController.value = val;
	}

	#onChange({mask, value}){
		const val = (this.value&mask)|value;
		this.value = val;
	}
}