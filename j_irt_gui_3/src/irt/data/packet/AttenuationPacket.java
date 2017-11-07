
package irt.data.packet;

import java.util.Optional;

import irt.data.packet.interfaces.PacketWork;

public class AttenuationPacket extends PacketAbstract {

	public AttenuationPacket(Byte linkAddr, Short value) {
		super(
				linkAddr,
				value!=null
				? PacketImp.PACKET_TYPE_COMMAND
						: PacketImp.PACKET_TYPE_REQUEST,
				PacketWork.PACKET_ID_CONFIGURATION_ATTENUATION,
				PacketImp.GROUP_ID_CONFIGURATION,
				Optional.ofNullable(linkAddr).filter(b->b!=0).map(b->PacketImp.PARAMETER_ID_CONFIGURATION_ATTENUATION).orElse(PacketImp.PARAMETER_CONFIG_FCM_ATTENUATION),
				value!=null ? PacketImp.toBytes(value) : null,
				value!=null
					? Priority.COMMAND
						: Priority.REQUEST);
	}

	public AttenuationPacket() {
		this((byte)0, null);
	}

	@Override
	public Object getValue() {
		return getPayloads()
				.stream()
				.findAny()
				.map(pl->pl.getShort(0));
	}
}
