import Register from '../../packet/parameter/value/register.js'
import RegisterController from './registe-controllerr.js'
import FieldController from './field-controler.js'

export default class TemperatureControl extends RegisterController{

	#$regTemperatureCompensation;

	#temperatureController;

	constructor($card){
		super($card);
		this.#$regTemperatureCompensation = $card.find('#regTemperatureCompensation');
		this.#temperatureController = new FieldController(this.#$regTemperatureCompensation, 0xffff, 0);
		this.#temperatureController.change = this.#onChange.bind(this);
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
		this.#temperature(value);
	}

	reset(){
		super.reset();
		const value = this._register.value;
		this.#temperature(value);
	}

	#temperature(val){
		this.#temperatureController.value = val;
	}

	#onChange({mask, value}){
		const val = (this.value&mask)|value;
		this.value = val;
	}
}