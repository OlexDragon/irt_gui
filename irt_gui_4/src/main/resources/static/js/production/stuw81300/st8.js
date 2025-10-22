import PllRegister from './pll-register.js';

export default class ST8 extends PllRegister {

	static address = 8;
	static mask = 0b100000011110111011101110111;// [25-20], [15], [11], [7], [3] - must be set to ‘0’

	constructor($container, index = PllRegister.index) {
		super($container, index, ST8.address, ST8.mask);

		this.addElement($container.find('#PD_RF2_DISABLE'));
		this.addElement($container.find('#REG_OCP_DIS'));
		this.addElement($container.find('#REG_DIG_PD'));
		this.addElement($container.find('#REG_DIG_VOUT'));
		this.addElement($container.find('#REG_REF_PD'));
		this.addElement($container.find('#REG_REF_VOUT'));
		this.addElement($container.find('#REG_RF_PD'));
		this.addElement($container.find('#REG_RF_VOUT'));
		this.addElement($container.find('#REG_VCO_PD'));
		this.addElement($container.find('#REG_VCO_VOUT'));
		this.addElement($container.find('#REG_VCO_4V5_PD'));
		this.addElement($container.find('#REG_VCO_4V5_VOUT'));
	}
}