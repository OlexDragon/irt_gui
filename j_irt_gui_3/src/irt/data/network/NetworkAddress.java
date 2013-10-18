package irt.data.network;

import irt.data.packet.Packet;
import irt.data.packet.Payload;

import java.util.Arrays;
import java.util.List;

public class NetworkAddress {

	public enum ADDRESS_TYPE{
		UNKNOWN,
		STATIC,
		DYNAMIC
		
	}
	//{address type (1 byte), IP address (4 bytes), Mask (4 bytes), Gateway (4 bytes)}
	private byte type;
	private byte[] address;
	private byte[] mask;
	private byte[] gateway;

	public void set(Packet packet) {
		if(packet!=null){
			List<Payload> payloads = packet.getPayloads();
			if(payloads.size()>0) {
				byte[] buffer = payloads.get(0).getBuffer();
				if(buffer.length>=13){
					type = buffer[0];
					address = Arrays.copyOfRange(buffer, 1, 5);
					mask = Arrays.copyOfRange(buffer, 5, 9);
					gateway = Arrays.copyOfRange(buffer, 9,13);
				}
			}
		}
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

	protected String asString(byte[] buffer, String splitter) {
		String returnStr = "";
		for(byte b:buffer){
			if(!returnStr.isEmpty())
				returnStr += splitter;
			returnStr += b&0x00FF;
		}
		return returnStr;
	}

	@Override
	public String toString() {
		return "NetworkAddress [type=" + getTypeAsString() + ", address=" + getAddressAsString() + ", mask=" + getMaskAsString() + ", gateway=" + getGatewayAsString()
				+ "]";
	}

	public String getTypeAsString() {
		return ADDRESS_TYPE.values()[type].toString();
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
}
