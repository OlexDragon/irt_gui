import PllRegister from './pll-register.js';

export default class ST3 extends PllRegister {

	static address = 3;
	static mask = 0b111111111111111111111111111;

	constructor($container, index = PllRegister.index) {
		super($container, index, ST3.address, ST3.mask);

		this.addElement($container.find('#DBR_ST3'));
		this.addElement($container.find('#PD'));
		this.addElement($container.find('#CP_LEAK_x2'));
		this.addElement($container.find('#CP_LEAK'));
		this.addElement($container.find('#CP_LEAK_DIR'));
		this.addElement($container.find('#DNSPLIT_EN'));
		this.addElement($container.find('#PFD_DEL_MODE'));
		this.addElement($container.find('#REF_PATH_SEL'));
		this.addElement($container.find('#R'));
	}
}