
package irt.gui.data.packet.observable.device_debug;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import irt.gui.data.PacketIdDetails;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.ParameterHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.observable.RegirterAbstractPacket;
import irt.gui.errors.PacketParsingException;

//*********************************************   InfoPacket   ****************************************************************
public class InitializeBiasPacket extends RegirterAbstractPacket {

	public static final PacketId PACKET_ID = PacketId.PRODUCTION_INITIALIZE_BIASES;

	/**
	 * @param mode null - to get Calibration mode;
	 * @throws PacketParsingException 
	 */
	public InitializeBiasPacket() throws PacketParsingException {
		super(
				new PacketHeader(
						PacketType.COMMAND,
						new PacketIdDetails(PACKET_ID, "Initialize BIAS Board "),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(
								PACKET_ID),
						null));
	}

	public InitializeBiasPacket(@JsonProperty("asBytes") byte[] answer, @JsonProperty(defaultValue="false", value="v") boolean hasAcknowledgment) throws PacketParsingException {
		super(PACKET_ID, answer, hasAcknowledgment);
	}

	@Override @JsonIgnore
	public PacketId getPacketId() {
		return PACKET_ID;
	}
}