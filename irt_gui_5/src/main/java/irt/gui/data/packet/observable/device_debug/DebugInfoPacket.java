package irt.gui.data.packet.observable.device_debug;

import irt.gui.data.PacketIdDetails;
import irt.gui.data.packet.Packet;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.ParameterHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.observable.RegirterAbstractPacket;
import irt.gui.errors.PacketParsingException;

public class DebugInfoPacket extends RegirterAbstractPacket {

	public DebugInfoPacket(DebugInfoCode code, int parameter) throws PacketParsingException {
		super(
				new PacketHeader(
						PacketType.REQUEST,
						new PacketIdDetails(code.getPacketID(), "; Get Debug Info"),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(code.getPacketID()),
						Packet.toBytes(parameter)));
	}

	public DebugInfoPacket(byte[] answer) throws PacketParsingException {
		super(DebugInfoCode.INFO.getPacketID(), answer);
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
