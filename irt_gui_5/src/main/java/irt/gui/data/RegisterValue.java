package irt.gui.data;

import irt.gui.data.packet.Packet;

public class RegisterValue {

	private int index;
	private int addr;
	private Integer value;

	/** set value to null */
	public RegisterValue(int index, int addr) {
		this(index, addr, null);
	}

	public RegisterValue(int index, int addr, Integer value) {
		this.addr = addr;
		this.index = index;
		this.value = value;
	}

	public int getAddr() {
		return addr;
	}

	public int getIndex() {
		return index;
	}

	public Integer getValue() {
		return value;
	}

	public byte[] toBytes() {
		byte[] i = Packet.toBytes(index);
		byte[] a = Packet.toBytes(addr);
		byte[] v = value!=null ? Packet.toBytes(value) : null;
		return Packet.concatAll(i, a, v);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = prime + addr;
		result = prime * result + index;
		return prime * result + ((value == null) ? 0 : value.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RegisterValue other = (RegisterValue) obj;
		if (addr != other.addr)
			return false;
		if (index != other.index)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "\n\tRegisterValue [index=" + index + ", addr=" + addr + ", value=" + value + "]";
	}
}
