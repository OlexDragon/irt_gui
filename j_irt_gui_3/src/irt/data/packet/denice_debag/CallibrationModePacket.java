
package irt.data.packet.denice_debag;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import irt.data.packet.PacketImp;
import irt.data.packet.PacketSuper;
import irt.data.packet.interfaces.Packet;

public class CallibrationModePacket extends PacketSuper {

	public final static Function<Packet, Optional<Object>> parseValueFunction = packet-> Optional
																										.ofNullable(packet)
																										.map(Packet::getPayloads)
																										.map(List::stream)
																										.flatMap(Stream::findAny)
																										.map(pl->pl.getInt(0))
																										.map(i->i==1);

	public CallibrationModePacket(byte linkAddr) {
		super(linkAddr, PacketImp.PACKET_TYPE_REQUEST, PacketIDs.DEVICE_DEBUG_CALIBRATION_MODE, PacketImp.GROUP_ID_DEVICE_DEBAG, PacketImp.PARAMETER_DEVICE_DEBUG_CALIBRATION_MODE, null, Priority.REQUEST);
	}

	public CallibrationModePacket(byte linkAddr, int value) {
		super(linkAddr, PacketImp.PACKET_TYPE_COMMAND, PacketIDs.DEVICE_DEBUG_CALIBRATION_MODE, PacketImp.GROUP_ID_DEVICE_DEBAG, PacketImp.PARAMETER_DEVICE_DEBUG_CALIBRATION_MODE, null, Priority.COMMAND);
		setValue(value);
	}
}