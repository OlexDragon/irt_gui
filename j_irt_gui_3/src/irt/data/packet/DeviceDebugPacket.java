package irt.data.packet;

import irt.data.RegisterValue;

public class DeviceDebugPacket extends PacketAbstract{

	public DeviceDebugPacket(byte linkAddr, RegisterValue registerValue, short packetIdDeviceDebug, byte parameterId) {
		super(
				linkAddr,
				PacketImp.PACKET_TYPE_REQUEST,
				packetIdDeviceDebug,
				PacketImp.GROUP_ID_DEVICE_DEBAG,
				parameterId,
				registerValue.toBytes(),
				Priority.REQUEST);
	}
}
