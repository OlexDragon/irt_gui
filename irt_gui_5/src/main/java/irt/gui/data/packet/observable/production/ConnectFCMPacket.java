
package irt.gui.data.packet.observable.production;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import irt.gui.controllers.PacketSenderJssc;
import irt.gui.controllers.interfaces.WaitTime;
import irt.gui.data.PacketIdDetails;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.PacketProperties;
import irt.gui.data.packet.ParameterHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.enums.PacketErrors;
import irt.gui.data.packet.enums.PacketId;
import irt.gui.data.packet.enums.PacketType;
import irt.gui.data.packet.observable.PacketAbstract5;
import irt.gui.errors.PacketParsingException;

//*********************************************  class  ConnectFCMPacket   ****************************************************************
public class ConnectFCMPacket extends PacketAbstract5 implements WaitTime {

	@Override
	public byte[] toBytes() {
		return new byte[]{0x7E, (byte) 0xFE, 0x00, 0x00, 0x00, 0x03, 0x00, 0x78, 0x64, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x5A, 0x51, 0x7E};
	}

	public static final PacketId PACKET_ID = PacketId.PRODUCTION_UPDATE_FCM;

	/**
	 * @param mode null - to get Calibration mode;
	 * @throws PacketParsingException 
	 */
	public ConnectFCMPacket() throws PacketParsingException {
		super(
				new PacketHeader(
						PacketType.COMMAND,
						new PacketIdDetails(PACKET_ID, "Connect to FCM"),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(
								PACKET_ID),
						null));

	}

	public ConnectFCMPacket(@JsonProperty("asBytes") byte[] answer, @JsonProperty(defaultValue="false", value="v") Boolean hasAcknowledgment) throws PacketParsingException {
		super(new PacketProperties(PACKET_ID).setHasAcknowledgment(Optional.ofNullable(hasAcknowledgment).orElse(false)), answer);
	}

	@Override @JsonIgnore
	public PacketId getPacketId() {
		return PACKET_ID;
	}

	@Override
	protected boolean checkAcknowledgement(byte[] acknowledgement, PacketProperties packetProperties) {
		return acknowledgement.length!=5;
	}

	@Override
	public int getWaitTime() {
		return PacketSenderJssc.FCM_BY_BAIS_WAIT_TIME;
	}
}