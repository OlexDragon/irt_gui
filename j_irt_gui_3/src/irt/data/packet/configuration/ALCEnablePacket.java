
package irt.data.packet.configuration;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import irt.data.packet.PacketImp;
import irt.data.packet.PacketSuper;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.Packet;

public class ALCEnablePacket extends PacketSuper {

	public final static Function<Packet, Optional<Object>> parseValueFunction = packet-> Optional
																										.ofNullable(packet)
																										.map(Packet::getPayloads)
																										.map(List::stream)
																										.flatMap(Stream::findAny)
																										.map(Payload::getByte)
																										.map(b->b==1);

	public ALCEnablePacket(Byte linkAddr, Byte value) {
		super(
				linkAddr,
				Optional.ofNullable(value).map(v->PacketImp.PACKET_TYPE_COMMAND).orElse(PacketImp.PACKET_TYPE_REQUEST),
				PacketIDs.CONFIGURATION_ALC_ENABLE,
				PacketImp.GROUP_ID_CONFIGURATION,
				PacketImp.PARAMETER_CONFIG_BUC_APC_ENABLE,
				Optional.ofNullable(value).map(v->PacketImp.toBytes(value)).orElse(null),
				Optional.ofNullable(value).map(v->Priority.COMMAND).orElse(Priority.REQUEST));
	}
}
