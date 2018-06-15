package irt.data.packet.configuration;

import java.nio.ByteBuffer;

import irt.data.packet.PacketAbstract;
import irt.data.packet.PacketImp;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.PacketWork;
import irt.data.value.ValueFrequency;

public class LOPacket extends PacketAbstract{

	/**
	 *  Converter request packet
	 */
	public LOPacket() {
		this(null);
	}

	/**
	 *  BIAS Board request packet
	 * @param linkAddr
	 */
	public LOPacket(byte linkAddr) {
		this(linkAddr, null);
	}

	/**
	 * BIAS Board command packet
	 * @param linkAddr
	 * @param id - select LO
	 */
	public LOPacket(byte linkAddr, Byte id) {
		super(
				linkAddr,
				id!=null ? PacketImp.PACKET_TYPE_COMMAND : PacketImp.PACKET_TYPE_REQUEST,
						PacketWork.PACKET_ID_CONFIGURATION_LO,
						PacketImp.GROUP_ID_CONFIGURATION,
						linkAddr!=0 ? PacketImp.PARAMETER_ID_CONFIGURATION_LO_SET : PacketImp.PARAMETER_CONFIG_FCM_FREQUENCY,
						id!=null ? PacketImp.toBytes(id) : null,
						id!=null ? Priority.COMMAND : Priority.REQUEST);
	}

	/**
	 * Converter command packet
	 * @param id - select LO
	 */
	public LOPacket(ValueFrequency frequency) {
		super(
				(byte)0,
				frequency!=null ? PacketImp.PACKET_TYPE_COMMAND : PacketImp.PACKET_TYPE_REQUEST,
				PacketWork.PACKET_ID_CONFIGURATION_LO,
				PacketImp.GROUP_ID_CONFIGURATION,
				PacketImp.PARAMETER_CONFIG_FCM_FREQUENCY,
				frequency!=null ? PacketImp.toBytes(frequency.getValue()) : null,
				frequency!=null ? Priority.COMMAND : Priority.REQUEST);
	}

	@Override
	public Object getValue() {
		return getPayloads()
				.stream()
				.findAny()
				.map(Payload::getBuffer)
				.map(ByteBuffer::wrap)
				.map(bb->{
					final int capacity = bb.capacity();
					switch (capacity) {
					case 1:
						return bb.get();

					case 8:
						final long fr = bb.getLong();
						return new ValueFrequency(fr, fr, fr);

					default:
						return bb;
					}
				});
	}
}
