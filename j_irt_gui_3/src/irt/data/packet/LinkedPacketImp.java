package irt.data.packet;

import java.util.Arrays;

public class LinkedPacketImp extends PacketImp implements LinkedPacket{

	public static final int PACKET_ID = 1;

	LinkHeader linkHeader;

	public LinkedPacketImp() {}

	public LinkedPacketImp(PacketImp packet) {
		set(packet.toBytes());
	}

	public LinkedPacketImp(LinkHeader linkHeader) {
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
		return "LinkedPacket [\n\tlinkHeader=\n\t\t" + linkHeader + ",\n\theader=\n\t\t" + getHeader() + ",\n\tpayloads" + getPayloads() + "]";
	}

	@Override
	public byte[] toBytes() {

		byte[] b = null;
		if(linkHeader!=null){
			b = linkHeader.toBytes();
			byte[] p = super.toBytes();

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