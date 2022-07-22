package irt.data.packet.denice_debag;

import irt.data.packet.DeviceDebugPacketIds;
import irt.data.value.Value;

public class DeviceDebugReadWritePacket extends DeviceDebugPacket {

	public DeviceDebugReadWritePacket(byte linkAddr, Value value, DeviceDebugPacketIds packetId) {
		super(linkAddr, value, packetId);
	}

	public DeviceDebugReadWritePacket() {
		this((byte)0, null, DeviceDebugPacketIds.INFO);
	}
}
