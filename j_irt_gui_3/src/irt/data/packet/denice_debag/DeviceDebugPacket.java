package irt.data.packet.denice_debag;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.stream.Stream;

import irt.data.packet.PacketImp;
import irt.data.packet.PacketSuper;
import irt.data.packet.ParameterHeader;
import irt.data.packet.Payload;
import irt.data.value.Value;

public class DeviceDebugPacket extends PacketSuper{

	public DeviceDebugPacket(byte linkAddr, Value value, DeviceDebugPacketIds deviceDebugPacketId) {
		super(
				linkAddr,
				value ==null ? PacketImp.PACKET_TYPE_REQUEST : PacketImp.PACKET_TYPE_COMMAND,
				deviceDebugPacketId.getPacketId(),
				Optional.ofNullable(value).map(v->ByteBuffer.allocate(12).put(deviceDebugPacketId.getPayloadData()).putInt((int)v.getValue()).array()).orElse(deviceDebugPacketId.getPayloadData()),
				Priority.REQUEST);
	}

	public DeviceDebugPacket(byte linkAddr, int index, int addr, Integer value, PacketIDs çacketId) {
		super(
				linkAddr,
				value ==null ? PacketImp.PACKET_TYPE_REQUEST : PacketImp.PACKET_TYPE_COMMAND,
				çacketId,
				Optional.ofNullable(value).map(v->ByteBuffer.allocate(12).putInt(index).putInt(addr).putInt(v).array()).orElse(ByteBuffer.allocate(8).putInt(index).putInt(addr).array()),
				Priority.REQUEST);
	}

	public DeviceDebugPacket() {
		this((byte)0, DeviceDebugPacketIds.INFO);
	}

	public DeviceDebugPacket(byte linkAddr, DeviceDebugPacketIds deviceDebugPacketId){
			super(
					linkAddr,
					PacketImp.PACKET_TYPE_REQUEST,
					deviceDebugPacketId.getPacketId(),
					deviceDebugPacketId.getPayloadData(),
					Priority.REQUEST);
	}

	public enum DeviceDebugType{
		INFO(PacketImp.PARAMETER_DEVICE_DEBUG_INFO),
		DEVICE(PacketImp.PARAMETER_DEVICE_DEBUG_DUMP);

		private final byte parameterCode;

		private DeviceDebugType(byte parameter){
			this.parameterCode = parameter;
		}

		public byte getParameterCode() {
			return parameterCode;
		}
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
		final int intId = getHeader().getPacketId()&0xFF;
		final PacketIDs[] values = PacketIDs.values();
		PacketIDs packetId = Optional.of(intId).filter(i->i<values.length).map(i->values[i]).orElse(PacketIDs.UNNECESSARY);


		return packetId.toString();
	}
}
