import PllRegister from './pll-register.js';

export default class ST11 extends PllRegister {

	static address = 11;
	static mask = 0b111111111111111111111111111; // all bits used

	constructor($container, index = PllRegister.index) {
		super($container, index, ST11.address, ST11.mask);

		this.addElement($container.find('#Device_ID'));
	}
}