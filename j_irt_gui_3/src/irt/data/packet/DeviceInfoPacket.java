
package irt.data.packet;

import irt.data.DeviceInfo;

public class DeviceInfoPacket extends PacketAbstract {

	public DeviceInfoPacket(byte linkAddr) {
		super(linkAddr, PacketImp.PACKET_TYPE_REQUEST, PacketIDs.DEVICE_INFO.getId(), PacketImp.GROUP_ID_DEVICE_INFO, PacketImp.PARAMETER_ALL, null, Priority.IMPORTANT);
	}

	public DeviceInfoPacket() {
		this((byte) 0);
	}

	@Override
	public Object getValue() {
		return DeviceInfo.parsePacket(this);
	}
}
