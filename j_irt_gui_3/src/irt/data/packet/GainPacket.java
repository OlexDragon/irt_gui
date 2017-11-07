package irt.data.packet;

import java.util.Optional;

import irt.data.packet.interfaces.PacketWork;

public class GainPacket extends PacketAbstract {

	public GainPacket(Byte linkAddr, Short value) {
		super(
				linkAddr,
				Optional.ofNullable(value).map(v->PacketImp.PACKET_TYPE_COMMAND).orElse(PacketImp.PACKET_TYPE_REQUEST),
				PacketWork.PACKET_ID_CONFIGURATION_GAIN,
				PacketImp.GROUP_ID_CONFIGURATION,
				Optional.ofNullable(linkAddr).filter(b->b!=0).map(b->PacketImp.PARAMETER_ID_CONFIGURATION_GAIN).orElse(PacketImp.PARAMETER_CONFIG_FCM_GAIN),
				Optional.ofNullable(value).map(v->PacketImp.toBytes(value)).orElse(null),
				Optional.ofNullable(value).map(v->Priority.COMMAND).orElse(Priority.REQUEST));
	}

	public GainPacket() {
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
