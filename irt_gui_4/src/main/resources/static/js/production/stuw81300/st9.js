import PllRegister from './pll-register.js';

export default class ST9 extends PllRegister {

	static address = 9;
	static mask = 0b111111111111111111111111111; // all bits used

	constructor($container, index = PllRegister.index) {
		super($container, index, ST9.address, ST9.mask);

		this.addElement($container.find('#RESERVED_ST9'));
	}
}