import PllRegister from './pll-register.js';

export default class ST4 extends PllRegister {

	static address = 4;
	static mask = 0b1100010111101111111111111;// [26:25], [22:20], [18], [13] - must be set to ‘0’

	constructor($container, index = PllRegister.index) {
		super($container, index, ST4.address, ST4.mask);

		this.addElement($container.find('#CALB_3V3_MODE1'));
		this.addElement($container.find('#RF_OUT_3V3'));
		this.addElement($container.find('#EXT_VCO_EN'));
		this.addElement($container.find('#VCO_AMP'));
		this.addElement($container.find('#CALB_3V3_MODE0'));
		this.addElement($container.find('#VCALB_MODE'));
		this.addElement($container.find('#KVCO_COMP_DIS'));
		this.addElement($container.find('#PFD_POL'));
		this.addElement($container.find('#REF_BUFF_MODE'));
		this.addElement($container.find('#MUTE_LOCK_EN'));
		this.addElement($container.find('#LD_ACTIVELOW'));
		this.addElement($container.find('#LD_PREC'));
		this.addElement($container.find('#LD_COUNT'));
	}
}