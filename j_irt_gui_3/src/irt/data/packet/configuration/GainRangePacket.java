package irt.data.packet.configuration;

import irt.data.Range;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketGroupIDs;
import irt.data.packet.PacketID;
import irt.data.packet.PacketSuper;
import irt.data.packet.interfaces.RangePacket;

public class GainRangePacket extends PacketSuper implements RangePacket {

	public GainRangePacket( byte linkAddr) {
		super(linkAddr,
				PacketImp.PACKET_TYPE_REQUEST,
				PacketID.CONFIGURATION_GAIN_RANGE,
				PacketGroupIDs.CONFIGURATION,
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
