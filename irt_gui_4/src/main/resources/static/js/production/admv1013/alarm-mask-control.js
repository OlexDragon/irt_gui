import Register from '../../packet/parameter/value/register.js'
import RegisterController from './registe-controllerr.js'

export default class AlarmMaskControl extends RegisterController{

	#$regParityMask;
	#$regTooFewErrorsMask;
	#$regTooManyErrorsMask;
	#$regAddressRangeErrorMask;

	constructor($card){
		super($card);
		const bind = this.#onChange.bind(this);
		this.#$regParityMask = $card.find('#regParityMask').change(bind);
		this.#$regTooFewErrorsMask = $card.find('#regTooFewErrorsMask').change(bind);
		this.#$regTooManyErrorsMask = $card.find('#regTooManyErrorsMask').change(bind);
		this.#$regAddressRangeErrorMask = $card.find('#regAddressRangeErrorMask').change(bind);
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
		this.#parityMask(value);
		this.#tooFewErrorsMask(value);
		this.#tooManyErrorsMask(value);
		this.#addressRangeErrorMask(value);
	}

	reset(){
		super.reset();
		const value = this._register.value;
		this.#parityMask(value);
		this.#tooFewErrorsMask(value);
		this.#tooManyErrorsMask(value);
		this.#addressRangeErrorMask(value);
	}

	#parityMask(val){
		this._setChecked(this.#$regParityMask, val, 0x8000);
	}

	#tooFewErrorsMask(val){
		this._setChecked(this.#$regTooFewErrorsMask, val, 0x4000);
	}

	#tooManyErrorsMask(val){
		this._setChecked(this.#$regTooManyErrorsMask, val, 0x2000);
	}

	#addressRangeErrorMask(val){
		this._setChecked(this.#$regAddressRangeErrorMask, val, 0x1000);
	}

	#onChange({currentTarget:{id,checked}}){
		let toSet;
		const val = this.value;
		switch(id){

		case 'regParityMask':
			toSet = checked ? val|0x8000 : val&0x7fff;
			break;

		case 'regTooFewErrorsMask':
			toSet = checked ? val|0x4000 : val&0xbfff;
			break;

		case 'regTooManyErrorsMask':
			toSet = checked ? val|0x2000 : val&0xDfff;
			break;

		case 'regAddressRangeErrorMask':
			toSet = checked ? val|0x1000 : val&0xEfff;
			break;

		default:
			console.log(id);
		}
		this.value = toSet;
	}
}