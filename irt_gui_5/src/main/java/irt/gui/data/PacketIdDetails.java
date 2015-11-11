package irt.gui.data;

import irt.gui.data.packet.Packet;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketId;

public class PacketIdDetails {

	private PacketId packetId;		public PacketId	getPacketId()		{ return packetId;		}
	private String packetDetails;	public String	getPacketDetails()	{ return packetDetails; }

	public PacketIdDetails(PacketId packetId, String packetDetails) {

		this.packetId = packetId;

		if(packetDetails==null)
			this.packetDetails = "";
		else
			this.packetDetails = packetDetails;
	}

	public short getValue() { return packetId.getValue(); }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = prime + ((packetId == null) ? 0 : packetId.hashCode());
		return prime * result + ((packetDetails == null) ? 0 : packetDetails.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PacketIdDetails other = (PacketIdDetails) obj;
		if (packetId != other.packetId)
			return false;
		if (packetDetails == null) {
			if (other.packetDetails != null)
				return false;
		} else if (!packetDetails.equals(other.packetDetails))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return packetId + ":" + packetDetails;
	}

	public byte[] toBytes() {
		return packetId!=null ? Packet.toBytes(packetId.getValue()) : null;
		
	}
}
