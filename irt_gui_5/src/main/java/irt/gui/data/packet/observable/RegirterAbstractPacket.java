
package irt.gui.data.packet.observable;

import java.util.Arrays;
import java.util.List;

import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.PacketProperties;
import irt.gui.data.packet.Payload;
import irt.gui.errors.PacketParsingException;

public abstract class RegirterAbstractPacket extends PacketAbstract{

	protected RegirterAbstractPacket(PacketHeader packetHeader, Payload payload) {
		super(packetHeader, payload);
	}

	protected RegirterAbstractPacket(PacketProperties packetProperties, byte[] answer) throws PacketParsingException {
		super(packetProperties, answer);
	}

	@Override
	public int hashCode() {

		final List<Payload> payloads = getPayloads();
		int hash = 0;

		if(payloads!=null && payloads.size()>0){
			final Payload payload = payloads.get(0);
			if(payload.getParameterHeader().getPayloadSize().getSize()!=0){
				final byte[] buffer = payload.getBuffer();
				hash = Arrays.hashCode(buffer.length>8 ? Arrays.copyOf(buffer, 8) : buffer);
			}
		}

		return 31 * super.hashCode() + hash;
	}
}
