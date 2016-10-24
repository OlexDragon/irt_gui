package irt.data.packet;

import irt.data.PacketWork;

public class LnbPowerPacket  extends PacketAbstract{

	private static final short PACKET_ID 	= PacketWork.PACKET_ID_CONFIGURATION_FCM_LNB_POWER;
	private static final byte GROUP_ID 		= PacketImp.GROUP_ID_CONFIGURATION;
	private static final byte PARAMETER 	= PacketImp.PARAMETER_CONFIG_LNB_POWER;

	public enum PowerStatus{
		UNDEFINED0,
		UNDEFINED1,
		ON,
		OFF
	}

	public LnbPowerPacket() {
		super((byte)0, PacketImp.PACKET_TYPE_REQUEST, PACKET_ID, GROUP_ID, PARAMETER, null, Priority.REQUEST);
	}

	public LnbPowerPacket(PowerStatus powerStatus) {
		super((byte)0, PacketImp.PACKET_TYPE_COMMAND, PACKET_ID, GROUP_ID, PARAMETER, new byte[]{(byte) powerStatus.ordinal()}, Priority.COMMAND);
	}
}
