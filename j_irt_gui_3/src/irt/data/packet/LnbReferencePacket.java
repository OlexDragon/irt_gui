package irt.data.packet;

import irt.data.packet.interfaces.PacketWork;

public class LnbReferencePacket  extends PacketAbstract{

	private static final short PACKET_ID 	= PacketWork.PACKET_ID_CONFIGURATION_FCM_LNB_REFERENCE;
	private static final byte GROUP_ID 		= PacketImp.GROUP_ID_CONFIGURATION;
	private static final byte PARAMETER 	= PacketImp.PARAMETER_CONFIG_FCM_LNB_REFERENCE_CONTROL;

	public enum ReferenceStatus{
		UNDEFINED,
		ON,
		OFF
	}

	public LnbReferencePacket() {
		super((byte)0, PacketImp.PACKET_TYPE_REQUEST, PACKET_ID, GROUP_ID, PARAMETER, null, Priority.REQUEST);
	}

	public LnbReferencePacket(ReferenceStatus referenceStatus) {
		super((byte)0, PacketImp.PACKET_TYPE_COMMAND, PACKET_ID, GROUP_ID, PARAMETER, new byte[]{(byte) referenceStatus.ordinal()}, Priority.COMMAND);
	}
}
