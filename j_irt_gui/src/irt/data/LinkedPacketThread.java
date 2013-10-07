package irt.data;

import irt.data.packet.LinkHeader;
import irt.data.packet.LinkedPacket;
import irt.data.packet.Packet;

import java.util.Arrays;

public class LinkedPacketThread extends PacketThread {

	private LinkHeader linkHeader;

	public LinkedPacketThread(LinkHeader linkHeader, byte[] packetSetting) {
		super(packetSetting);
		setLinkHeader(linkHeader);
	}

	public LinkedPacketThread(LinkHeader linkHeader, byte[] packetSetting, String threadName) {
		super(packetSetting, threadName);
		setLinkHeader(linkHeader);
	}

	public void setLinkHeader(LinkHeader linkHeader){

		this.linkHeader = linkHeader;
		if(linkHeader!=null){
			byte[] b = linkHeader.asBytes();
			b = Arrays.copyOf(b, LinkHeader.SIZE+data.length);
			System.arraycopy(data, 0, b, LinkHeader.SIZE, data.length);
			data = b;
		}
	}

	public LinkHeader getLinkHeader() {
		return linkHeader;
	}

	@Override
	protected Packet newPacket() {
		return new LinkedPacket();
	}

	@Override
	public void setDataPacketTypeCommand() {
		if(data!=null)
			data[4] = Packet.IRT_SLCP_PACKET_TYPE_COMMAND;
	}
}
