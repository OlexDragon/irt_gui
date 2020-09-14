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

public class SpectrumInversionPacket  extends PacketSuper{

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
																													final int index = bb.get()&1;
																													return Optional
																															.of( Spectrum.values())
																															.map(v->v[index]);
																												});

	public enum Spectrum{
		INVERTED,
		NOT_INVERTED
	}

	public SpectrumInversionPacket() {
		super((byte)0, PacketImp.PACKET_TYPE_REQUEST, PacketIDs.CONFIGURATION_SPECTRUM_INVERSION, PacketGroupIDs.CONFIGURATION, PacketImp.PARAMETER_ID_CONFIGURATION_SPECTRUM_INVERSION, null, Priority.REQUEST);
	}

	public SpectrumInversionPacket(Spectrum spectrum) {
		super((byte)0, PacketImp.PACKET_TYPE_COMMAND, PacketIDs.CONFIGURATION_SPECTRUM_INVERSION, PacketGroupIDs.CONFIGURATION, PacketImp.PARAMETER_ID_CONFIGURATION_SPECTRUM_INVERSION, new byte[]{(byte) (spectrum==Spectrum.INVERTED ? 1 : 2)}, Priority.COMMAND);
	}
}
