package irt.data.packet;

import java.util.Arrays;

public class LinkedPacket extends Packet {

	public static final int PACKET_ID = 1;

	LinkHeader linkHeader;

	public LinkedPacket() {}

	public LinkedPacket(Packet packet) {
		set(packet.asBytes());
	}

	public LinkedPacket(LinkHeader linkHeader) {
		this.linkHeader = linkHeader;
	}

	@Override
	public void set(byte[] data) {
		setLinkHeader(data);
		super.set(Arrays.copyOfRange(data, LinkHeader.SIZE, data.length));
	}

	public void setLinkHeader(byte[] data) {
		linkHeader = new LinkHeader(data[0], data[1], LinkHeader.getProtocol(data[2], data[3]));
	}

	@Override
	public String toString() {
		return "LinkedPacket [linkHeader=" + linkHeader + ", header=" + getHeader() + ", payloads" + getPayloads() + "]";
	}

	@Override
	public byte[] asBytes() {

		byte[] b = null;
		if(linkHeader!=null){
			b = linkHeader.asBytes();
			byte[] p = super.asBytes();

			if(p!=null){
				b = Arrays.copyOf(b, LinkHeader.SIZE+p.length);
				System.arraycopy(p, 0, b, LinkHeader.SIZE, p.length);
			}
		}
		return b;
	}

	@Override
	public int size() {
		return super.size()+LinkHeader.SIZE;
	}

	public LinkHeader getLinkHeader() {
		return linkHeader;
	}

}
