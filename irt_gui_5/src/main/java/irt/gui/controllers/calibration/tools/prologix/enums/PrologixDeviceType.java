package irt.gui.controllers.calibration.tools.prologix.enums;
public enum PrologixDeviceType {

	DEVICE,
	CONTROLLER,
	FOR_BOTH;

	public static PrologixDeviceType valueOf(byte[] arg) {
		return values()[Integer.parseInt(new String(arg).trim())];
	}
}
