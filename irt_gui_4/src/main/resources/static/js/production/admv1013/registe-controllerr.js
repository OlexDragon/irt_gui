export default class RegisterController{

	_register;
	#$card;
	#$register
	#btnSet;
	#btnReset;
	#callBak;

	constructor($card){
		this.#$card = $card;
		this.#$register = $card.find('.register').change(this.#onChange.bind(this));
		this.#btnSet =  $card.find('.btn-set').click(this.save.bind(this));
		this.#btnReset =  $card.find('.btn-reset').click(this.reset.bind(this));
	}

	get register(){
		return this._register;
	}
	set register(reg){
		this.#$register.val(reg.value).prop('disabled', false);
		this._register = reg;
	}

	get value(){
		return +this.#$register.val();
	}
	set value(val){
		this.#$register.val(val).change();
	}

	equals(reg){

		if(!this._register)
			return false;

		if((reg.index!==this._register.index || reg.address!==this._register?.address))
			throw new Error('Wrong register ' + reg);

		return reg.value === this._register.value;
	}

	/**
     * @param {Function} cb
     */
	set onSave(cb){
		this.#callBak = cb;
	}

	_setChecked($checkBox, value, mask){
		const checked = $checkBox.prop('disabled', false).prop('checked');
		const toSet = (value&mask)>0;
		if(checked === toSet)
			return;

		$checkBox.prop('checked', toSet);
	}

	save(){
		this.#btnSet.prop('disabled', true);
		this.#btnReset.prop('disabled', true);
		this._register.value = this.value;
		this.#callBak(this._register);
		this.#$card.removeClass('connection-wrong');
	}

	reset(){
		this.#$register.val(this._register.value).change();
	}

	#onChange({currentTarget:{value}}){
		const val = +value
		const rVal = this._register.value;
		const disable = val===rVal;
		this.#btnSet.prop('disabled', disable);
		this.#btnReset.prop('disabled', disable);
		if(disable)
			this.#$card.removeClass('connection-wrong');
		else
			this.#$card.addClass('connection-wrong');
	}
}