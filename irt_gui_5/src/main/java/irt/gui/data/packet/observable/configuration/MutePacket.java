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

	public enum MuteStatus{
		UNMUTED,
		MUTED,
		UNKNOWN	
	}

	private PacketId packetId;

	public MutePacket() throws PacketParsingException {
		this((MuteStatus)null);
	}

	public MutePacket(MuteStatus muteStatus) throws PacketParsingException {
		super(
				new PacketHeader(
						Optional.ofNullable(muteStatus).map(ms->PacketType.COMMAND).orElse(PacketType.REQUEST),
						new PacketIdDetails(PacketId.CONFIGURATION_MUTE, Optional.ofNullable(muteStatus).map(ms->"Set Mute status to " + ms).orElse("Get Mute status")),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(
								PacketId.CONFIGURATION_MUTE),
						null));
		packetId = PacketId.CONFIGURATION_MUTE;
	}

	public MutePacket(@JsonProperty("asBytes") byte[] answer, @JsonProperty(defaultValue="false", value="v") Boolean hasAcknowledgment) throws PacketParsingException {
		super(new PacketProperties(PacketId.CONFIGURATION_FCM_MUTE).setHasAcknowledgment(Optional.ofNullable(hasAcknowledgment).orElse(false)), answer);
	}

	@Override @JsonIgnore
	public PacketId getPacketId() {
		return packetId;
	}

	@Override public synchronized boolean setLinkHeaderAddr(byte addr) {

		if(addr==0)
			packetId = PacketId.CONFIGURATION_FCM_MUTE;
		else
			packetId = PacketId.CONFIGURATION_MUTE;

		return super.setLinkHeaderAddr(addr);
	}

	public void setCommand(MuteStatus muteStatus) {
		getPayloads().get(0).setBuffer((byte)muteStatus.ordinal());
	}
}
