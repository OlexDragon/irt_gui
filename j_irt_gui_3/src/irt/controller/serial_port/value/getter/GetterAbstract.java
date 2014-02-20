package irt.controller.serial_port.value.getter;

import irt.data.LinkedPacketThread;
import irt.data.PacketThread;
import irt.data.PacketWork;
import irt.data.RegisterValue;
import irt.data.packet.LinkHeader;
import irt.data.packet.LinkedPacket;
import irt.data.packet.Packet;
import irt.data.packet.ParameterHeader;
import irt.data.packet.Payload;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class GetterAbstract extends ValueChangeListenerClass implements PacketWork {

	private LinkHeader linkHeader;
	private byte packetType;
	private byte groupId;
	private byte packetParameterHeaderCode;
	private short packetId;

	protected PacketThread packetThread;

	public GetterAbstract(LinkHeader linkHeader, byte groupId,	byte packetParameterHeaderCode, short packetId) {
		this(linkHeader, Packet.IRT_SLCP_PACKET_TYPE_REQUEST, groupId, packetParameterHeaderCode, packetId);
	}

	public GetterAbstract(LinkHeader linkHeader, byte packetType, byte groupId, byte packetParameterHeaderCode, short packetId) {
		logger.trace("packetType={},groupId={},packetParameterHeaderCode={},packetId={}", packetType, groupId, packetParameterHeaderCode, packetId);
		this.linkHeader = linkHeader;
		this.packetType = packetType;
		this.groupId = groupId;
		this.packetParameterHeaderCode = packetParameterHeaderCode;
		this.packetId = packetId;
		logger.trace(Arrays.toString(getCommand()));
		logger.trace(linkHeader);
		packetThread = linkHeader!=null ? new LinkedPacketThread(linkHeader, getCommand(), "LinkedPacketId="+packetId) : new PacketThread(getCommand(), "PacketId="+packetId);
	}

	public <T> GetterAbstract(LinkHeader linkHeader, byte packetType, byte groupId, byte packetParameterHeaderCode, short packetId, T value) {
		this(linkHeader, packetType, groupId, packetParameterHeaderCode, packetId);
		byte[] data = packetThread.getData();
		byte[] bytesValue = Packet.toBytes(value);
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

	public void setPacketType(byte packetType){
		packetThread.setType(packetType);
		packetThread.preparePacket();
	}

	protected byte[] getCommand() {

		byte[] packetId = Packet.toBytes(this.packetId);

		return new byte[]{	packetType,		// Packet Type
				packetId[0],
				packetId[1],
				groupId,
				0,							// Reserved
				0,							// Reserved
				Packet.ERROR_NO_ERROR,		// Error
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

	public PacketThread getPacketThread() {
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

	@Override
	public String toString() {
		return "AbstractGetter [packetThread=" + packetThread + "]"+super.toString();
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
		return getPriority()<o.getPriority() ? 1 : getPriority()>o.getPriority() ? -1 : 0;
	}

	public Integer getPriority() {
		return 0;
	}

	public LinkHeader getLinkHeader() {
		return linkHeader;
	}

	@Override
	public boolean isAddressEquals(Packet packet) {
		boolean addrEquals;

		if(packet!=null){
			if(packet instanceof LinkedPacket){
				byte addr = ((LinkedPacket)packet).getLinkHeader().getAddr();
				addrEquals = linkHeader.getAddr() == addr;
			}else if(linkHeader==null)
				addrEquals = true;
			else
				addrEquals = false;
		}else
			addrEquals = false;

		return addrEquals;
	}
}
