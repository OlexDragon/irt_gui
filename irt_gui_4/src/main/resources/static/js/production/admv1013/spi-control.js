import Register from '../../packet/parameter/value/register.js'
import RegisterController from './registe-controllerr.js'

export default class SpiControl extends RegisterController{

	#regRevision;
	#regChipIdl;
	#regSpiSoftReser;
	#regParity;

	constructor($card){
		super($card);
		this.#regRevision = $card.find('#regRevision');
		this.#regChipIdl = $card.find('#regChipIdl');
		const bind = this.#onChange.bind(this);
		this.#regSpiSoftReser = $card.find('#regSpiSoftReser').change(bind);
		this.#regParity = $card.find('#regParity').change(bind);
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
		this.#revision(value);
		this.#chipId(value);
		this.#softReset(value);
		this.#parity(value);
	}

	reset(){
		super.reset();
		const value = this._register.value;
		this.#revision(value);
		this.#chipId(value);
		this.#softReset(value);
		this.#parity(value);
	}

	#revision(val){

		const value = +this.#regRevision.val();
		const toSet = val&0xf;
		if(value === toSet)
			return;

		this.#regRevision.val(toSet);
	}

	#chipId(val){

		const value = +this.#regChipIdl.val();
		const toSet = (val&0xff0)>>4;
		if(value === toSet)
			return;

		this.#regChipIdl.val(toSet);
	}

	#softReset(val){
		const checked = this.#regSpiSoftReser.prop('disabled', false).prop('checked');
		const toSet = (val&0x4000)>0;
		if(checked === toSet)
			return;

		this.#regSpiSoftReser.prop('checked', toSet);
	}

	#parity(val){
		const checked = this.#regParity.prop('disabled', false).prop('checked');
		const toSet = (val&0x8000)>0;
		if(checked === toSet)
			return;

		this.#regParity.prop('checked', toSet);
	}

	#onChange({currentTarget:{id,checked}}){
		let toSet;
		const val = this.value;
		switch(id){

		case 'regSpiSoftReser':
			toSet = checked ? val|0x4000 : val&0xbfff;
			break;

		case 'regParity':
			toSet = checked ? val|0x8000 : val&0x7fff;
			break;

		default:
			console.log(id);
		}
		this.value = toSet;
	}
}