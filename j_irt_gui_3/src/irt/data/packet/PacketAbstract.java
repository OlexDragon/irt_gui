
package irt.data.packet;

import java.util.ArrayList;
import java.util.List;

import irt.data.PacketThread;
import irt.data.PacketThreadWorker;
import irt.data.PacketWork;
import irt.data.listener.ValueChangeListener;

public class PacketAbstract implements PacketWork, PacketThreadWorker, LinkedPacket{

	private Priority priority;

	private final LinkHeader linkHeader;
	private final PacketHeader header;
	private final Payload payload;

	protected PacketAbstract(byte linkAddr, byte packetType, short packetId, byte groupId, byte payloadCommand, byte[] payloadData, Priority priority){
		linkHeader = linkAddr>0 ? new LinkHeader(linkAddr, (byte)0, (short)0) : null;
		header = new PacketHeader();
		header.setType(packetType);
		header.setGroupId(groupId);
		header.setPacketId(packetId);
		payload = new Payload( new ParameterHeader( payloadCommand), payloadData);
		this.priority = priority;
	}

	@Override
	public int compareTo(PacketWork packetWork) {
		return priority.compareTo(packetWork.getPriority());
	}

	@Override
	public Priority getPriority() {
		return priority;
	}

	@Override	// function of java.lang.Thread
	public void setPriority(int priority) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public PacketThreadWorker getPacketThread() {
		return this;
	}

	@Override
	public void addVlueChangeListener(ValueChangeListener valueChangeListener) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public void removeVlueChangeListener(ValueChangeListener valuechangelistener) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public void removeVlueChangeListeners() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public boolean isAddressEquals(Packet packet) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public void join() throws InterruptedException {
	}

	@Override
	public void join(long i) throws InterruptedException {
	}

	@Override
	public Packet getPacket() {
		return this;
	}

	@Override
	public byte[] getData() {
		final byte[] l = linkHeader!=null ? linkHeader.toBytes() : new byte[0];
		final byte[] h = header.toBytes();
		final byte[] p = payload.toBytes();
		return PacketThread.preparePacket(PacketImp.concatAll(l, h, p));
	}

	@Override
	public boolean isReadyToSend() {
		return true;
	}

	@Override
	public void start() {
	}

	@Override
	public Object getValue() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public LinkHeader getLinkHeader() {
		return linkHeader;
	}

	@Override
	public void setData(byte[] d) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public void preparePacket() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public void preparePacket(byte packetParameterHeaderCode, Object value) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public void setDataPacketTypeCommand() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public void setValue(Object source) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public void preparePacket(byte value) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public PacketHeader getHeader() {
		return header;
	}

	@Override
	public void setHeader(PacketHeader packetHeader) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public void setPayloads(List<Payload> payloadsList) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public List<Payload> getPayloads() {
		final ArrayList<Payload> arrayList = new ArrayList<>();
		if(payload!=null)
			arrayList.add(payload);
		return arrayList;
	}

	@Override
	public Payload getPayload(int i) {
		return payload;
	}

	@Override
	public boolean set(Packet packet) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public byte[] toBytes() {
		return getData();
	}

	@Override
	public void set(byte[] data) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public Payload getPayload(byte parameterId) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = prime + ((header == null) ? 0 : header.getPacketId());
		result = prime * result + ((payload == null) ? 0 : payload.getParameterHeader().getCode());
		return prime * result + ((linkHeader == null) ? 0 : linkHeader.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;

		if (!(obj instanceof LinkedPacket))
			return false;
		LinkedPacket other = (LinkedPacket) obj;
		if (header == null) {
			if (other.getHeader() != null)
				return false;
		} else if (header.getPacketId()!=other.getHeader().getPacketId())
			return false;
		if (linkHeader == null) {
			if (other.getLinkHeader() != null)
				return false;
		} else if (!linkHeader.equals(other.getLinkHeader()))
			return false;
		if (payload == null) {
			if (other.getPayloads().isEmpty())
				return false;
		} else if (payload.getParameterHeader().getCode()!=(other.getPayloads().get(0).getParameterHeader().getCode()))
			return false;
		
		return true;
	}

	@Override
	public String toString() {
		return "\n\t" + getClass().getSimpleName()
				+ " [priority=" + priority + ", linkHeader=" + linkHeader + ", header=" + header + ", payload=" + payload + "]";
	}

	public enum Priority {
		REQUEST,
		RANGE,
		ALARM,
		COMMAND
	}
}
