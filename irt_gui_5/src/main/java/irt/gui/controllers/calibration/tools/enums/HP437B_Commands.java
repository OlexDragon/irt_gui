
package irt.gui.controllers.calibration.tools.enums;

import irt.gui.controllers.calibration.tools.prologix.enums.Eos;

public enum HP437B_Commands implements ToolCommands{

	/** Clear all status register */
	CLEAR_REGISTER("*CLS"),
	/** Clear the status byte */
	CLEAR_BYTE("CS"),
	/** Clear sensor table */
	CLEAR_SENSOR("*CT"),
	/** Calibrate */
	CALIBRATE("CL"),
	/** Duty Cycle off */
	CYCLE_OFF("DC0"),
	/** DutyC ycle on */
	CYCLE_ON("DC1"),
	/** Display disable */
	DISPLAY_DISABLE("DD"),
	/** Display enable */
	DISPLAY_ENABLE("DE"),
	/** Automatic filter selection */
	FILTER_AUTOMATIC("FA"),
	/** Enter measurement frequency */
	FREQUENCY_ENTER("FR"),
	/** Ignore GET bus command */
	GET_IGNORE("GT0"),
	/** Trigger immediate response to GET */
	GET_IMMEDIATE("GT1"),
	/** Trigger with delay response to GET */
	GET_DELAY("GT2"),
	/** Identification */
	ID("*ID?"),
	/** Log units(dBm/dB )*/
	UNITS_LOG("LG"),
	/** Linear units(watts/%) */
	UNITS_LINEAR("LN"),
	/** Auto range */
	RANGE("RA"),
	/** Reset */
	RESET("*RST"),
	TRIGGER_HOLD("TR0"),
	TRIGGER_IMMEDIATE("TR1"),
	TRIGGER_WITH_DELAY("TR2"),
	TRIGGER_FREE_RUN("TR3"),
	/** Self test */
	TEST("*TST?"),
	ZERO("ZE");

	private String command;
	private HP437B_Commands(String command){
		this.command = command;
	}

	@Override public byte[] getCommand() {
		return (command + Eos.LF).getBytes();
	}

	@Override public String toString() {
		return name() + "('" + command + "')";
	}

	@Override public void setValue(Object value) {
		throw new UnsupportedOperationException("This function should not be used");
	}

	@Override public Object getValue() {
		return null;
	}
}
