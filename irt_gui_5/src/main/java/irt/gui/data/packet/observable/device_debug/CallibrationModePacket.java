
package irt.gui.data.packet.observable.device_debug;

import java.util.List;

import irt.gui.data.PacketIdDetails;
import irt.gui.data.packet.Packet;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.ParameterHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.observable.PacketAbstract;
import irt.gui.errors.PacketParsingException;

//*********************************************   InfoPacket   ****************************************************************
public class CallibrationModePacket extends PacketAbstract implements LinkedPacket {

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

	public CallibrationModePacket(byte[] answer) throws PacketParsingException {
		super(PACKET_ID, answer);
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
		}

		return value!=null ? CalibrationMode.values()[value] : null;
	}
}