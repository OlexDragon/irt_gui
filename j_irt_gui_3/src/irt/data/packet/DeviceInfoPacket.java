
package irt.data.packet;

import java.util.Optional;
import java.util.function.Function;

import irt.data.DeviceInfo;
import irt.data.packet.interfaces.Packet;

public class DeviceInfoPacket extends PacketSuper {

	public final static Function<Packet, Optional<Object>> parseValueFunction = packet-> Optional
																										.ofNullable(packet)
																										.map(DeviceInfo::parsePacket);

	public DeviceInfoPacket(byte linkAddr) {
		super(linkAddr, PacketImp.PACKET_TYPE_REQUEST, PacketID.DEVICE_INFO, PacketGroupIDs.DEVICE_INFO, PacketImp.PARAMETER_ALL, null, Priority.IMPORTANT);
	}

	public DeviceInfoPacket() {
		this((byte) 0);
	}

	@Override
	public Object getValue() {
		return DeviceInfo.parsePacket(this);
	}
}
