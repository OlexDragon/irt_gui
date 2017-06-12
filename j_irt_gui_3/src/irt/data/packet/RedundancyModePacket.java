
package irt.data.packet;

import java.util.Optional;

import irt.data.packet.interfaces.PacketWork;

public class RedundancyModePacket extends ConfifurationPacket {

	public RedundancyModePacket(byte linkAddr, RedundancyMode redundancyMode) {
		super(
				linkAddr,
						PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_MODE,
						PacketImp.PARAMETER_ID_CONFIGURATION_REDUNDANCY_MODE,
						redundancyMode!=null ? redundancyMode.toBytes() : null);
	}

	public RedundancyModePacket() {
		this((byte) 0, null);
	}

	@Override
	public Object getValue() {
		final RedundancyMode[] values = RedundancyMode.values();
		return Optional
				.ofNullable(getPayloads())
				.map(pls->pls.parallelStream())
				.flatMap(stream->stream.findAny())
				.map(Payload::getBuffer)
				.map(d->d[0])
				.filter(index->index<values.length)
				.map(index->values[index]);
	}

	public enum RedundancyMode {
		COLD_STANDBY("Cold Standby"),
		HOT_STANDBY("Hot Standby");

		private String mode;

		private RedundancyMode(String mode){
			this.mode = mode;
		}
		public void setMode(String mode) {
			this.mode = mode;
		}
		public byte[] toBytes(){
			return new byte[]{(byte) ordinal()};
		}
		@Override
		public String toString() {
			return mode;
		}
	}
}
