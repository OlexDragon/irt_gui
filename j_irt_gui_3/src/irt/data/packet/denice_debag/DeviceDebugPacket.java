package irt.data.packet.denice_debag;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.stream.Stream;

import irt.data.RegisterValue;
import irt.data.packet.PacketAbstract;
import irt.data.packet.PacketImp;
import irt.data.packet.ParameterHeader;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.PacketWork;

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

	public enum Dump{
		INFO,
		DEVICE
	}
	public DeviceDebugPacket(byte linkAddr, int index, Dump dump){
		super(
				linkAddr,
				PacketImp.PACKET_TYPE_REQUEST,
				(short)(dump == Dump.INFO ? PacketWork.DUMPS + index : PacketWork.DEVICES + index),
				PacketImp.GROUP_ID_DEVICE_DEBAG,
				dump == Dump.INFO ? PacketImp.PARAMETER_DEVICE_DEBAG_INFO :PacketImp.PARAMETER_DEVICE_DEBAG_DUMP,
				ByteBuffer.allocate(4).putInt(index).array(),
				Priority.REQUEST);
}

	@Override
	public Object getValue() {

		final Optional<Payload> oPayload = Optional.ofNullable(getPayloads()).map(pls->pls.parallelStream()).orElse(Stream.empty()).findAny();
		Optional<Integer> oSize = oPayload.map(Payload::getParameterHeader).map(ParameterHeader::getSize);

		//buffer size  bigger then register packet return String
		if(oSize.filter(s->s>12).isPresent())
			return '\n' + new String(oPayload.map(pl->pl.getBuffer()).orElse(new byte[]{'N', '/', 'A'})).trim();

		if(oSize.filter(s->s<4).isPresent())
			return "N/A";

		return oPayload.map(pl->pl.getRegisterValue()).orElse(null);
	}

	public String getParsePacketId() {
		final short packetId = getHeader().getPacketId();

		String result;
		if(packetId>=PacketWork.DEVICES) 
			result = "2." + (packetId -PacketWork.DEVICES);
		else
			result = "1." + (packetId -PacketWork.DUMPS);

		return result;
	}
}
