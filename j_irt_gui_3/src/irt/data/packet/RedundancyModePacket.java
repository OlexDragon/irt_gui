
package irt.data.packet;

import irt.data.PacketWork;

public class RedundancyModePacket extends PacketAbstract {

	public RedundancyModePacket(byte linkAddr, RedundancyMode redundancyMode) {
		super(
				linkAddr,
				redundancyMode!=null ? PacketImp.PACKET_TYPE_COMMAND : PacketImp.PACKET_TYPE_REQUEST,
						PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_MODE,
						PacketImp.GROUP_ID_CONFIGURATION,
						PacketImp.PARAMETER_ID_CONFIGURATION_REDUNDANCY_MODE,
						redundancyMode!=null ? redundancyMode.toBytes() : null,
								redundancyMode!=null ? Priority.COMMAND : Priority.REQUEST);
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
