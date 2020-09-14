
package irt.data.packet.configuration;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import irt.data.packet.LinkHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketGroupIDs;
import irt.data.packet.PacketIDs;
import irt.data.packet.PacketSuper;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.LinkedPacket;
import irt.data.packet.interfaces.Packet;

public class MuteControlPacket extends PacketSuper {

	public final static Function<Packet, Optional<Object>> parseValueFunction = packet-> Optional
																										.ofNullable(packet)
																										.map(Packet::getPayloads)
																										.map(List::stream)
																										.flatMap(Stream::findAny)
																										.map(Payload::getByte)
																										.map(b->MuteStatus.values()[b]);

	public static final PacketGroupIDs GROUP_ID = PacketGroupIDs.CONFIGURATION;

	public MuteControlPacket(Byte linkAddr, MuteCommands value) {
		super(
				linkAddr,
				Optional.ofNullable(value).map(b->PacketImp.PACKET_TYPE_COMMAND).orElse(PacketImp.PACKET_TYPE_REQUEST),
				PacketIDs.CONFIGURATION_MUTE,
				GROUP_ID,
				getParameterCode(linkAddr),
				Optional.ofNullable(value).map(b->PacketImp.toBytes((byte)value.ordinal())).orElse(null),
				Optional.ofNullable(value).map(b->Priority.COMMAND).orElse(Priority.REQUEST));
	}

	public MuteControlPacket(Packet packet) {
		super(Optional.ofNullable(packet).filter(LinkedPacket.class::isInstance).map(LinkedPacket.class::cast).map(LinkedPacket::getLinkHeader).map(LinkHeader::getAddr).orElse((byte) 0),
				packet.getHeader().getPacketType(),
				PacketIDs.CONFIGURATION_MUTE,
				GROUP_ID,
				packet.getPayload(0).getParameterHeader().getCode(),
				packet.getPayload(0).getBuffer(),
				packet.getHeader().getPacketType()==PacketImp.PACKET_TYPE_COMMAND ? Priority.COMMAND : Priority.REQUEST);

		Optional.of(packet).map(Packet::getHeader).filter(h->GROUP_ID.match(h.getGroupId())).filter(h->PacketIDs.CONFIGURATION_MUTE.match(h.getPacketId())).orElseThrow(()->new IllegalArgumentException(packet.toString()));
	}

	public MuteControlPacket() {
		this((byte)0, null);
	}

	@Override
	public PacketSuper setAddr(byte linkAddr) {

		Optional
		.ofNullable(getPayloads())
		.flatMap(pls->pls.stream().findAny())
		.map(pl->pl.getParameterHeader())
		.ifPresent(ph->ph.setCode(getParameterCode(linkAddr)));

		return super.setAddr(linkAddr);
	}

	private static Byte getParameterCode(Byte linkAddr) {
		return Optional.ofNullable(linkAddr).filter(b->b!=0).map(b->PacketImp.PARAMETER_ID_CONFIGURATION_MUTE).orElse(PacketImp.PARAMETER_CONFIG_FCM_MUTE_CONTROL);
	}

	@Override
	public Object getValue() {
		return parseValueFunction.apply(this);
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
