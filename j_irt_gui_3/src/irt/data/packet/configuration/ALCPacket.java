
package irt.data.packet.configuration;

import java.util.Optional;

import irt.data.packet.PacketSuper;
import irt.data.packet.PacketGroupIDs;
import irt.data.packet.PacketIDs;
import irt.data.packet.PacketImp;

public class ALCPacket extends PacketSuper {

	public ALCPacket(Byte linkAddr, Short value) {
		super(
				linkAddr,
				Optional.ofNullable(value).map(v->PacketImp.PACKET_TYPE_COMMAND).orElse(PacketImp.PACKET_TYPE_REQUEST),
				PacketIDs.CONFIGURATION_ALC_LEVEL,
				PacketGroupIDs.CONFIGURATION,
				PacketImp.PARAMETER_CONFIG_BUC_APC_LEVEL,
				Optional.ofNullable(value).map(v->PacketImp.toBytes(value)).orElse(null),
				Optional.ofNullable(value).map(v->Priority.COMMAND).orElse(Priority.REQUEST));
	}
}
