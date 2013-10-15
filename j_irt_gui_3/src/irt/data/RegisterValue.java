package irt.data;

import irt.data.value.Value;

public class RegisterValue {

	private int index;
	private int addr;
	private Value value;

	public RegisterValue(int index, int addr, Value value) {
		this.addr = addr;
		this.index = index;
		this.value = value;
	}

	public RegisterValue(RegisterValue rv) {
		this(rv.getIndex(), rv.getAddr(), rv.getValue());
	}

	public int getAddr() {
		return addr;
	}

	public int getIndex() {
		return index;
	}

	public Value getValue() {
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		return obj!=null ?
				obj.getClass().getSimpleName().equals("RegisterValue") ?
						((RegisterValue)obj).getAddr()==addr &&
						((RegisterValue)obj).getIndex() == index &&
						(((RegisterValue)obj).getValue()==value ||
								((RegisterValue)obj).getValue()!=null ? ((RegisterValue)obj).getValue().equals(value) : false) : false : false;
	}

	@Override
	public int hashCode() {
		return addr^index^(int)(value!=null ? value.getValue() : 0);
	}

	@Override
	public String toString() {
		return "RegisterValue [index=" + index + ", addr=" + addr + ", value="
				+ value + "]";
	}

	public void setValue(Value value) {
		this.value = value;
	}

	public void setAddr(int addr) {
		this.addr = addr;
	}

	public void setIndex(int index) {
		this.index = index;
	}
}
