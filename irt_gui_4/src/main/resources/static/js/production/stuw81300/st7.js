import PllRegister from './pll-register.js';

export default class ST7 extends PllRegister {

	static address = 7;
	static mask =  0b11111111111111111111111111;// [26] - must be set to ‘0’

	constructor($container, index = PllRegister.index) {
		super($container, index, ST7.address, ST7.mask);

		this.addElement($container.find('#LD_SDO_tristate'));
		this.addElement($container.find('#LD_SDO_MODE'));
		this.addElement($container.find('#SPI_DATA_OUT_DISABLE'));
		this.addElement($container.find('#LD_SDO_SEL'));
		this.addElement($container.find('#REGDIG_OCP_DIS'));
		this.addElement($container.find('#CYCLE_SLIP_EN'));
		this.addElement($container.find('#FSTLCK_EN'));
		this.addElement($container.find('#CP_SEL_FL'));
		this.addElement($container.find('#FSTLCK_CNT'));
	}
}