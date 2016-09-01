package irt.data.packets;

import java.util.Arrays;
import java.util.Optional;

import irt.data.packets.core.Packet;
import irt.data.packets.core.PacketHeader;
import irt.data.packets.core.PacketProperties;
import irt.data.packets.core.ParameterHeader;
import irt.data.packets.core.Payload;
import irt.data.packets.enums.PacketErrors;
import irt.data.packets.enums.PacketId;
import irt.data.packets.enums.PacketType;

public class RegisterPacket extends RegirterAbstractPacket {
//
//	@Override public synchronized void deleteObserver(Observer o) {
//		super.deleteObserver(o);
//	}
//
//	@Override public synchronized void deleteObservers() {
//		super.deleteObservers();
//	}

	public static final PacketId PACKET_ID = PacketId.DEVICE_DEBAG_REGISTER;

	public RegisterPacket( RegisterValue registerValue) throws PacketParsingException {
		super(
				new PacketHeader(
						registerValue.getValue()==null ? PacketType.REQUEST : PacketType.COMMAND,
						new PacketIdDetails(PACKET_ID, "; register:" +  registerValue),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(PACKET_ID),
						getBuffer(registerValue.getIndex(), registerValue.getAddr(), registerValue.getValue())));
	}

	public RegisterPacket( byte[] answer, Boolean hasAcknowledgment) throws PacketParsingException {
		super(new PacketProperties(PACKET_ID).setHasAcknowledgment(Optional.ofNullable(hasAcknowledgment).orElse(false)), answer);
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
