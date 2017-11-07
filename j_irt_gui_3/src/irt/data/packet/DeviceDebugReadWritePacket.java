package irt.data.packet;

import irt.data.RegisterValue;

public class DeviceDebugReadWritePacket extends DeviceDebugPacket {

	public DeviceDebugReadWritePacket(byte linkAddr, RegisterValue registerValue, short packetId) {
		super(linkAddr, registerValue, packetId, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE);
	}

	public DeviceDebugReadWritePacket() {
		this((byte)0, new RegisterValue(0, 0, null), (short)0);
	}
}
