
package irt.data.packet;

import java.util.List;

import irt.data.PacketThread;
import irt.data.PacketThreadWorker;
import irt.data.PacketWork;
import irt.data.listener.ValueChangeListener;

public class PacketAbstract implements PacketWork, PacketThreadWorker, LinkedPacket{

	private int priority;

	private final LinkHeader linkHeader;
	private final PacketHeader header;
	private final Payload payload;

	protected PacketAbstract(byte addr, byte packetType, byte groupId, byte command, Short alarmId, int priority){
		linkHeader = new LinkHeader(addr, (byte)0, (short)0);
		header = new PacketHeader();
		header.setType(packetType);
		header.setGroupId(groupId);
		header.setPacketId((short) PacketImp.shiftAndAdd(addr, command));
		payload = new Payload( new ParameterHeader( command, PacketImp.toBytes((short)2)), alarmId!=null ? PacketImp.toBytes(alarmId) : null );
		this.priority = priority;
	}

	@Override
	public int compareTo(PacketWork packetWork) {
		return Integer.compare(priority, packetWork.getPriority());
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public void setPriority(int priority) {
		this.priority = priority;
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
		final byte[] l = linkHeader.toBytes();
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
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public Payload getPayload(int i) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public boolean set(Packet packet) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public String toString() {
		return "\n\t" + getClass().getSimpleName()
				+ " [priority=" + priority + ", linkHeader=" + linkHeader + ", header=" + header + ", payload="
				+ payload + "]";
	}

	@Override
	public byte[] toBytes() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
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
}
