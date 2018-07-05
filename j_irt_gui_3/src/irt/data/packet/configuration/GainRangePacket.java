package irt.data.packet.configuration;

import irt.data.Range;
import irt.data.packet.PacketSuper;
import irt.data.packet.PacketImp;
import irt.data.packet.interfaces.RangePacket;

public class GainRangePacket extends PacketSuper implements RangePacket {

	public GainRangePacket( byte linkAddr) {
		super(linkAddr,
				PacketImp.PACKET_TYPE_REQUEST,
				PacketIDs.CONFIGURATION_GAIN_RANGE,
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
