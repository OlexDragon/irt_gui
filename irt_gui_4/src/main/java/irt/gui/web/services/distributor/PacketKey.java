package irt.gui.web.services.distributor;

import java.util.Arrays;
import java.util.Objects;

import irt.gui.web.beans.RequestPacket;

final class PacketKey {

	private final String serialPort;
	private final Integer unitAddr;
	private final byte[] bytes;
	private final int hash;

	PacketKey(RequestPacket packet) {

		this.serialPort = packet.getSerialPort();
		this.unitAddr = packet.getUnitAddr();

		byte[] packetBytes = packet.getBytes();
		this.bytes = Arrays.copyOf(packetBytes, packetBytes.length);

		int hash = Objects.hash(serialPort, unitAddr);
		this.hash = 31 * hash + Arrays.hashCode(bytes);
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;

		if (obj == null || getClass() != obj.getClass())
			return false;

		PacketKey other = (PacketKey) obj;

		return other.hash == hash;
	}

	public static PacketKey of(RequestPacket requestPacket) {
		return new PacketKey(requestPacket);
	}
}
