
package irt.data.packets;

import java.util.List;
import java.util.Optional;

import irt.data.packets.core.Packet;
import irt.data.packets.core.PacketHeader;
import irt.data.packets.core.PacketProperties;
import irt.data.packets.core.ParameterHeader;
import irt.data.packets.core.Payload;
import irt.data.packets.enums.PacketErrors;
import irt.data.packets.enums.PacketId;
import irt.data.packets.enums.PacketType;

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

	public CallibrationModePacket( byte[] answer, Boolean hasAcknowledgment) throws PacketParsingException {
		super(new PacketProperties(PACKET_ID).setHasAcknowledgment(Optional.ofNullable(hasAcknowledgment).orElse(false)), answer);
	}

	public CalibrationMode getCallibrationMode() {

		Integer value = null;

		List<Payload> pls = getPayloads();
		if(pls!=null && !pls.isEmpty()){
			value = pls.get(0).getInt(0);

			if(value<0 || value>1){
				logger.warn("Resived wrong value: {}", value);
				value = null;
			}
		}else
			logger.warn("Payload is empty: {}", pls);

		return value!=null ? CalibrationMode.values()[value] : null;
	}

	@Override
	public PacketId getPacketId() {
		return PACKET_ID;
	}
}