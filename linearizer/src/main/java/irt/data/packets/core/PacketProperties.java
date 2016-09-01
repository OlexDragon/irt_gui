package irt.data.packets.core;

import irt.data.packets.enums.PacketId;

public class PacketProperties {

	public static final int CONVERTER_PACKET_MIN_LENGTH = 7;
	public static final int LINKED_PACKET_MIN_LENGTH = 11;

	private boolean converter;
	private boolean hasAcknowledgment;
	private PacketId packetId;

	public PacketProperties(PacketId packetId) {
		this.packetId = packetId;
	}

	public boolean isConverter() {
		return converter;
	}

	public void setConverter(boolean converter) {
		this.converter = converter;
	}

	public boolean hasAcknowledgment() {
		return hasAcknowledgment;
	}

	public PacketProperties setHasAcknowledgment(boolean hasAcknowledgment) {
		this.hasAcknowledgment = hasAcknowledgment;
		return this;
	}

	@Override
	public String toString() {
		return "PacketProperties [converter=" + converter + ", hasAcknowledgment=" + hasAcknowledgment + "]";
	}

	public int getMinLength() {
		return converter ? CONVERTER_PACKET_MIN_LENGTH : LINKED_PACKET_MIN_LENGTH;
	}

	public int getPacketTypeIndex() {
		return converter ? 0 : 4;
	}

	public int getPacketIdIndex() {
		return converter ? 1 : 5;
	}

	public int getPacketAlarmsIndex() {
		return converter ? 6 : 10;
	}

	public PacketId getPacketId() {
		return packetId;
	}

	public int gerHeadersSize() {
		return converter ? PacketHeader.SIZE : LinkHeader.SIZE+PacketHeader.SIZE;
	}
}
