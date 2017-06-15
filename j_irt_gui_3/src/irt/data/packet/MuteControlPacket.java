
package irt.data.packet;

import java.util.Optional;

import irt.data.packet.interfaces.LinkedPacket;
import irt.data.packet.interfaces.PacketWork;

public class MuteControlPacket extends PacketAbstract {

	public static final byte GROUP_ID = PacketImp.GROUP_ID_CONFIGURATION;
	public static final short PACKET_ID = PacketWork.PACKET_ID_CONFIGURATION_MUTE;

	public MuteControlPacket(Byte linkAddr, Byte value) {
		super(
				linkAddr,
				value!=null
					? PacketImp.PACKET_TYPE_COMMAND
					: PacketImp.PACKET_TYPE_REQUEST,
				PACKET_ID,
				GROUP_ID,
				PacketImp.PARAMETER_ID_CONFIGURATION_MUTE,
				value!=null ? PacketImp.toBytes(value) : null,
				value!=null
					? Priority.COMMAND
					: Priority.REQUEST);
	}

	public MuteControlPacket(LinkedPacket packet) {
		super(Optional.ofNullable(packet.getLinkHeader()).map(LinkHeader::getAddr).orElse((byte) 0),
				packet.getHeader().getPacketType(),
				PACKET_ID,
				GROUP_ID,
				packet.getPayload(0).getParameterHeader().getCode(),
				packet.getPayload(0).getBuffer(),
				packet.getHeader().getPacketType()==PacketImp.PACKET_TYPE_COMMAND ? Priority.COMMAND : Priority.REQUEST);

		Optional.of(packet).map(LinkedPacket::getHeader).filter(h->h.getGroupId()==GROUP_ID).filter(h->h.getPacketId()==PACKET_ID).orElseThrow(()->new IllegalArgumentException(packet.toString()));
	}

	@Override
	public Object getValue() {
		return getPayloads()
				.parallelStream()
				.findAny()
				.map(Payload::getByte)
				.map(b->MuteStatus.values()[b]);
	}

	@Override
	public void setValue(Object value) {
		final MuteCommands command = Optional
											.of(value)
											.filter(MuteCommands.class::isInstance)
											.map(MuteCommands.class::cast)
											.orElseThrow(()->new IllegalArgumentException(value.toString()));

		getPayloads()
		.parallelStream()
		.findAny()
		.ifPresent(pl->{
			pl.setBuffer((byte)command.ordinal());
		});
	}

	public enum MuteStatus{
		UNMUTED	(MuteCommands.MUTE),
		MUTED	(MuteCommands.UNMUTE);

		private final MuteCommands comman;
											public MuteCommands getCommand() { return comman; }

		private MuteStatus(MuteCommands command){
			this.comman =command;
		}
	}

	public enum MuteCommands{
		UNMUTE,
		MUTE
	}
}
