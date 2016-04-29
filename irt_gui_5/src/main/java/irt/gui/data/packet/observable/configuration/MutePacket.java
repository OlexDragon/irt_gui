package irt.gui.data.packet.observable.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import irt.gui.data.PacketIdDetails;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.ParameterHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.enums.PacketErrors;
import irt.gui.data.packet.enums.PacketId;
import irt.gui.data.packet.enums.PacketType;
import irt.gui.data.packet.interfaces.ConfigurationGroup;
import irt.gui.data.packet.observable.PacketAbstract;
import irt.gui.errors.PacketParsingException;

public class MutePacket extends PacketAbstract implements ConfigurationGroup{

//	private static final Logger l = LogManager.getLogger();

	public static final PacketId PACKET_ID = PacketId.CONFIGURATION_MUT;

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
						new PacketIdDetails(PACKET_ID, muteStatus==null ? "Get Mute status" : "Set Mute status to "+ muteStatus),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(
								PACKET_ID),
						null));
	}

	public MutePacket(@JsonProperty("asBytes") byte[] answer, @JsonProperty(defaultValue="false", value="v") boolean hasAcknowledgment) throws PacketParsingException {
		super(PACKET_ID, answer, hasAcknowledgment);
	}

	@Override @JsonIgnore
	public PacketId getPacketId() {
		return PACKET_ID;
	}

	public void setCommand(MuteStatus muteStatus) {
		getPayloads().get(0).setBuffer((byte)muteStatus.ordinal());
	}
}
