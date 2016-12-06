package irt.packet.observable.configuration;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import irt.packet.PacketHeader;
import irt.packet.PacketIdDetails;
import irt.packet.PacketParsingException;
import irt.packet.PacketProperties;
import irt.packet.ParameterHeader;
import irt.packet.Payload;
import irt.packet.enums.PacketErrors;
import irt.packet.enums.PacketId;
import irt.packet.enums.PacketType;
import irt.packet.interfaces.ConfigurationGroup;
import irt.packet.observable.PacketAbstract;

public class MutePacket extends PacketAbstract implements ConfigurationGroup{

//	private static final Logger l = LogManager.getLogger();

	public static final PacketId BUC_PACKET_ID = PacketId.CONFIGURATION_MUTE;
	public static final PacketId FCM_PACKET_ID = PacketId.CONFIGURATION_FCM_MUTE;

	public enum MuteStatus{
		UNMUTED,
		MUTED
		
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

	public void setCommand(MuteStatus muteStatus) {
		getPayloads().get(0).setBuffer((byte)muteStatus.ordinal());
	}
}
