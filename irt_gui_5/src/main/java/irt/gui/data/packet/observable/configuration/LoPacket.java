
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
import irt.gui.data.packet.observable.PacketAbstract5;
import irt.gui.errors.PacketParsingException;

public class LoPacket extends PacketAbstract5{

	public static final PacketId PACKET_ID_BUC 			= PacketId.CONFIGURATION_LO;
	public static final PacketId PACKET_ID_CONVERTER 	= PacketId.CONFIGURATION_FREQUENCY_FCM;

	public LoPacket() throws PacketParsingException {
		this(null);
	}

	public LoPacket(Byte value) throws PacketParsingException {
		super(
				new PacketHeader(
						value==null ? PacketType.REQUEST : PacketType.COMMAND,
						new PacketIdDetails(
								PACKET_ID_BUC, "Get Lo Frequencies"),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(PACKET_ID_BUC),
						Optional.ofNullable(value).map(b->new byte[]{b}).orElse(null)));
	}

	public LoPacket(@JsonProperty("asBytes") byte[] answer, @JsonProperty(defaultValue="false", value="v") Boolean hasAcknowledgment) throws PacketParsingException {
		super(new PacketProperties(PACKET_ID_BUC).setHasAcknowledgment(Optional.ofNullable(hasAcknowledgment).orElse(false)), answer);
	}

	@Override @JsonIgnore
	public PacketId getPacketId() {

		return getLinkHeader().getAddr()==-1 ? PACKET_ID_CONVERTER : PACKET_ID_BUC;
	}

	public void setValue(Object value) {

		if(value instanceof Byte){
			getPayloads().get(0).setBuffer((Byte)value);
			return;
		}

		if(value instanceof Long){
			if(getLinkHeader().getAddr()==-1)
				getPayloads().get(0).setBuffer((Long)value);

			else
				getPayloads().get(0).setBuffer(((Long) value).byteValue());
		}
	}
}
