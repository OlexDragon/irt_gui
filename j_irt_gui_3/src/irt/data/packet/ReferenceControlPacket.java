package irt.data.packet;

import irt.data.PacketWork;

public class ReferenceControlPacket extends PacketAbstract{

	public enum ReferenceStatus{
		UNKNOWN,
		ON,
		OFF
	}
	/**
	 *  Converter request packet
	 */
	public ReferenceControlPacket() {
		this(null);
	}

	public ReferenceControlPacket(ReferenceStatus referenceStatus) {
		super(
				(byte)0,
				referenceStatus!=null ? PacketImp.PACKET_TYPE_COMMAND : PacketImp.PACKET_TYPE_REQUEST,
						PacketWork.PACKET_ID_CONFIGURATION_REFERENCE_CONTROL,
						PacketImp.GROUP_ID_CONFIGURATION,
						PacketImp.PARAMETER_CONFIG_FCM_ALC_REFERENCE_CONTROL,
						referenceStatus!=null ? new byte[]{Integer.valueOf(referenceStatus.ordinal()).byteValue()} : null,
								referenceStatus!=null ? Priority.COMMAND : Priority.REQUEST);
	}

	@Override
	public void setValue(Object source) {
		if(source instanceof ReferenceStatus){
			getPayload(0).setBuffer(Integer.valueOf(((ReferenceStatus)source).ordinal()).byteValue());
		}
	}
}
