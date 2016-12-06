package irt.packet.enums;

public enum PacketGroupId {

	NONE			((byte) 0x00),		/* Reserved for special use. */
	ALARM			((byte) 0x01),		/* Alarm: message content is product specific. */
	CONFIGURATION	((byte) 0x02),		/* Configuration: content is product specific. */
	FILETRANSFER	((byte) 0x03),		/* File transfer: software upgrade command (optional). */
	MEASUREMENT		((byte) 0x04),		/* Measurement: device status, content is product specific. */
	RESET			((byte) 0x05),		/* Device reset: generic command. */
	DEVICE_INFO		((byte) 0x08),		/* Device information: generic command. */
	CONFIG_PROFILE	((byte) 0x09),		/* Save configuration: generic command. */
	PROTOCOL		((byte) 0x0A),
	NETWORK			((byte) 0x0B),
	DEVICE_DEBAG	((byte) 0x3D), 		/* 0x3D Device Debug. */
	PRODUCTION		((byte)	0X64);

	byte value = 0; 			public byte getValue() { return value; }

	private PacketGroupId(byte value){
		this.value = value;
	}

	@Override
	public String toString(){
		return name() + ":" + value;
	}
}
