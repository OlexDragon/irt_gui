package irt.data.packet;

import irt.data.PacketWork;

public class MeasurementPacket extends PacketAbstract{

	/**
	 *  Converter request packet
	 */
	public MeasurementPacket() {
		this((byte)0);
	}

	/**
	 *  BIAS Board request packet
	 * @param linkAddr
	 */
	public MeasurementPacket(byte linkAddr) {
		super(
				linkAddr,
				PacketImp.PACKET_TYPE_REQUEST,
				PacketWork.PACKET_ID_MEASUREMENT_ALL,
				PacketImp.GROUP_ID_MEASUREMENT,
				PacketImp.PARAMETER_ALL,
				null,
				Priority.REQUEST);
	}
}
