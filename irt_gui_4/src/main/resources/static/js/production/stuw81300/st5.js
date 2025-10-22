import PllRegister from './pll-register.js';

export default class ST5 extends PllRegister {

	static address = 5;
	static mask = 0b10101;// [26:5], [3], [1] - must be set to ‘0’

	constructor($container, index = PllRegister.index) {
		super($container, index, ST5.address, ST5.mask);

		this.addElement($container.find('#RF2_OUTBUF_LP'));
		this.addElement($container.find('#DEMUX_LP'));
		this.addElement($container.find('#REF_BUFF_LP'));
	}
}