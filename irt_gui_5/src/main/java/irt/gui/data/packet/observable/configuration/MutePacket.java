package irt.gui.data.packet.observable.configuration;

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
import irt.gui.data.packet.interfaces.ConfigurationGroup;
import irt.gui.data.packet.observable.PacketAbstract5;
import irt.gui.errors.PacketParsingException;

public class MutePacket extends PacketAbstract5 implements ConfigurationGroup{

//	private static final Logger l = LogManager.getLogger();

	public static final PacketId BUC_PACKET_ID = PacketId.CONFIGURATION_MUTE;
	public static final PacketId FCM_PACKET_ID = PacketId.CONFIGURATION_FCM_MUTE;

	public enum MuteStatus{
		UNMUTED,
		MUTED,
		UNKNOWN	
	}

	public MutePacket() throws PacketParsingException {
		this((MuteStatus)null);
	}

	public MutePacket(MuteStatus muteStatus) throws PacketParsingException {
		super(
				new PacketHeader(
						muteStatus==null ? PacketType.REQUEST : PacketType.COMMAND,
						new PacketIdDetails(BUC_PACKET_ID, muteStatus==null ? "Get Mute status" : "Set Mute status to "+ muteStatus),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(
								BUC_PACKET_ID),
						null));
	}

	public MutePacket(@JsonProperty("asBytes") byte[] answer, @JsonProperty(defaultValue="false", value="v") Boolean hasAcknowledgment) throws PacketParsingException {
		super(new PacketProperties(BUC_PACKET_ID).setHasAcknowledgment(Optional.ofNullable(hasAcknowledgment).orElse(false)), answer);
	}

	@Override @JsonIgnore
	public PacketId getPacketId() {
		return linkHeader.getAddr()==CONVERTER_ADDR ? FCM_PACKET_ID : BUC_PACKET_ID;
	}

	@Override public synchronized void setLinkHeaderAddr(byte addr) {

		if(addr == linkHeader.getAddr())
			return;

		super.setLinkHeaderAddr(addr);

		try {
			getPayloads().get(0).setParameterHeader(new ParameterHeader(addr==CONVERTER_ADDR ? FCM_PACKET_ID : BUC_PACKET_ID));
		} catch (PacketParsingException e) {
			logger.catching(e);
		}
	}

	public void setCommand(MuteStatus muteStatus) {
		getPayloads().get(0).setBuffer((byte)muteStatus.ordinal());
	}
}
