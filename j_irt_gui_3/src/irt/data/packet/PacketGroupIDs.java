package irt.data.packet;

import java.util.Arrays;

/*	User interface.
 *  Packet ID.
 *  Packet ID represents unique identifier of “command/request – response” transaction.
 *  Packet ID is generated on the client side and is copied to response message on the server side.
 *   Acknowledgement message always contains ID of the received packet acknowledgement was sent on. */
public enum PacketGroupIDs {
	NONE((byte)0),		/* Reserved for special use. */
	ALARM			((byte)1),		/* Alarm: message content is product specific. */
	CONFIGURATION	((byte)2),		/* Configuration: content is product specific. */
	FILETRANSFER	((byte)3),		/* File transfer: software upgrade command (optional). */
	MEASUREMENT		((byte)4),		/* Measurement: device status, content is product specific. */
	RESET			((byte)5),		/* Device reset: generic command. */
	DEVICE_INFO		((byte)8),		/* Device information: generic command. */
	CONTROL			((byte)9),		/* Device control operations. Save configuration: generic command. */
	PROTO			((byte)10),
	REDUNDANCY		((byte)12),
	DEVICE_DEBUG	((byte)61),		/* Device Debug. */

/* Protocol */
	PROTOCOL ((byte)10), /* Packet protocol parameters configuration and monitoring. */

/* Network */
	NETWORK ((byte)11), /* Network configuration. */

/* backwards compatibility - to be deleted */
	PRODUCTION_GENERIC_SET_1 ((byte)100),
	DEVELOPER_GENERIC_SET_1 ((byte)120);

	private final byte id;

	private PacketGroupIDs(byte id) {
		this.id = id;
	}

	public byte getId() {
		return id;
	}

	public boolean match(byte groupId) {
		return groupId == id;
	}

	public String toString() {
		return name() + "(" + id + ")";
	}

	public static PacketGroupIDs valueOf(byte packetId) {
		return Arrays.stream(values()).filter(pId->pId.id==packetId).findAny().orElse(NONE);
	}
}
