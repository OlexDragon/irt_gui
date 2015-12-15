
package irt.gui.data.packet.observable.device_debug;

import java.util.Arrays;

import irt.gui.data.PacketIdDetails;
import irt.gui.data.RegisterValue;
import irt.gui.data.packet.Packet;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.ParameterHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.observable.RegirterAbstractPacket;
import irt.gui.errors.PacketParsingException;

public class ConverterDACPacket extends RegirterAbstractPacket {

	public static final PacketId PACKET_ID = PacketId.DEVICE_DEBAG_CONVERTER_DAC;

	public ConverterDACPacket( RegisterValue registerValue) throws PacketParsingException {
		super(
				new PacketHeader(
						registerValue.getValue()==null ? PacketType.REQUEST : PacketType.COMMAND,
						new PacketIdDetails(PACKET_ID, "; value:" +  registerValue),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(PACKET_ID),
						getBuffer(registerValue.getIndex(), registerValue.getAddr(), registerValue.getValue())));

	}

	public ConverterDACPacket(byte[] answer) throws PacketParsingException {
		super(PACKET_ID, answer);
	}

	public static byte[] getBuffer(int index, int addr, Integer value) {

		int length = value==null ? 8 : 12;

		byte[] bs  = Arrays.copyOf(Packet.toBytes(index), length);
		System.arraycopy(Packet.toBytes(addr), 0, bs, 4, 4);

		if(value!=null)
			System.arraycopy(Packet.toBytes(value), 0, bs, 8, 4);

		return bs;
	}

	@Override
	public PacketId getPacketId() {
		return PACKET_ID;
	}
}
