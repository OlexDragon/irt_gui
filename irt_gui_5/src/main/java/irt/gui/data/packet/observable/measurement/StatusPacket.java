package irt.gui.data.packet.observable.measurement;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

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

public class StatusPacket extends PacketAbstract5{

//	private static final Logger l = LogManager.getLogger();

	public static final PacketId BUC_PACKET_ID = PacketId.MEASUREMENT_STATUS_BUC;
	public static final PacketId FCM_PACKET_ID = PacketId.MEASUREMENT_STATUS_FCM;

	public StatusPacket() throws PacketParsingException {
		super(
				new PacketHeader(
						PacketType.REQUEST,
						new PacketIdDetails(BUC_PACKET_ID, "Get status byte"),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(
								BUC_PACKET_ID),
						null));
	}

	public StatusPacket(@JsonProperty("asBytes") byte[] answer, @JsonProperty(defaultValue="false", value="v") Boolean hasAcknowledgment) throws PacketParsingException {
		super(new PacketProperties(BUC_PACKET_ID).setHasAcknowledgment(Optional.ofNullable(hasAcknowledgment).orElse(false)), answer);
	}

	@Override @JsonIgnore
	public PacketId getPacketId() {
		return linkHeader.getAddr()==-1 ? FCM_PACKET_ID : BUC_PACKET_ID;
	}

	@Override public synchronized void setLinkHeaderAddr(byte addr) {

		if(addr == linkHeader.getAddr())
			return;

		super.setLinkHeaderAddr(addr);

		try {
			getPayloads().get(0).setParameterHeader(new ParameterHeader(addr==-1 ? FCM_PACKET_ID : BUC_PACKET_ID));
		} catch (PacketParsingException e) {
			logger.catching(e);
		}
	}
}
