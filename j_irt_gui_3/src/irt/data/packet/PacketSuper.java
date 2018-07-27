
package irt.data.packet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import irt.controller.serial_port.value.setter.ConfigurationSetter;
import irt.data.PacketThread;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.PacketImp.PacketGroupIDs;
import irt.data.packet.interfaces.LinkedPacket;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.interfaces.PacketThreadWorker;

public class PacketSuper implements PacketWork, PacketThreadWorker, LinkedPacket{

	private Priority priority;

	private LinkHeader linkHeader;
	private PacketHeader header;
	private List<Payload> payloads = new ArrayList<>();
	private long timestamp;

	protected PacketSuper(Byte linkAddr, byte packetType, PacketIDs packetID, PacketGroupIDs groupId, byte parameterHeaderCode, byte[] payloadData, Priority priority){
		linkHeader = Optional.ofNullable(linkAddr).filter(la->la!=0).map(la-> new LinkHeader(linkAddr, (byte)0, (short)0)).orElse(null);
		header = new PacketHeader();
		header.setType(packetType);
		header.setGroupId(groupId.getId());
		header.setPacketId(Optional.ofNullable(packetID).map(PacketIDs::getId).orElseGet(()->(short)(PacketIDs.values().length + new Random().nextInt(Short.MAX_VALUE - PacketIDs.values().length))));
		payloads.add(new Payload( new ParameterHeader( parameterHeaderCode), payloadData));
		this.priority = priority;
		timestamp = System.currentTimeMillis();
	}

	public PacketSuper(ConfigurationSetter configurationSetter) {
		this(
				configurationSetter.getLinkHeader().getAddr(),
				configurationSetter.getPacketType(),
				PacketIDs.valueOf(configurationSetter.getPacketId()).orElse(PacketIDs.UNNECESSARY),
				PacketGroupIDs.valueOf(configurationSetter.getGroupId()),
				configurationSetter.getPacketParameterHeaderCode(),
				null,
				Priority.REQUEST);
	}

	@Override
	public int compareTo(PacketWork packetWork) {

		return Optional

				.of(priority.compareTo(packetWork.getPriority()))
				.filter(c->c!=0)
				.orElse(
						Optional
						.of(packetWork)
						.filter(p->p instanceof PacketSuper)
						.map(PacketSuper.class::cast)
						.map(p->Long.compare(p.timestamp, timestamp))
						.orElseGet(
								()->
								Optional
								.of(packetWork)
								.map(PacketWork::getPacketThread)
								.map(PacketThreadWorker::getPacket)
								.map(Packet::getHeader)
								.map(PacketHeader::getPacketType)
								.filter(t->t==PacketImp.PACKET_TYPE_COMMAND)
								.map(t->1)
								.orElse(priority==Priority.REQUEST ? 0 : -1)));
	}

	@Override
	public Priority getPriority() {
		return priority;
	}

	@Override	// function of java.lang.Thread
	public void setPriority(Priority priority) {
		this.priority = priority;
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
	public void join(){
	}

	@Override
	public void join(long i){
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

		return PacketImp.concatAll(l, h, p);
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
		 return Optional.ofNullable(header).map(PacketHeader::getPacketId).flatMap(PacketIDs::valueOf).flatMap(pId->pId.valueOf(this)).map(Object.class::cast).orElse("not implemented");
	}

	@Override
	public LinkHeader getLinkHeader() {
		return Optional.ofNullable(linkHeader).orElse(new LinkHeader((byte)0, (byte)0, (short) 0));
	}

	public void setLinkHeader(LinkHeader linkHeader) {
		this.linkHeader = linkHeader;
	}

	public PacketSuper setAddr(byte linkAddr) {
		this.linkHeader = linkAddr!=0 ? new LinkHeader(linkAddr, (byte)0, (short)0) : null;
		return this;
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
	public void setValue(Object value) {
		getPayloads()
		.parallelStream()
		.findAny()
		.ifPresent(pl->pl.setBuffer(value));
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
		header = packetHeader;
	}

	@Override
	public void setPayloads(List<Payload> payloadsList) {
		payloads = payloadsList;
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
		return PacketThread.preparePacket(getData());
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
		return 31 + ((header == null) ? 0 : header.getPacketId());
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;

		return Optional

				.ofNullable(obj)
				.filter(Packet.class::isInstance)
				.map(Packet.class::cast)
				.map(Packet::getHeader)
				.map(PacketHeader::getPacketId)
				.filter(otherId->Optional.ofNullable(header).map(PacketHeader::getPacketId).filter(id->id.equals(otherId)).isPresent())
				.isPresent();
	}

	@Override
	public String toString() {
		return "\n\t" + getClass().getSimpleName() + " [timestamp=" + timestamp + ", priority=" + priority + ", linkHeader=" + linkHeader + ", header=" + header + ", payloads=" + payloads.stream().map(Payload::toString).collect(Collectors.joining(",")) + "]";
	}

	public enum Priority {
		REQUEST,
		RANGE,
		ALARM,
		IMPORTANT,
		COMMAND
	}

	@Override
	public byte[] getAcknowledg() {

		final byte[] b = Optional.ofNullable(linkHeader).filter(lh->lh.getAddr()!=0).map(LinkHeader::toBytes).map(bs->Arrays.copyOf(bs, 7)).orElseGet(()->new byte[3]);
		final byte[] idBytes = header.packetIdAsBytes();

		int idPosition = b.length-3;
		b[idPosition] = (byte) 0xFF;
		b[++idPosition] = idBytes[0];
		b[++idPosition] = idBytes[1];

		return PacketThread.preparePacket(b);
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
}
