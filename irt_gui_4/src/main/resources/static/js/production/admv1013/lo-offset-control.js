import Register from '../../packet/parameter/value/register.js'
import RegisterController from './registe-controllerr.js'
import FieldController from './field-controler.js'

export default class LoOffsetIControl extends RegisterController{

	#$offsetPositive;
	#$offsetNegative;

	#offsetPosettiveController;
	#offsetNegativeController;

	constructor($card){
		super($card)
		this.#$offsetPositive = $card.find('.offset-positive');
		this.#offsetPosettiveController = new FieldController(this.#$offsetPositive, 0xFE00, 9);
		this.#offsetPosettiveController.change = this.#onChange.bind(this);

		this.#$offsetNegative = $card.find('.offset-negative');
		this.#offsetNegativeController = new FieldController(this.#$offsetNegative, 0x1FC, 2);
		this.#offsetNegativeController.change = this.#onChange.bind(this);
	}


	get register(){
		return super.register;
	}
	/**
     * @param {Register} reg
     */
	set register(reg){
		if(this.equals(reg))
			return;

		super.register = reg;
		const value = reg.value;
		this.#positive(value);
		this.#negative(value);
	}

	reset(){
		super.reset();
		const value = this._register.value;
		this.#positive(value);
		this.#negative(value);
	}

	#positive(val){
		this.#offsetPosettiveController.value = val;
	}

	#negative(val){
	this.#offsetNegativeController.value = val;
	}

	#onChange({mask, value}){
		const val = (this.value&mask)|value;
		this.value = val;
	}
}