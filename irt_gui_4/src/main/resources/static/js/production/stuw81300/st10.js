import PllRegister from './pll-register.js';

export default class ST10 extends PllRegister {

	static address = 10;
	static mask = 0b111111111111111111;// [26-18] - must be set to ‘0’

	constructor($container, index = PllRegister.index) {
		super($container, index, ST10.address, ST10.mask);

		this.addElement($container.find('#REG_DIG_STARTUP'));
		this.addElement($container.find('#REG_REF_STARTUP'));
		this.addElement($container.find('#REG_RF_STARTUP'));
		this.addElement($container.find('#REG_VCO_STARTUP'));
		this.addElement($container.find('#REG_VCO_4V5_STARTUP'));
		this.addElement($container.find('#REG_DIG_OCP'));
		this.addElement($container.find('#REG_REF_OCP'));
		this.addElement($container.find('#REG_RF_OCP'));
		this.addElement($container.find('#REG_VCO_OCP'));
		this.addElement($container.find('#REG_VCO_4V5_OCP'));
		this.addElement($container.find('#LOCK_DET'));
		this.addElement($container.find('#VCO_SEL_ST10'));
		this.addElement($container.find('#WORD'));
	}
}