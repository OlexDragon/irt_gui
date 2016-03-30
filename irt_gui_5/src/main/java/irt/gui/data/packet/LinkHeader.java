package irt.gui.data.packet;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LinkHeader implements Comparable<LinkHeader>{

	public static final int SIZE = 4;

	@JsonProperty
	private byte addr; 		public byte getAddr() { return addr; } 			@JsonIgnore public int getIntAddr() { return addr & 0xFF; } 		public void setAddr(byte addr) { this.addr = addr; }
	@JsonIgnore
	private byte control; 	public byte getControl() { return control; } 	@JsonIgnore public int getIntControl() { return control & 0xFF; }
	@JsonIgnore
	private short protocol; public short getProtocol() { return protocol; } @JsonIgnore public int getIntProtocol() { return protocol & 0xFFFF; }

	public LinkHeader(byte addr, byte control, short protocol) {
		this.addr = addr;
		this.control = control;
		this.protocol = protocol;
	}

	public LinkHeader(byte[] packetInBytes) {
		addr = packetInBytes[0];
		control = packetInBytes[1];
		protocol = (short) Packet.shiftAndAdd(Arrays.copyOfRange(packetInBytes, 2, 4));
	}

	public byte[] toBytes(){
		byte[] data = new byte[4];

		data[0] = addr;
		data[1] = control;
		data[2] = (byte) protocol;
		data[3] = (byte) (protocol>>8);

		return data;
	}

	public static short getProtocol(byte MSB, byte LSB) {
		return (short) ((MSB & 0xFF)<<8 + (LSB & 0xFF));
	}

	@Override
	public String toString() {
		return "\n\tLinkHeader [addr=" + getIntAddr() + ", control=" + getControl() + ", protocol=" + getProtocol() + "]";
	}

	@Override
	public boolean equals(Object obj) {
		return obj!=null ? obj.hashCode()==hashCode() : false;
	}

	@Override
	public int hashCode() {
		return addr+control<<16+protocol<<8;
	}

	@Override
	public int compareTo(LinkHeader linkHeader) {
		return linkHeader!=null ? addr-linkHeader.getAddr() :-1;
	}
}
