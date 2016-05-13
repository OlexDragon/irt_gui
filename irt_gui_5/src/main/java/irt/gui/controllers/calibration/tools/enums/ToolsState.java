package irt.gui.controllers.calibration.tools.enums;

public enum ToolsState {
	OFF,
	ON;

	public static ToolsState valueOf(byte... value){
		String str = new String(value).trim();
		return str.equals("1") ? ON : OFF;
	}
}
