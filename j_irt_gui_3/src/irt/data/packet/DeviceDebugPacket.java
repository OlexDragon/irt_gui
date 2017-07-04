package irt.data.packet;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.stream.Stream;

import irt.data.RegisterValue;

public class DeviceDebugPacket extends PacketAbstract{

	public DeviceDebugPacket(byte linkAddr, RegisterValue registerValue, short packetId, byte parameterId) {
		super(
				linkAddr,
				registerValue.getValue() ==null ? PacketImp.PACKET_TYPE_REQUEST : PacketImp.PACKET_TYPE_COMMAND,
				packetId,
				PacketImp.GROUP_ID_DEVICE_DEBAG,
				parameterId,
				registerValue.toBytes(),
				Priority.REQUEST);
	}

	public DeviceDebugPacket() {
		this((byte)0, new RegisterValue(0, 0, null), (short)0, (byte) 0);
	}

	public DeviceDebugPacket(byte linkAddr, int index, short packetId, byte parameterId){
			super(
					linkAddr,
					PacketImp.PACKET_TYPE_REQUEST,
					packetId,
					PacketImp.GROUP_ID_DEVICE_DEBAG,
					parameterId,
					ByteBuffer.allocate(4).putInt(index).array(),
					Priority.REQUEST);
	}

	@Override
	public Object getValue() {

		final Optional<Payload> findAny = Optional.ofNullable(getPayloads()).map(pls->pls.parallelStream()).orElse(Stream.empty()).findAny();

		if(findAny.map(Payload::getParameterHeader).map(ParameterHeader::getSize).map(s->s>12).orElse(false))
			return '\n' + new String(findAny.map(pl->pl.getBuffer()).orElse(new byte[]{'N', '/', 'A'})).trim();

		return Optional.ofNullable(getPayloads()).map(pls->pls.parallelStream()).orElse(Stream.empty()).findAny().map(pl->pl.getRegisterValue()).orElse(null);
	}
}
