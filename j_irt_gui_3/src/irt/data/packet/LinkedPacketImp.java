package irt.data.packet;

import java.util.Arrays;
import java.util.Optional;

import irt.data.PacketThread;
import irt.data.packet.interfaces.LinkedPacket;

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
	public byte[] getData() {
		byte[] b = null;
		if(linkHeader!=null){
			b = linkHeader.toBytes();
			byte[] p = super.getData();

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

	@Override
	public byte[] getAcknowledg() {

		final byte[] b = Optional.ofNullable(linkHeader).filter(lh->lh.getAddr()!=0).map(LinkHeader::toBytes).map(bs->Arrays.copyOf(bs, 7)).orElseGet(()->new byte[3]);
		final byte[] idBytes = getHeader().packetIdAsBytes();

		int idPosition = b.length-3;
		b[idPosition] = (byte) 0xFF;
		b[++idPosition] = idBytes[0];
		b[++idPosition] = idBytes[1];

		return PacketThread.preparePacket(b);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((linkHeader == null) ? 0 : linkHeader.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof LinkedPacket))
			return false;
		LinkedPacket other = (LinkedPacket) obj;
		if (linkHeader == null) {
			if (other.getLinkHeader() != null)
				return false;
		} else if (!linkHeader.equals(other.getLinkHeader()))
			return false;
		return true;
	}

}
