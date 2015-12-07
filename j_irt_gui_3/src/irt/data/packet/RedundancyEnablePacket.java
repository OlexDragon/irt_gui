
package irt.data.packet;

import irt.data.PacketWork;

public class RedundancyEnablePacket extends PacketAbstract {

	public RedundancyEnablePacket(byte linkAddr, RedundancyEnable redundancyEnable) {
		super(
				linkAddr,
				redundancyEnable!=null ? PacketImp.PACKET_TYPE_COMMAND : PacketImp.PACKET_TYPE_REQUEST,
						PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_ENABLE,
						PacketImp.GROUP_ID_CONFIGURATION,
						PacketImp.PARAMETER_ID_CONFIGURATION_REDUNDANCY_ENABLE,
						redundancyEnable!=null ? redundancyEnable.toBytes() : null,
						redundancyEnable!=null ? Priority.COMMAND : Priority.REQUEST);
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
