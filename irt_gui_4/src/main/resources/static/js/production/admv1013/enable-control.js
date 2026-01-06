import Register from '../../packet/parameter/value/register.js'
import RegisterController from './registe-controllerr.js'

export default class EnableControl extends RegisterController{

	#$regVgaPd;
	#$regMixerPd;
	#$regQuadPd;
	#$regBandGapPd;
	#$regIfMode;
	#$regEnvelopeDetector;

	constructor($card){
		super($card);
		const bind = this.#onChange.bind(this);
		this.#$regVgaPd = $card.find('#regVgaPd').change(bind);
		this.#$regMixerPd = $card.find('#regMixerPd').change(bind);
		this.#$regQuadPd = $card.find('#regQuadPd').change(bind);
		this.#$regBandGapPd = $card.find('#regBandGapPd').change(bind);
		this.#$regIfMode = $card.find('#regIfMode').change(bind);
		this.#$regEnvelopeDetector = $card.find('#regEnvelopeDetector').change(bind);
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
		this.#vgaPd(value);
		this.#mixerPd(value);
		this.#quadPd(value);
		this.#bandGapPd(value);
		this.#ifMode(value);
		this.#envelopeDetector(value);
	}

	reset(){
		super.reset();
		const value = this._register.value;
		this.#vgaPd(value);
		this.#mixerPd(value);
		this.#quadPd(value);
		this.#bandGapPd(value);
		this.#ifMode(value);
		this.#envelopeDetector(value);
	}

	#vgaPd(val){
		this._setChecked(this.#$regVgaPd, val, 0x8000);
	}


	#mixerPd(val){
		this._setChecked(this.#$regMixerPd, val, 0x4000);
	}

	#quadPd(val){
		this._setChecked(this.#$regQuadPd, val, 0x3800);
	}

	#bandGapPd(val){
		this._setChecked(this.#$regBandGapPd, val, 0x400);
	}

	#ifMode(val){
		this._setChecked(this.#$regIfMode, val, 0x80);
	}

	#envelopeDetector(val){
		this._setChecked(this.#$regEnvelopeDetector, val, 0x20);
	}

	#onChange({currentTarget:{id,checked}}){
		let toSet;
		const val = this.value;
		switch(id){

		case 'regVgaPd':
			toSet = checked ? val|0x8000 : val&0x7fff;
			break;

		case 'regMixerPd':
			toSet = checked ? val|0x4000 : val&0xbfff;
			break;

		case 'regQuadPd':
			toSet = checked ? val|0x3800 : val&0xC7FF;
			break;

		case 'regBandGapPd':
			toSet = checked ? val|0x400 : val&0xFBFF;
			break;

		case 'regIfMode':
			toSet = checked ? val|0x80 : val&0xFF7F;
			break;

		case 'regEnvelopeDetector':
			toSet = checked ? val|0x20 : val&0xFFDF;
			break;

		default:
			console.log(id);
		}
		this.value = toSet;
	}
}