package irt.data.packet;

import java.util.Arrays;
import java.util.Optional;

import irt.data.packet.PacketImp.PacketGroupIDs;
import irt.data.packet.PacketWork.PacketIDs;

public class PacketHeader{

//	private final Logger logger = (Logger) LogManager.getLogger();

	public static final int SIZE = 7;
	byte[] packetHeader;

	public PacketHeader(byte[] data) {

		if(data!=null && data.length>=SIZE)
			packetHeader = Arrays.copyOf(data, SIZE);
	}

	public PacketHeader() {
		packetHeader = new byte[SIZE];
	}

/*	private byte	type;		0
 * 	private short 	packetId;	1,2
	private byte 	groupId;	3
	private short 	reserved;	4,5
	private byte 	code; 		6
*/
	public byte[]	toBytes		()	{ return packetHeader;		}
	public byte		getPacketType()	{ return Optional.ofNullable(packetHeader).map(ph->ph[0]).orElse((byte) 0);	}
	public short	getPacketId	()	{ return (short) PacketImp.shiftAndAdd(new byte[]{packetHeader[1], packetHeader[2]});	}
	public byte[]	packetIdAsBytes	()	{ return new byte[]{packetHeader[1], packetHeader[2]};	}
	public byte		getGroupId	()	{ return Optional.ofNullable(packetHeader).map(ph->ph[3]).orElse((byte) 0);	}
	public short	getReserved	()	{ return (short) Optional.ofNullable(packetHeader).map(ph->(short) PacketImp.shiftAndAdd(Arrays.copyOfRange(packetHeader, 4, 6))).orElse((short) 0);}
	public byte		getOption	()	{ return Optional.ofNullable(packetHeader).map(ph->ph[6]).orElse((byte) 0);	}

	public byte[] set(byte[]data){
		if(data!=null && data.length>=SIZE)
			packetHeader = Arrays.copyOf(data, SIZE);

		return data!=null && data.length>SIZE ? Arrays.copyOfRange(data, SIZE, data.length) : null;
	}

	public void setType		(byte type) 				{ packetHeader[0] = type;}
	public void setPacketId	(short irtSlcpPacketId)		{ System.arraycopy(PacketImp.toBytes(irtSlcpPacketId), 0, packetHeader, 1, 2);	}
	public void setGroupId	(byte irtSlcpPacketGroupId) { packetHeader[3] = irtSlcpPacketGroupId;}
	public void setOption	(byte option)			 	{ packetHeader[6] = option;	}

	public String getPacketIdStr() {
//		LogManager.getLogger().error(getPacketId());
		return PacketIDs.toString(getPacketId());
	}

	public String getOptionStr() {
		return packetHeader!=null ? getOptionStr(getOption()) : null;

	}

	public static String getOptionStr(byte code) {

		if(code<0)
			code = (byte) -code;

		String codeStr = null;
		switch (code) {
		case PacketImp.ERROR_NO_ERROR:
			codeStr = "No error(" + code + ")";
			break;
		case 1:
			codeStr = "System internal(" + code + ")";
			break;
		case 2:
			codeStr = "Write error(" + code + ")";
			break;
		case 3:
			codeStr = "Function not implemented(" + code + ")";
			break;
		case 4:
			codeStr = "Value outside of valid range(" + code + ")";
			break;
		case 5:
			codeStr = "Requested information can’t be generated(" + code + ")";
			break;
		case 6:
			codeStr = "Command can’t be executed(" + code + ")";
			break;
		case 7:
			codeStr = "Invalid data format(" + code + ")";
			break;
		case 8:
			codeStr = "Invalid value(" + code + ")";
			break;
		case 9:
			codeStr = "Not enough memory (" + code + ")";
			break;
		case PacketImp.ERROR_REQUESTED_ELEMENT_NOT_FOUND:
			codeStr = "Requested element not found(" + code + ")";
			break;
		case 11:
			codeStr = "Timed out(" + code + ")";
			break;
		case 20:
			codeStr = "Communication problem(" + code + ")";
			break;
		default:
			codeStr = "" + code;
		}
		return codeStr;
	}

	public String getPacketTypeStr() {
		String typeStr = null;
		if(packetHeader!=null)
		switch(getPacketType()){
		case PacketImp.PACKET_TYPE_SPONTANEOUS:
			typeStr = "Spontaneous("+ PacketImp.PACKET_TYPE_SPONTANEOUS+")";
			break;
		case PacketImp.PACKET_TYPE_RESPONSE:
			typeStr = "Response("+ PacketImp.PACKET_TYPE_RESPONSE+")";
			break;
		case PacketImp.PACKET_TYPE_REQUEST:
			typeStr = "Request("+ PacketImp.PACKET_TYPE_REQUEST+")";
			break;
		case PacketImp.PACKET_TYPE_COMMAND:
			typeStr = "Command("+ PacketImp.PACKET_TYPE_COMMAND+")";
			break;
		case PacketImp.PACKET_TYPE_ACKNOWLEDGEMENT:
			typeStr = "Acknowledgement("+ PacketImp.PACKET_TYPE_ACKNOWLEDGEMENT+")";
			break;
		default:
			typeStr = ""+(getPacketType()&0xFF);
		}
		return typeStr;
	}

	private String getGroupIdStr() {
		return PacketGroupIDs.valueOf(getGroupId()).toString();
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(packetHeader);
	}

	@Override
	public boolean equals(Object obj) {
		return obj!=null ? obj.hashCode()==hashCode() : false;
	}

	@Override
	public String toString() {
		return "PacketHeader [type="+ getPacketTypeStr() + ",packetId=" +getPacketIdStr()+",groupId=" +getGroupIdStr()+",reserved=" +getReserved()+",option=" +getOptionStr()+"]";
	}
}
