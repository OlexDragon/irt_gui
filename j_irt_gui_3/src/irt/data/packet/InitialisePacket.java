
package irt.data.packet;

import java.util.Optional;
import java.util.function.Function;

import irt.data.DeviceInfo;
import irt.data.packet.interfaces.Packet;

public class InitialisePacket extends PacketSuper {

	public final static Function<Packet, Optional<Object>> parseValueFunction = packet-> Optional
																										.ofNullable(packet)
																										.map(DeviceInfo::parsePacket);

	public InitialisePacket(byte linkAddr) {
		super(linkAddr, PacketImp.PACKET_TYPE_REQUEST, PacketID.DEVICE_DEBUG_CALIBRATION_MODE, PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_ALL, null, Priority.IMPORTANT);
	}

	public InitialisePacket() {
		this((byte) 0);
	}

	@Override
	public Object getValue() {
		return DeviceInfo.parsePacket(this);
	}
}
