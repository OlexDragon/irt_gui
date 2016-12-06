package irt.packet.enums;

public enum PacketErrors {
	NO_ERROR					((byte) 0),
	SYSTEM_INTERNAL				((byte) 1),
	WRITE_ERROR					((byte) 2),
	FUNCTION_NOT_IMPLEMENTED	((byte) 3),
	VALUE_OUTSIDE_OF_VALID_RANGE((byte) 4),
	CAN_NOT_BE_GENERATED		((byte) 5),
	CAN_NOT_BE_EXECUTED			((byte) 6),
	INVALID_DATA_FORMAT			((byte) 7),
	INVALID_VALUE				((byte) 8),
	NOT_ENOUGH_MEMORY			((byte) 9),
	REQUESTED_ELEMENT_NOT_FOUND	((byte)10),
	TIMED_OUT					((byte)11),
	COMMUNICATION_PROBLEM		((byte) 20),
	UNKNOWN						(Byte.MAX_VALUE);

	private byte value; 		public byte getValue()	{ return value; }

	private PacketErrors(byte value){
		this.value = value;
	}

	public static PacketErrors valueOf(short value){
		PacketErrors packetError = UNKNOWN;

		for(PacketErrors p:values())
			if(p.getValue()==value){
				packetError = p;
				break;
			}

		return packetError;
	}

	@Override
	public String toString(){
		return name() + "(" + value + ")";
	}
}
