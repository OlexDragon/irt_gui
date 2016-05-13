package irt.gui.controllers.flash.enums;

public enum UnitAddress {
	PROGRAM("PROGRAM"		, 0x08000000),
	CONVERTER("CONVERTER"	, 0x080C0000),
	BIAS("BIAS BOARD"		, 0x080E0000),
	HP_BIAS("HP BIAS"		, 0x081E0000),
	ORPC("ORPC"				, 0x081C0000);

	private String text;
	private int addr;

	private UnitAddress(String name, int addr) {
		this.text = name;
		this.addr = addr;
	}

	public int getAddr() {
		return addr;
	}

	@Override
	public String toString() {
		return text;
	}
}
