package irt.data.tools.enums;

public enum ToolsState {
	OFF,
	ON;

	/**
	 * 
	 * @param value - byte representing character '1' or '0'
	 * @return 'ToolsState'
	 */
	public static ToolsState valueOf(byte... value){
		String str = new String(value).trim();
		return str.equals("1") ? ON : OFF;
	}
}
