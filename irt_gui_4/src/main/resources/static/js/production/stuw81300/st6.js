import PllRegister from './pll-register.js';

export default class ST6 extends PllRegister {

	static address = 6;
	static mask = 0b111110111111111111111111111;// [21] - must be set to ‘0’

	constructor($container, index = PllRegister.index) {
		super($container, index, ST6.address, ST6.mask);

		this.addElement($container.find('#DITHERING'));
		this.addElement($container.find('#CP_UP_OFF'));
		this.addElement($container.find('#CP_DN_OFF'));
		this.addElement($container.find('#DSM_ORDER'));
		this.addElement($container.find('#EN_AUTOCAL'));
		this.addElement($container.find('#VCO_SEL'));
		this.addElement($container.find('#VCO_WORD'));
		this.addElement($container.find('#CAL_TEMP_COMP'));
		this.addElement($container.find('#PRCHG_DEL'));
		this.addElement($container.find('#CAL_ACC_EN'));
		this.addElement($container.find('#CAL_DIV'));
	}
}