package irt.data.packet;

import irt.data.packet.interfaces.PacketWork;

public enum Packets {

	MEASUREMENT_ALL	(new MeasurementPacket()),
	ALARM_ID		(new AlarmsIDsPacket((byte) 0)),
	ALARMS_SUMMARY_STATUS(new AlarmsSummaryPacket((byte) 0));

	private PacketWork packetWork;

	private Packets(PacketWork packetWork){
		this.packetWork = packetWork;
	}

	public PacketWork getPacketWork() {
		return packetWork;
	}
}
