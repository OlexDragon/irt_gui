package irt.gui.data.packet.observable.device_debug;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import irt.gui.data.PacketIdDetails;
import irt.gui.data.packet.Packet;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.ParameterHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.enums.PacketErrors;
import irt.gui.data.packet.enums.PacketId;
import irt.gui.data.packet.enums.PacketType;
import irt.gui.data.packet.observable.RegirterAbstractPacket;
import irt.gui.errors.PacketParsingException;

public class DebugInfoPacket extends RegirterAbstractPacket {

	private static PacketId PACKET_ID;

	public DebugInfoPacket(DebugInfoCode code, int parameter) throws PacketParsingException {
		super(
				new PacketHeader(
						PacketType.REQUEST,
						new PacketIdDetails(PACKET_ID = code.getPacketID(), "; Get Debug Info"),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(PACKET_ID),
						Packet.toBytes(parameter)));
	}

	public DebugInfoPacket(@JsonProperty("asBytes") byte[] answer, @JsonProperty(defaultValue="false", value="v") boolean hasAcknowledgment) throws PacketParsingException {
		super(DebugInfoCode.INFO.getPacketID(), answer, hasAcknowledgment);
	}

	@Override @JsonIgnore
	public PacketId getPacketId() {
		return PACKET_ID;
	}

	public enum DebugInfoCode {
		INFO(PacketId.DEVICE_DEBAG_INFO, "device information: parts, firmware and etc."),
		DUMP(PacketId.DEVICE_DEBAG_DUMP, "dump of registers for specified device index ");

		private final PacketId packetId;
		private final String text;

		DebugInfoCode(PacketId packetId, String text){
			this.packetId = packetId;
			this.text = text;
		}

		public PacketId getPacketID() {
			return packetId;
		}

		@Override
		public String toString(){
			return text;
		}
	}
}
