
package irt.data.packet;

import irt.data.RegisterValue;

public class RegisterPacket extends PacketAbstract {

	public RegisterPacket(Byte linkAddr, RegisterValue value, short packetId) {
		super(
				linkAddr,
				value.getValue()!=null
				? PacketImp.PACKET_TYPE_COMMAND
						: PacketImp.PACKET_TYPE_REQUEST,
				packetId,
				PacketImp.GROUP_ID_DEVICE_DEBAG,
				PacketImp.PARAMETER_DEVICE_DEBAG_READ_WRITE,
				value.toBytes(),
				value.getValue()!=null
					? Priority.COMMAND
						: Priority.REQUEST);
	}

	@Override
	public void setValue(Object source) {

		if(source instanceof RegisterValue){
			getPayload(0).setBuffer(((RegisterValue)source).toBytes());
		}
	}
}
