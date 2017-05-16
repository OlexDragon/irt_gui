package irt.controller.serial_port.value.getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import irt.data.LinkedPacketThread;
import irt.data.PacketThread;
import irt.data.RegisterValue;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.packet.PacketAbstract.Priority;
import irt.data.packet.interfaces.LinkedPacket;
import irt.data.packet.interfaces.PacketThreadWorker;
import irt.data.packet.interfaces.PacketWork;
import irt.data.packet.PacketImp;
import irt.data.packet.ParameterHeader;
import irt.data.packet.Payload;

public abstract class GetterAbstract extends ValueChangeListenerClass implements PacketWork {

	private LinkHeader linkHeader;
	private byte packetType;
	private byte groupId;
	private byte packetParameterHeaderCode;
	private short packetId;

	protected PacketThread packetThread;

	public GetterAbstract(LinkHeader linkHeader, byte groupId,	byte packetParameterHeaderCode, short packetId) {
		this(linkHeader, PacketImp.PACKET_TYPE_REQUEST, groupId, packetParameterHeaderCode, packetId);
	}

	public GetterAbstract(LinkHeader linkHeader, byte packetType, byte groupId, byte packetParameterHeaderCode, short packetId) {
		this.linkHeader = linkHeader!=null ? linkHeader : new LinkHeader((byte)0, (byte)0, (short) 0);
		this.packetType = packetType;
		this.groupId = groupId;
		this.packetParameterHeaderCode = packetParameterHeaderCode;
		this.packetId = packetId;
		byte[] command = getCommand();
		logger.trace("\n\tpacketType=\t{},\n\t"
				+ "groupId=\t{},\n\t"
				+ "packetParameterHeaderCode=\t{},\n\t"
				+ "packetId=\t{},\n\t"
				+ "getCommand()=\t{},\n\t"
				+ "linkHeader=\t{}",
				packetType,
				groupId,
				packetParameterHeaderCode,
				packetId,
				Arrays.toString(command),
				linkHeader);
		packetThread = this.linkHeader.getAddr()!=0 ? new LinkedPacketThread(linkHeader, command, "LinkedPacketId="+packetId) : new PacketThread(command, "PacketId="+packetId);
	}

	public <T> GetterAbstract(LinkHeader linkHeader, byte packetType, byte groupId, byte packetParameterHeaderCode, short packetId, T value) {
		this(linkHeader, packetType, groupId, packetParameterHeaderCode, packetId);
		byte[] data = packetThread.getData();
		byte[] bytesValue = PacketImp.toBytes(value);
		int length = data.length;
		data[length-1] = (byte) bytesValue.length;
		data = Arrays.copyOf(data, length+bytesValue.length);
		System.arraycopy(bytesValue, 0, data, length, bytesValue.length);
		packetThread.setData(data);
	}

	public GetterAbstract(LinkHeader linkHeader, RegisterValue registerValue, byte groupId, byte packetParameterHeaderCode, short packetId) {
		this(linkHeader, groupId, packetParameterHeaderCode, packetId);
		packetThread.setValue(registerValue);
	}

	public byte getPacketType() {
		return packetType;
	}

	public void setPacketType(byte packetType){
		packetThread.setType(packetType);
		packetThread.preparePacket();
	}

	protected byte[] getCommand() {

		byte[] packetId = PacketImp.toBytes(this.packetId);

		return new byte[]{	packetType,		// Packet Type
				packetId[0],
				packetId[1],
				groupId,
				0,							// Reserved
				0,							// Reserved
				PacketImp.ERROR_NO_ERROR,		// Error
				packetParameterHeaderCode,	// Parameter Header Code (Payload's Header Code)
				0,0};						// Payload's buffer size
	}

	public abstract boolean set(Packet packet);

	@Override
	public boolean equals(Object obj) {
		return obj!=null ? obj.hashCode()==hashCode() : false;
	}

	@Override
	public int hashCode() {
		int tmp = 0;
		if(packetThread instanceof LinkedPacketThread)
			tmp = ((LinkedPacketThread)packetThread).getLinkHeader().getIntAddr()<<16;
		return packetId+tmp;
	}

	@Override
	public PacketThreadWorker getPacketThread() {
		return packetThread;
	}

	public byte getGroupId() {
		return groupId;
	}

	public byte getPacketParameterHeaderCode() {
		return packetParameterHeaderCode;
	}

	/**
	 * Create new Payload
	 * @param packetParameterHeaderCode
	 */
	public void setPacketParameterHeaderCode(byte packetParameterHeaderCode) {
		this.packetParameterHeaderCode = packetParameterHeaderCode;
		List<Payload> payloads = new ArrayList<>();
		payloads.add(new Payload(new ParameterHeader(packetParameterHeaderCode), null));
		getPacketThread().getPacket().setPayloads(payloads);
	}

	public short getPacketId() {
		return packetId;
	}

	public void setPacketId(short packetId) {
		this.packetId = packetId;
		packetThread.getPacket().getHeader().setPacketId(packetId);
	}

	public void clear(){
		packetThread.clear();
		packetThread = null;
	}

	@Override
	public int compareTo(PacketWork o) {
		return getPriority().compareTo(o.getPriority());
	}

	public Priority getPriority() {
		return Priority.REQUEST;
	}

	public LinkHeader getLinkHeader() {
		return linkHeader;
	}

	@Override
	public boolean isAddressEquals(Packet packet) {
		boolean addrEquals;

		if(packet!=null){
			if(packet instanceof LinkedPacket){
				final LinkHeader lh = ((LinkedPacket)packet).getLinkHeader();
				byte addr = lh!=null ? lh.getAddr() : 0;
				addrEquals = linkHeader.getAddr() == addr;
			}else if(linkHeader==null || linkHeader.getAddr()==0)
				addrEquals = true;
			else
				addrEquals = false;
		}else
			addrEquals = false;

		return addrEquals;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [packetThread=" + packetThread + "]";
	}
}
