import Register from '../../packet/parameter/value/register.js'
import RegisterController from './registe-controllerr.js'

export default class AlarmControl extends RegisterController{

	#$parityError;
	#$tooFewErrors;
	#$tooManyErrors;
	#$addressRangeError;

	constructor($card){
		super($card);
		this.#$parityError = $card.find('#parityError');
		this.#$tooFewErrors = $card.find('#tooFewErrors');
		this.#$tooManyErrors = $card.find('#tooManyErrors');
		this.#$addressRangeError = $card.find('#addressRangeError');
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
		this.#parity(value);
		this.#tooFew(value);
		this.#tooMany(value);
		this.#addressError(value);
	}

	reset(){
		super.reset();
//		const value = this._register.value;
//		this.#revision(value);
//		this.#chipId(value);
//		this.#softReset(value);
//		this.#parity(value);
	}

	#parity(value){
		this.#showError(this.#$parityError, value, 0x8000)
	}

	#tooFew(value){
		this.#showError(this.#$tooFewErrors, value, 0x4000)
	}

	#tooMany(value){
		this.#showError(this.#$tooManyErrors, value, 0x2000)
	}

	#addressError(value){
		this.#showError(this.#$addressRangeError, value, 0x1000)
	}

	#showError($div, value, mask){
		if(value&mask){
			if($div.text()!=='ERROR')
				$div.text('ERROR').addClass('text-bg-danger').removeClass('text-bg-success');
		}else
			if($div.text()!=='NO ERROR')
				$div.text('NO ERROR').removeClass('text-bg-danger').addClass('text-bg-success');
	}
}