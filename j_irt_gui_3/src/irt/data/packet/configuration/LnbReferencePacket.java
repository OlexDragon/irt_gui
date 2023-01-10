package irt.data.packet.configuration;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import irt.data.packet.PacketImp;
import irt.data.packet.PacketGroupIDs;
import irt.data.packet.PacketID;
import irt.data.packet.PacketSuper;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.Packet;

public class LnbReferencePacket  extends PacketSuper{

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
																													final int index = bb.get()&3;
																													return Optional
																															.of( ReferenceStatus.values())
																															.map(v->v[index]);
																												});

	private final static PacketID PACKET_ID 	= PacketID.CONFIGURATION_FCM_LNB_REFERENCE;
	private final static PacketGroupIDs GROUP_ID 		= PacketGroupIDs.CONFIGURATION;
	private final static byte PARAMETER 	= PacketImp.PARAMETER_CONFIG_FCM_LNB_REFERENCE_CONTROL;

	public enum ReferenceStatus{
		UNDEFINED,
		ON,
		OFF
	}

	public LnbReferencePacket() {
		super((byte)0, PacketImp.PACKET_TYPE_REQUEST, PacketID.CONFIGURATION_FCM_LNB_REFERENCE, GROUP_ID, PARAMETER, null, Priority.REQUEST);
	}

	public LnbReferencePacket(ReferenceStatus referenceStatus) {
		super((byte)0, PacketImp.PACKET_TYPE_COMMAND, PACKET_ID, GROUP_ID, PARAMETER, new byte[]{(byte) referenceStatus.ordinal()}, Priority.COMMAND);
	}
}
