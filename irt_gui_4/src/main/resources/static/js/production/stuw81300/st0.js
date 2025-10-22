import PllRegister from './pll-register.js';

export default class ST0 extends PllRegister {

	static address = 0;
	static mask = 0b11111110011111111111111111;// [26], [18], [17] - must be set to ‘0’

	constructor($container, index = PllRegister.index) {
		super($container, index, ST0.address, ST0.mask);

		this.addElement($container.find('#VCO_CALB_DISABLE'));
		this.addElement($container.find('#CP_SEL'));
		this.addElement($container.find('#PFD_DEL'));
		this.addElement($container.find('#N'));
	}
}