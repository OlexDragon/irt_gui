
package irt.data;

import java.util.Arrays;

import irt.services.ObjectParsingException;

public class NetworkAddress {

	public static final int SIZE = 13;

	public NetworkAddress() throws ObjectParsingException {
		this(new byte[SIZE]);
	}

	public NetworkAddress(byte...bytes) throws ObjectParsingException {

		if(bytes.length<13)
			throw new ObjectParsingException("\n\t Argument length is to short");

		type 		= NetworkAddressType.values()[bytes[0]];
		ipAddress 	= Arrays.copyOfRange(bytes, 1, 5);
		mask		= Arrays.copyOfRange(bytes, 5, 9);
		gateway		= Arrays.copyOfRange(bytes, 9, SIZE);
	}

	private NetworkAddressType type; 	public NetworkAddressType 	getType		() { return type; 		} public void setType		(NetworkAddressType type) { this.type = type; 			}
	private byte[] ipAddress; 			public byte[] 				getIpAddress() { return ipAddress; 	} public void setIpAddress	(byte[] ipAddress		) { this.ipAddress = ipAddress; }
	private byte[] mask; 				public byte[] 				getMask		() { return mask; 		} public void setMask		(byte[] mask			) { this.mask = mask; 			}
	private byte[] gateway; 			public byte[] 				getGateway	() { return gateway; 	} public void setGateway	(byte[] gateway			) { this.gateway = gateway; 	}

	public byte[] toBytes(){
		return  new byte[]{(byte) type.ordinal(), ipAddress[0], ipAddress[1], ipAddress[2], ipAddress[3], mask[0], mask[1], mask[2], mask[3], gateway[0], gateway[1], gateway[2], gateway[3]};
	}

	@Override
	public String toString() {
		return "NetworkAddress [type=" + type + ", ipAddress=" + Arrays.toString(ipAddress) + ", mask="
				+ Arrays.toString(mask) + ", gateway=" + Arrays.toString(gateway) + "]";
	}

	public enum NetworkAddressType {
		UNKNOWN,
		STATIC,
		DHCP
	}
}
