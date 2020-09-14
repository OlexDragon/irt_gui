package irt.data.packet.configuration;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import irt.data.packet.PacketImp;
import irt.data.packet.PacketGroupIDs;
import irt.data.packet.PacketIDs;
import irt.data.packet.PacketSuper;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.Packet;

public class LnbPowerPacket  extends PacketSuper{

	public final static Function<Packet, Optional<Object>> parseValueFunction = packet-> Optional
																										.ofNullable(packet)
																										.map(Packet::getPayloads)
																										.map(List::stream)
																										.flatMap(Stream::findAny)
																										.map(Payload::getBuffer)
																										.map(ByteBuffer::wrap)
																										.filter(bb->bb.remaining()==1)
																										.flatMap(
																												bb->{
																													final int index = bb.get()&7;
																													return Optional
																															.of( PowerStatus.values())
																															.map(v->v[index]);
																												});

	private static final PacketIDs PACKET_ID 	= PacketIDs.CONFIGURATION_FCM_LNB_POWER;
	private static final PacketGroupIDs GROUP_ID 		= PacketGroupIDs.CONFIGURATION;
	private static final byte PARAMETER 	= PacketImp.PARAMETER_CONFIG_LNB_POWER;

	public enum PowerStatus{
		UNDEFINED0,
		UNDEFINED1,
		ON,
		OFF
	}

	public LnbPowerPacket() {
		super((byte)0, PacketImp.PACKET_TYPE_REQUEST, PACKET_ID, GROUP_ID, PARAMETER, null, Priority.REQUEST);
	}

	public LnbPowerPacket(PowerStatus powerStatus) {
		super((byte)0, PacketImp.PACKET_TYPE_COMMAND, PACKET_ID, GROUP_ID, PARAMETER, new byte[]{(byte) powerStatus.ordinal()}, Priority.COMMAND);
	}
}
