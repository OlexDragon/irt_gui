package irt.gui.data.packet;

//import irt.controller.DumpControllers;
//import irt.data.PacketWork;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.data.PacketIdDetails;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketErrors;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketGroupId;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketId;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketType;
import irt.gui.errors.PacketParsingException;;

public class PacketHeader{

	private final Logger logger = (Logger) LogManager.getLogger();

	public static final int SIZE = 7;

	/*
	  	private byte	type;		0
	  	private short 	packetId;	1,2
		private byte 	groupId;	3
		private short 	reserved;	4,5
		private byte 	code; 		6
	 */
	private PacketType		packetType;						public PacketType 		getPacketType() 	{ return packetType; 		}
	private PacketIdDetails packetIdDetails; 				public PacketIdDetails 	getPacketIdDetails(){ return packetIdDetails; 	}
	/*private PacketGroupId 	packetGroupId;*/			public PacketGroupId 	getPacketGroupId() 	{ return packetIdDetails.getPacketId().getPacketGroupId(); 	}
	private byte[]			reserved = new byte[]{0, 0}; 	public short			getReserved()		{ return (short) Packet.shiftAndAdd(reserved);	}
	private PacketErrors 	packetError;					public PacketErrors		getPacketErrors() 	{ return packetError;		}

	public PacketHeader(PacketType packetType, PacketIdDetails packetIdDetails, PacketErrors packetError) {
		this.packetType = packetType;
		this.packetIdDetails = packetIdDetails;
		this.packetError = packetError;
	}

	public PacketHeader(byte[] packetInBytes) throws PacketParsingException {

		if(packetInBytes==null || packetInBytes.length<11)
			throw new PacketParsingException(packetInBytes==null ? "\n\tThe Constructor Parameter can not be null." : "\n\tThe Constructor Parameter length is " + packetInBytes.length + ". It can not be less than 11");

		setPacketType	(packetInBytes[4]);

		byte[] copyOfRange = Arrays.copyOfRange(packetInBytes, 5, 7);
		short shiftAndAdd = (short)Packet.shiftAndAdd(copyOfRange);
		logger.trace("\n\t{}\n\t{}", copyOfRange, shiftAndAdd);

		setPacketIdDetails(shiftAndAdd);
		setPacketAlarms(packetInBytes[10]);
	}

	private void setPacketAlarms(byte errorAsByte) {
		for(PacketErrors a:PacketErrors.values())
			if(a.getValue()==errorAsByte)
				packetError = a;

		if(packetError==null){
			packetError = PacketErrors.UNKNOWN;
			logger.warn("Unknown alarm {}", errorAsByte);
		}
	}

	private void setPacketIdDetails(short PacketIdDetailsAsShort) throws PacketParsingException {
		logger.entry(PacketIdDetailsAsShort);

		for(PacketId id:PacketId.values())
			if(id.getValue()==PacketIdDetailsAsShort)
				packetIdDetails = new PacketIdDetails(id, null);

		if(packetIdDetails==null)
			throw new PacketParsingException("\n\tParsing 'Packet ID' ERROR.("+ PacketIdDetailsAsShort +")");
	}

	private void setPacketType(byte packetTypeAsByte) throws PacketParsingException {
		for(PacketType t:PacketType.values()) {
			if(t.getValue()==packetTypeAsByte)
				packetType = t;
		}

		if(packetType==null)
			throw new PacketParsingException("\n\tParsing 'Packet Type' ERROR.("+ packetTypeAsByte +")");
	}


/**
 * @return { packetType[0], packetId[1,2], groupId[3], reserved[4,5], error[6] }
 */
	public byte[]	toBytes(){

		PacketId packetId = packetIdDetails.getPacketId();
		short value = packetId.getValue();
		byte[] packetIdAsBytes = Packet.toBytes(value);
		PacketGroupId packetGroupId = packetId.getPacketGroupId();

		return new byte[]{packetType.getValue(), packetIdAsBytes[0], packetIdAsBytes[1], packetGroupId.getValue(), reserved[0], reserved[1], packetError.getValue()};
	}

	@Override
	public String toString() {
		return "\n\t PacketHeader [packetType=" + packetType + ", packetIdDetails=" + packetIdDetails + ", reserved=" + Arrays.toString(reserved) + ", packetAlarms=" + packetError + "]";
	}
}
