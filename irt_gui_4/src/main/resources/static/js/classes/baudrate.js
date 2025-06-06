export default class Baudrate{

	#$baudrate;

	constructor($baudrate){
		this.#$baudrate = $baudrate.change(this.#onChange);

		const ub = Cookies.get('unitBaudrate');
		if(ub)
			this.#$baudrate.val(ub);
		else
			this.#$baudrate.val(115200);
	}

	get baudrate(){
		return +this.#$baudrate.val();
	}

	set baudrate(v){
		this.#$baudrate.val(v.toString()).change();
	}

	#onChange(e){
		Cookies.set('unitBaudrate', e.currentTarget.value, {expires: 365, path: ''});
	}
}