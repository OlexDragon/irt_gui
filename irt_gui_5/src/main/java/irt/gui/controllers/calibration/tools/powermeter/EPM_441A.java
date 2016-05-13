package irt.gui.controllers.calibration.tools.powermeter;

import irt.gui.controllers.calibration.tools.Tools;
import irt.gui.data.value.ValueDouble;

public class EPM_441A extends Tools{

	public enum SCPILanguage{
		MEASURE("*cls;meas1?");

		private String command;
		private SCPILanguage(String command){
			this.command = command;
		}

		public byte[] getCommand(){
			return command.getBytes();
		}
	}

	private byte addr = 13;
	private ValueDouble power = new ValueDouble(0, 3);

	@Override
	public byte getAddr() {
		return addr;
	}

	public void setAddr(byte addr) {
		this.addr = addr;
	}

	public ValueDouble getPower() {
		return power;
	}

	public void setPower(String value) {
		power.setValue(value);
	}
}
