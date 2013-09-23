package irt.data;

public class DacValue {

	private byte dacNumber;
	private short dacValue;

	public DacValue(byte dacNumber, short dacValue) {
		this.dacNumber = dacNumber;
		this.dacValue = dacValue;
	}

	public byte getDacNumber() {
		return dacNumber;
	}

	public short getDacValue() {
		return dacValue;
	}

	public int getIntDacValue() {
		return dacValue & 0xffff;
	}
}
