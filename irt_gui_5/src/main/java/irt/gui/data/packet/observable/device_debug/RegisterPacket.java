package irt.gui.data.packet.observable.device_debug;

import java.util.Arrays;
import java.util.Observer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import irt.gui.data.PacketIdDetails;
import irt.gui.data.RegisterValue;
import irt.gui.data.packet.Packet;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.PacketProperties;
import irt.gui.data.packet.ParameterHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.enums.PacketErrors;
import irt.gui.data.packet.enums.PacketId;
import irt.gui.data.packet.enums.PacketType;
import irt.gui.data.packet.observable.RegirterAbstractPacket;
import irt.gui.errors.PacketParsingException;

public class RegisterPacket extends RegirterAbstractPacket {

	@Override
	public synchronized void deleteObserver(Observer o) {
		super.deleteObserver(o);
	}

	@Override
	public synchronized void deleteObservers() {
		super.deleteObservers();
	}

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

	public RegisterPacket(@JsonProperty("asBytes") byte[] answer, @JsonProperty(defaultValue="false", value="v") boolean hasAcknowledgment) throws PacketParsingException {
		super(new PacketProperties(PACKET_ID).setHasAcknowledgment(hasAcknowledgment), answer);
	}

	public static byte[] getBuffer(int index, int addr, Integer value) {

		int length = value==null ? 8 : 12;

		byte[] bs  = Arrays.copyOf(Packet.toBytes(index), length);
		System.arraycopy(Packet.toBytes(addr), 0, bs, 4, 4);

		if(value!=null)
			System.arraycopy(Packet.toBytes(value), 0, bs, 8, 4);

		return bs;
	}

	@Override @JsonIgnore
	public PacketId getPacketId() {
		return PACKET_ID;
	}
}
