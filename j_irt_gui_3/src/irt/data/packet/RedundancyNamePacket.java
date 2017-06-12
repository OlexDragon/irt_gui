
package irt.data.packet;

import java.util.Optional;

import irt.data.packet.interfaces.PacketWork;

public class RedundancyNamePacket extends ConfifurationPacket {

	public RedundancyNamePacket(byte linkAddr, RedundancyName redundancyName) {
		super(
				linkAddr,
						PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_NAME,
						PacketImp.PARAMETER_ID_CONFIGURATION_REDUNDANCY_NAME,
						redundancyName!=null ? redundancyName.toBytes() : null);
	}


	public RedundancyNamePacket() {
		this((byte) 0, null);
	}

	@Override
	public Object getValue() {
		final RedundancyName[] values = RedundancyName.values();
		return Optional
				.ofNullable(getPayloads())
				.map(pls->pls.parallelStream())
				.flatMap(stream->stream.findAny())
				.map(Payload::getBuffer)
				.map(d->d[0])
				.filter(index->index<values.length)
				.map(index->values[index]);
	}

	public enum RedundancyName {
		NO_NAME(null),
		BUC_A("BUC A"),
		BUC_B("BUC B");

		private String name;

		private RedundancyName(String name){
			this.name = name;
		}
		public byte[] toBytes() {
			return new byte[]{(byte) ordinal()};
		}
		@Override
		public String toString() {
			return name;
		}
	}
}
