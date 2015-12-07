
package irt.data.packet;

import irt.data.PacketWork;

public class RedundancyNamePacket extends PacketAbstract {

	public RedundancyNamePacket(byte linkAddr, RedundancyName redundancyName) {
		super(
				linkAddr,
				redundancyName!=null ? PacketImp.PACKET_TYPE_COMMAND : PacketImp.PACKET_TYPE_REQUEST,
						PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_NAME,
						PacketImp.GROUP_ID_CONFIGURATION,
						PacketImp.PARAMETER_ID_CONFIGURATION_REDUNDANCY_NAME,
						redundancyName!=null ? redundancyName.toBytes() : null,
						redundancyName!=null ? Priority.COMMAND : Priority.REQUEST);
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
