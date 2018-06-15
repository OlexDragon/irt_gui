package irt.data.packet.denice_debag;

import irt.data.RegisterValue;
import irt.data.packet.PacketImp;

public class DeviceDebugReadWritePacket extends DeviceDebugPacket {

	public DeviceDebugReadWritePacket(byte linkAddr, RegisterValue registerValue, short packetId) {
		super(linkAddr, registerValue, packetId, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE);
	}

	public DeviceDebugReadWritePacket() {
		this((byte)0, new RegisterValue(0, 0, null), (short)0);
	}
}
