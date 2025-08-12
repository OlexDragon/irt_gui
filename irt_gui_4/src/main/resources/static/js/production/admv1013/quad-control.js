import Register from '../../packet/parameter/value/register.js'
import RegisterController from './registe-controllerr.js'
import FieldController from './field-controler.js'

export default class QuadControl extends RegisterController{

	#$regSwitchDifferential;
	#$regLOBandwidth;

	#mageRejectionController;
	#mixerGateVoltageController;

	constructor($card){
		super($card)
		this.#$regSwitchDifferential = $card.find('#regSwitchDifferential');
		this.#mageRejectionController = new FieldController(this.#$regSwitchDifferential, 0x3C0, 6);
		this.#mageRejectionController.change = this.#onChange.bind(this);

		this.#$regLOBandwidth = $card.find('#regLOBandwidth');
		this.#mixerGateVoltageController = new FieldController(this.#$regLOBandwidth, 0xf, 0);
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
		this.#switchDifferential(value);
		this.#lOBandwidth(value);
	}

	_reset(){
		super._reset();
		const value = this._register.value;
		this.#switchDifferential(value);
		this.#lOBandwidth(value);
	}

	#switchDifferential(val){
		this.#mageRejectionController.value = val;
	}

	#lOBandwidth(val){
	this.#mixerGateVoltageController.value = val;
	}

	#onChange({mask, value}){
		const val = (this.value&mask)|value;
		this.value = val;
	}
}