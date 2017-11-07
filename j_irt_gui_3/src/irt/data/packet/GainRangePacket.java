package irt.data.packet;

import irt.data.Range;
import irt.data.packet.interfaces.PacketWork;

public class GainRangePacket extends PacketAbstract {

	public GainRangePacket( byte linkAddr) {
		super(linkAddr,
				PacketImp.PACKET_TYPE_REQUEST,
				PacketWork.PACKET_ID_CONFIGURATION_GAIN_RANGE,
				PacketImp.GROUP_ID_CONFIGURATION,
				PacketImp.PARAMETER_ID_CONFIGURATION_GAIN_RANGE,
				null,
				Priority.RANGE);
	}

	public GainRangePacket() {
		this((byte)0);
	}

	@Override
	public Object getValue() {
		return getPayloads()
				.stream()
				.findAny()
				.map(Range::new);
	}
}
