
package irt.data.packet;

import java.util.Optional;

import irt.data.packet.configuration.ConfifurationPacket;
import irt.data.packet.interfaces.PacketWork;

public class RedundancyEnablePacket extends ConfifurationPacket {

	public RedundancyEnablePacket(byte linkAddr, RedundancyEnable redundancyEnable) {
		super(
				linkAddr,
						PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_ENABLE,
						PacketImp.PARAMETER_ID_CONFIGURATION_REDUNDANCY_ENABLE,
						redundancyEnable!=null ? redundancyEnable.toBytes() : null);
	}

	public RedundancyEnablePacket() {
		this((byte)0, null);
	}

	@Override
	public Object getValue() {
		final RedundancyEnable[] values = RedundancyEnable.values();
		return Optional
				.ofNullable(getPayloads())
				.map(pls->pls.parallelStream())
				.flatMap(stream->stream.findAny())
				.map(Payload::getBuffer)
				.map(d->d[0])
				.filter(index->index<values.length)
				.map(index->values[index]);
	}

	public enum RedundancyEnable{
		DISABLE("Desable"),
		ENABLE("Enable");
		
		private String redundancy;

		private RedundancyEnable(String redundancy){
			this.redundancy = redundancy;
		}
		public byte[] toBytes(){
			return new byte[]{(byte) ordinal()};
		}
		public void setRedundancy(String redundancy) {
			this.redundancy = redundancy;
		}
		@Override
		public String toString() {
			return redundancy;
		}
	}
}
