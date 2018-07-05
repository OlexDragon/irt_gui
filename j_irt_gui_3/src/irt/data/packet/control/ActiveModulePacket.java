package irt.data.packet.control;

import java.util.Optional;

import irt.data.packet.PacketSuper;
import irt.data.packet.PacketImp;

public class ActiveModulePacket extends PacketSuper{

	public ActiveModulePacket(Byte linkAddr, Byte value) {
		super(
				linkAddr,
				Optional.ofNullable(value).map(v->PacketImp.PACKET_TYPE_COMMAND).orElse(PacketImp.PACKET_TYPE_REQUEST),
				PacketIDs.CONTROL_ACTIVE_MODULE,
				PacketImp.GROUP_ID_CONTROL,
				PacketImp.PACKET_ID_CONFIG_ACTIVE_MODULE_INDEX,
				Optional.ofNullable(value).map(v->new byte[]{v}).orElse(null),
				Optional.ofNullable(value).map(v->Priority.COMMAND).orElse(Priority.REQUEST));
	}

}
