
package irt.data.packet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import irt.data.PacketThread;
import irt.data.PacketThreadWorker;
import irt.data.PacketWork;
import irt.data.listener.ValueChangeListener;

public class PacketAbstract implements PacketWork, PacketThreadWorker, LinkedPacket{

	private Priority priority;

	private LinkHeader linkHeader;
	private final PacketHeader header;
	private final List<Payload> payloads = new ArrayList<>();

	protected PacketAbstract(byte linkAddr, byte packetType, short packetId, byte groupId, byte payloadCommand, byte[] payloadData, Priority priority){
		linkHeader = linkAddr!=0 ? new LinkHeader(linkAddr, (byte)0, (short)0) : null;
		header = new PacketHeader();
		header.setType(packetType);
		header.setGroupId(groupId);
		header.setPacketId(packetId);
		payloads.add(new Payload( new ParameterHeader( payloadCommand), payloadData));
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
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public PacketThreadWorker getPacketThread() {
		return this;
	}

	@Override
	public void addVlueChangeListener(ValueChangeListener valueChangeListener) {
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public void removeVlueChangeListener(ValueChangeListener valuechangelistener) {
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public void removeVlueChangeListeners() {
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public boolean isAddressEquals(Packet packet) {
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
		final byte[] p = payloads.stream().map(Payload::toBytes).collect(new Collector<byte[], byte[], byte[]>(){

									private byte[] result;

									@Override
									public Supplier<byte[]> supplier() {
										return ()->new byte[0];
									}

									@Override
									public BiConsumer<byte[], byte[]> accumulator() {
										return (a, b)->{
											result = Optional
															.ofNullable(a)
															.filter(r->a.length>0)
															.map(r->{
																r = new byte[a.length + b.length];
																System.arraycopy(a, 0, result, 0, a.length);
																System.arraycopy(b, 0, result, a.length, b.length);
																return r;
															})
															.orElse(b);
										};
									}

									@Override
									public BinaryOperator<byte[]> combiner() {
										return (a, b)->{
											byte[] result = new byte[a.length + b.length];
											System.arraycopy(a, 0, result, 0, a.length);
											System.arraycopy(b, 0, result, a.length, b.length);
											return result;
										};
									}

									@Override
									public Function<byte[], byte[]> finisher() {
										return a->{
											return result;
										};
									}

									private final Set<Characteristics> set = new HashSet<>();
									@Override
									public Set<Characteristics> characteristics() {
										return set;
									}});

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
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public LinkHeader getLinkHeader() {
		return linkHeader;
	}

	public void setAddr(byte linkAddr) {
		this.linkHeader = linkAddr!=0 ? new LinkHeader(linkAddr, (byte)0, (short)0) : null;
	}

	@Override
	public void setData(byte[] d) {
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public void preparePacket() {
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public void preparePacket(byte packetParameterHeaderCode, Object value) {
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public void setDataPacketTypeCommand() {
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public void setValue(Object source) {
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public void preparePacket(byte value) {
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public PacketHeader getHeader() {
		return header;
	}

	@Override
	public void setHeader(PacketHeader packetHeader) {
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public void setPayloads(List<Payload> payloadsList) {
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public List<Payload> getPayloads() {
		return payloads;
	}

	@Override
	public Payload getPayload(int index) {
		return Optional.of(payloads).filter(pls->pls.size()>index).map(pls->pls.get(index)).orElse(null);
	}

	@Override
	public boolean set(Packet packet) {
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public byte[] toBytes() {
		return getData();
	}

	@Override
	public void set(byte[] data) {
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public Payload getPayload(byte parameterId) {
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = prime + ((header == null) ? 0 : header.getPacketId());
		result = prime * result + payloads.hashCode();
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
		if (payloads.isEmpty()) {
			if (!other.getPayloads().isEmpty())
				return false;
		} else if (!payloads.equals(other.getPayloads()))
			return false;
		
		return true;
	}

	@Override
	public String toString() {
		return "\n\t" + getClass().getSimpleName() + " [priority=" + priority + ", linkHeader=" + linkHeader + ", header=" + header + ", payloads=" + payloads.stream().map(Payload::toString).collect(Collectors.joining(",")) + "]";
	}

	public enum Priority {
		REQUEST,
		RANGE,
		ALARM,
		COMMAND
	}
}
