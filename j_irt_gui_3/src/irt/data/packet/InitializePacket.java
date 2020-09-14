
package irt.data.packet;

import java.util.Optional;
import java.util.function.Function;

import irt.data.packet.interfaces.Packet;

public class InitializePacket extends PacketSuper {

	public final static Function<Packet, Optional<Object>> parseValueFunction = packet-> Optional
																										.ofNullable(packet)
																										.map(Packet::getHeader)
																										.map(PacketHeader::getOption)
																										.map(error->error==PacketImp.ERROR_NO_ERROR);

	public InitializePacket(byte linkAddr) {
		super(
				linkAddr,
				PacketImp.PACKET_TYPE_COMMAND,
				PacketIDs.PRODUCTION_GENERIC_SET_1_INITIALIZE,
				PacketGroupIDs.PRODUCTION_GENERIC_SET_1,
				PacketImp.PARAMETER_ID_PRODUCTION_GENERIC_SET_1_DP_INIT,
				null,
				Priority.COMMAND);
	}

	public InitializePacket() {
		this((byte) 0);
	}

	@Override
	public Object getValue() {
		return parseValueFunction.apply(this);
	}
}
