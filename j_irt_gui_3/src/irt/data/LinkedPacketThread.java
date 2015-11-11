package irt.data;

import java.util.Arrays;

import irt.data.packet.LinkHeader;
import irt.data.packet.LinkedPacketImp;
import irt.data.packet.PacketImp;

public class LinkedPacketThread extends PacketThread {

	private LinkHeader linkHeader;

	public LinkedPacketThread(LinkHeader linkHeader, byte[] packetSetting) {
		super(packetSetting);
		setLinkHeader(linkHeader);
	}

	public LinkedPacketThread(LinkHeader linkHeader, byte[] packetSetting, String threadName) {
		super(packetSetting, threadName);
		setLinkHeader(linkHeader);
		logger.trace(linkHeader);
	}

	public void setLinkHeader(LinkHeader linkHeader){

		synchronized (data) {
			this.linkHeader = linkHeader;
			if(linkHeader!=null){
				byte[] b = linkHeader.toBytes();
				b = Arrays.copyOf(b, LinkHeader.SIZE+data.length);
				System.arraycopy(data, 0, b, LinkHeader.SIZE, data.length);
				data = b;
				logger.trace(Arrays.toString(data));
			}
		}
	}

	public LinkHeader getLinkHeader() {
		return linkHeader;
	}

	@Override
	protected PacketImp newPacket() {
		return new LinkedPacketImp();
	}

	@Override
	public void setDataPacketTypeCommand() {
		if(data!=null)
			data[4] = PacketImp.PACKET_TYPE_COMMAND;
	}
}
