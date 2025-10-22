import PllRegister from './pll-register.js';

export default class ST2 extends PllRegister {

	static address = 2;
	static mask = 0b1100001111111111111111111111;// [22:25] - must be set to ‘0’

	constructor($container, index = PllRegister.index) {
		super($container, index, ST2.address, ST2.mask);

		this.addElement($container.find('#DBR_ST2'));
		this.addElement($container.find('#DSM_CLK_DISABLE'));
		this.addElement($container.find('#RF2_OUT_PD'));
		this.addElement($container.find('#MOD'));
	}
}