
package irt.gui.data.packet.observable.device_debug;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import irt.gui.data.PacketIdDetails;
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

//*********************************************   InfoPacket   ****************************************************************
public class CallibrationModePacket extends RegirterAbstractPacket {

	public enum CalibrationMode{
		OFF,
		ON
		
	}

	public static final PacketId PACKET_ID = PacketId.DEVICE_DEBAG_CALIBRATION_MODE;

	/**
	 * @param mode null - to get Calibration mode;
	 * @throws PacketParsingException 
	 */
	public CallibrationModePacket(CalibrationMode mode) throws PacketParsingException {
		super(
				new PacketHeader(
						mode==null ? PacketType.REQUEST : PacketType.COMMAND,
						new PacketIdDetails(PACKET_ID, mode==null ? "Get Calibration Mode" : "Set Calibration Mode "+mode),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(
								PACKET_ID),
						mode!=null ? Packet.toBytes(mode.ordinal()) : null));

	}

	public CallibrationModePacket(@JsonProperty("asBytes") byte[] answer, @JsonProperty(defaultValue="false", value="v") Boolean hasAcknowledgment) throws PacketParsingException {
		super(new PacketProperties(PACKET_ID).setHasAcknowledgment(Optional.ofNullable(hasAcknowledgment).orElse(false)), answer);
	}

	@JsonIgnore
	public CalibrationMode getCallibrationMode() {

		Integer value = null;

		List<Payload> pls = getPayloads();
		if(pls!=null && !pls.isEmpty()){
			value = pls.get(0).getInt(0);

			if(value<0 || value>1){
				logger.warn("Resived wrong value: {}", value);
				value = null;
			}
		}

		return value!=null ? CalibrationMode.values()[value] : null;
	}

	@Override @JsonIgnore
	public PacketId getPacketId() {
		return PACKET_ID;
	}
}