package irt.data.network;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import irt.data.packet.interfaces.Packet;

public class NetworkAddress {

	public enum AddressType{
		UNKNOWN("Unknown"),
		STATIC("Static"),
		DYNAMIC("Dynamic");

		private String description; 	public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }

		AddressType(String description){
			this.description = description;
		}

		@Override
		public String toString(){
			return description;
		}
	}
	//{address type (1 byte), IP address (4 bytes), Mask (4 bytes), Gateway (4 bytes)}
	private byte type;
	private byte[] address;
	private byte[] mask;
	private byte[] gateway;

	public NetworkAddress set(Packet packet) {

		Optional.ofNullable(packet)
		.map(Packet::getPayloads)
		.map(List::stream)
		.orElse(Stream.empty())
		.findAny()
		.map(pl->pl.getBuffer())
		.filter(b->b!=null)
		.filter(b->b.length>=13)
		.ifPresent(b->{
			type = b[0];
			address = Arrays.copyOfRange(b, 1, 5);
			mask = Arrays.copyOfRange(b, 5, 9);
			gateway = Arrays.copyOfRange(b, 9,13);
		});

		return this;
	}

	public String getAddressAsString(){
		return address!=null ? asString(address, ".") : null;
	}

	public String getMaskAsString(){
		return address!=null ? asString(mask, ".") : null;
	}

	public String getGatewayAsString(){
		return address!=null ? asString(gateway, ".") : null;
	}

	public byte[] toBytes(){

		byte[] copyOf = Arrays.copyOf(new byte[]{type}, 13);
		System.arraycopy(address, 0, copyOf, 1, address.length);
		System.arraycopy(mask, 0, copyOf, 5, mask.length);
		System.arraycopy(gateway, 0, copyOf, 9, gateway.length);

		return copyOf;
	}

	protected String asString(byte[] buffer, String splitter) {
		String returnStr = "";
		for(byte b:buffer){
			if(!returnStr.isEmpty())
				returnStr += splitter;
			returnStr += b&0x00FF;
		}
		return returnStr;
	}

	public String getTypeAsString() {
		return AddressType.values()[type].toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NetworkAddress other = (NetworkAddress) obj;
		if (!Arrays.equals(address, other.address))
			return false;
		if (!Arrays.equals(gateway, other.gateway))
			return false;
		if (!Arrays.equals(mask, other.mask))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(address);
		result = prime * result + Arrays.hashCode(gateway);
		result = prime * result + Arrays.hashCode(mask);
		result = prime * result + type;
		return result;
	}

	public NetworkAddress getCopy() {
		NetworkAddress na = new NetworkAddress();

		if(address!=null)
			na.setAddress(Arrays.copyOf(address, address.length));

		if(gateway!=null)
			na.setGateway(Arrays.copyOf(gateway, gateway.length));

		if(mask!=null)
			na.setMask(Arrays.copyOf(mask, mask.length));

		na.setType(type);
		return na;
	}

	public byte getType() {
		return type;
	}

	public byte[] getAddress() {
		return address;
	}

	public byte[] getMask() {
		return mask;
	}

	public byte[] getGateway() {
		return gateway;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public void setAddress(byte[] address) {
		this.address = address;
	}

	public void setAddress(String text) {
		setValue(address, text);
	}

	public void setMask(byte[] mask) {
		this.mask = mask;
	}

	public void setMask(String text) {
		setValue(mask, text);	
	}

	public void setGateway(byte[] gateway) {
		this.gateway = gateway;
	}

	public void setGateway(String text) {
		setValue(gateway, text);	
	}

	public void setType(AddressType at) {
		setType((byte) at.ordinal());
	}

	private void setValue(byte[] field, String text) {
		String[] split = text.split("\\.");
		for(int i=0; i<split.length && i<field.length; i++)
			field[i] = (byte) Integer.parseInt(split[i]);
	}

	@Override
	public String toString() {
		return "NetworkAddress [type=" + getTypeAsString() + ", address=" + getAddressAsString() + ", mask=" + getMaskAsString() + ", gateway=" + getGatewayAsString()
				+ "]";
	}
}
