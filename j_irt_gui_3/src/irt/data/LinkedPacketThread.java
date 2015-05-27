package irt.data;

import irt.data.packet.LinkHeader;
import irt.data.packet.LinkedPacket;
import irt.data.packet.Packet;

import java.util.Arrays;

import org.apache.logging.log4j.Logger;

public class LinkedPacketThread extends PacketThread {

	private LinkHeader linkHeader;

	public LinkedPacketThread(LinkHeader linkHeader, byte[] packetSetting, Logger logger) {
		super(packetSetting, logger);
		setLinkHeader(linkHeader);
	}

	public LinkedPacketThread(LinkHeader linkHeader, byte[] packetSetting, String threadName, Logger logger) {
		super(packetSetting, threadName, logger);
		setLinkHeader(linkHeader);
		logger.trace(linkHeader);
	}

	public void setLinkHeader(LinkHeader linkHeader){

		synchronized (data) {
			this.linkHeader = linkHeader;
			if(linkHeader!=null){
				byte[] b = linkHeader.asBytes();
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
	protected Packet newPacket() {
		return new LinkedPacket();
	}

	@Override
	public void setDataPacketTypeCommand() {
		if(data!=null)
			data[4] = Packet.PACKET_TYPE_COMMAND;
	}
}
