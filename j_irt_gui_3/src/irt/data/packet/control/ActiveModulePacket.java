package irt.data.packet.control;

import java.util.Optional;

import irt.data.packet.PacketAbstract;
import irt.data.packet.PacketImp;
import irt.data.packet.interfaces.PacketWork;

public class ActiveModulePacket extends PacketAbstract{

	public static final short PACKET_ID = PacketWork.PACKET_ID_ACTIVE_MODULE;

	public ActiveModulePacket(Byte linkAddr, Byte value) {
		super(
				linkAddr,
				Optional.ofNullable(value).map(v->PacketImp.PACKET_TYPE_COMMAND).orElse(PacketImp.PACKET_TYPE_REQUEST),
				PACKET_ID,
				PacketImp.GROUP_ID_CONTROL,
				PacketImp.PACKET_ID_CONFIG_ACTIVE_MODULE_INDEX,
				Optional.ofNullable(value).map(v->new byte[]{v}).orElse(null),
				Optional.ofNullable(value).map(v->Priority.COMMAND).orElse(Priority.REQUEST));
	}

}
