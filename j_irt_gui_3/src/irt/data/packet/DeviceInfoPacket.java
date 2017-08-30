
package irt.data.packet;

import irt.data.packet.interfaces.PacketWork;

public class DeviceInfoPacket extends PacketAbstract {

	public DeviceInfoPacket(byte linkAddr) {
		super(linkAddr, PacketImp.PACKET_TYPE_REQUEST, PacketWork.PACKET_ID_DEVICE_INFO, PacketImp.GROUP_ID_DEVICE_INFO, PacketImp.PARAMETER_ALL, null, Priority.ALARM);
	}
}
