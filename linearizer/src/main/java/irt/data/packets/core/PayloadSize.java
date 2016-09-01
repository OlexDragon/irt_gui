
package irt.data.packets.core;

import irt.data.ToHex;

public class PayloadSize {

	private short size; 
	
	public PayloadSize(short size) {
		this.size = size;
	}

	public short getSize() {
		return size;
	}

	public byte[] toBytes() {
		return Packet.toBytes(size);
	}

	@Override
	public int hashCode() {
		return 31 + size;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof PayloadSize ? obj.hashCode()==hashCode() : false;
	}

	@Override
	public String toString() {
		return "PayloadSize [size=: " + size + "( " + ToHex.bytesToHex(Packet.toBytes(size)) + ")]";
	}
}
