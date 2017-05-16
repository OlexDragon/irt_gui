
package irt.data.packet;

import irt.data.packet.interfaces.PacketWork;

public class RedundancyStatusPacket extends PacketAbstract {

	public RedundancyStatusPacket(byte linkAddr, RedundancyStatus redundancyStatus) {
		super(
				linkAddr,
				redundancyStatus!=null ? PacketImp.PACKET_TYPE_COMMAND : PacketImp.PACKET_TYPE_REQUEST,
						PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_STATUS,
						PacketImp.GROUP_ID_CONFIGURATION,
						PacketImp.PARAMETER_ID_CONFIGURATION_REDUNDANCY_STATUS,
						redundancyStatus!=null ? redundancyStatus.toBytes() : null,
						redundancyStatus!=null ? Priority.COMMAND : Priority.REQUEST);
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
