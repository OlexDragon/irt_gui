import PllRegister from './pll-register.js';

export default class ST1 extends PllRegister {

	static address = 1;
	static mask = 0b101111111111111111111111111;// [25] - must be set to ‘0’

	constructor($container, index = PllRegister.index) {
		super($container, index, ST1.address, ST1.mask);

		this.addElement($container.find('#DBR_ST1'));
		this.addElement($container.find('#RF1_OUT_PD'));
		this.addElement($container.find('#MAN_CALB_EN'));
		this.addElement($container.find('#PLL_SEL'));
		this.addElement($container.find('#RF1_SEL'));
		this.addElement($container.find('#FRAC'));
	}
}