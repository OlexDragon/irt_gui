package irt.data.packet.configuration;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import irt.data.Range;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketSuper;
import irt.data.packet.PacketImp.PacketGroupIDs;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.interfaces.RangePacket;

public class AttenuationRangePacket extends PacketSuper implements RangePacket{

	public final static Function<Packet, Optional<Object>> parseValueFunction = packet-> Optional
																										.ofNullable(packet)
																										.map(Packet::getPayloads)
																										.map(List::stream)
																										.flatMap(Stream::findAny)
																										.map(Range::new);

	public AttenuationRangePacket( byte linkAddr) {
		super(linkAddr,
				PacketImp.PACKET_TYPE_REQUEST,
				PacketIDs.CONFIGURATION_ATTENUATION_RANGE,
				PacketGroupIDs.CONFIGURATION,
				PacketImp.PARAMETER_CONFIG_ATTENUATION_RANGE,
				null,
				Priority.RANGE);
	}

	public AttenuationRangePacket() {
		this((byte)0);
	}

	@Override
	public Object getValue() {
		return parseValueFunction.apply(this);
	}
}
