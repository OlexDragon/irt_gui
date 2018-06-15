
package irt.data.packet;

import java.util.Optional;

import irt.data.packet.configuration.ConfifurationPacket;
import irt.data.packet.interfaces.PacketWork;

public class RedundancyStatusPacket extends ConfifurationPacket {

	public RedundancyStatusPacket(byte linkAddr, RedundancyStatus redundancyStatus) {
		super(
				linkAddr,
						PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_STATUS,
						PacketImp.PARAMETER_ID_CONFIGURATION_REDUNDANCY_STATUS,
						redundancyStatus!=null ? redundancyStatus.toBytes() : null);
	}

	public RedundancyStatusPacket() {
		this((byte) 0, null);
	}

	@Override
	public Object getValue() {
		final RedundancyStatus[] values = RedundancyStatus.values();
		return Optional
				.ofNullable(getPayloads())
				.map(pls->pls.parallelStream())
				.flatMap(stream->stream.findAny())
				.map(Payload::getBuffer)
				.map(d->d[0])
				.filter(index->index<values.length)
				.map(index->values[index]);
	}

	public enum RedundancyStatus{
		UNKNOWN,
		ONLINE,
		STANDBY;
		public byte[] toBytes(){
			return new byte[]{(byte) ordinal()};
		}
	}
}
