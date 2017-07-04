
package irt.data.packet;

import irt.data.packet.interfaces.PacketWork;

public class CallibrationModePacket extends PacketAbstract {

	public CallibrationModePacket(byte linkAddr) {
		super(linkAddr, PacketImp.PACKET_TYPE_REQUEST, PacketWork.PACKET_ID_DEVICE_DEBUG_CALIBRATION_MODE, PacketImp.GROUP_ID_DEVICE_DEBAG, PacketImp.PARAMETER_DEVICE_DEBAG_CALIBRATION_MODE, null, Priority.REQUEST);
	}

	public CallibrationModePacket(byte linkAddr, int value) {
		super(linkAddr, PacketImp.PACKET_TYPE_COMMAND, PacketWork.PACKET_ID_DEVICE_DEBUG_CALIBRATION_MODE, PacketImp.GROUP_ID_DEVICE_DEBAG, PacketImp.PARAMETER_DEVICE_DEBAG_CALIBRATION_MODE, null, Priority.COMMAND);
		setValue(value);
	}
}
