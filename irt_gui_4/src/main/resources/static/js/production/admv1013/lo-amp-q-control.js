import Register from '../../packet/parameter/value/register.js'
import RegisterController from './registe-controllerr.js'
import FieldController from './field-controler.js'

export default class LoAmpQControl extends RegisterController{

	#$regImageRejection;

	#imageRejectionController;

	constructor($card){
		super($card)
		this.#$regImageRejection = $card.find('#regImageRejectionQ');
		this.#imageRejectionController = new FieldController(this.#$regImageRejection, 0x3F80, 7);
		this.#imageRejectionController.change = this.#onChange.bind(this);
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
		this.#imageRejection(value);
	}

	reset(){
		super.reset();
		const value = this._register.value;
		this.#imageRejection(value);
	}

	#imageRejection(val){
		this.#imageRejectionController.value = val;
	}

	#onChange({mask, value}){
		const val = (this.value&mask)|value;
		this.value = val;
	}
}