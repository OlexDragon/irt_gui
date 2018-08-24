
package irt.data.packet.configuration;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import irt.data.packet.PacketImp;
import irt.data.packet.PacketSuper;
import irt.data.packet.Payload;
import irt.data.packet.PacketImp.PacketGroupIDs;
import irt.data.packet.interfaces.Packet;

public class ConfigurationPacket extends PacketSuper {

	public final static Function<Packet, Optional<Object>> parseValueFunction = packet-> Optional
																										.ofNullable(packet)
																										.map(Packet::getPayloads)
																										.map(List::stream)
																										.flatMap(Stream::findAny)
																										.map(Payload::getBuffer)
																										.map(ByteBuffer::wrap)
																										.map(bb->{
																											switch(bb.remaining()){
																											case 1:
																												return bb.get();
																											case 2:
																												return bb.getShort();
																											case 4:
																												return bb.getInt();
																											case 8:
																												return bb.getLong();
																											default:
																												return bb.array();
																											}
																										});

	public ConfigurationPacket(byte linkAddr, PacketIDs packetIdConfiguration, byte parameterIdConfiguration, byte[] data) {
		super(
				linkAddr,
				data!=null ? PacketImp.PACKET_TYPE_COMMAND : PacketImp.PACKET_TYPE_REQUEST,
						packetIdConfiguration,
						PacketGroupIDs.CONFIGURATION,
						parameterIdConfiguration,
						data,
						data!=null ? Priority.COMMAND : Priority.REQUEST);
	}

	public ConfigurationPacket() {
		this((byte) 0, PacketIDs.UNNECESSARY, (byte) 0, null);
	}

	public ConfigurationPacket(byte linkAddr, PacketIDs configurationPacketId, byte... data) {
		super(
				linkAddr,
				data!=null ? PacketImp.PACKET_TYPE_COMMAND : PacketImp.PACKET_TYPE_REQUEST,
						configurationPacketId,
						PacketGroupIDs.CONFIGURATION,
						configurationPacketId.getParameterCode(),
						data,
						data!=null ? Priority.COMMAND : Priority.REQUEST);
	}
}
