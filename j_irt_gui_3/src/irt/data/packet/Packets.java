package irt.data.packet;

import irt.data.PacketWork;

public enum Packets {

	MEASUREMENT_ALL(new MeasurementPacket());

	private PacketWork packetWork;

	private Packets(PacketWork packetWork){
		this.packetWork = packetWork;
	}

	public PacketWork getPacketWork() {
		return packetWork;
	}
}
