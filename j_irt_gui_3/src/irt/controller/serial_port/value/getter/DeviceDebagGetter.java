package irt.controller.serial_port.value.getter;

import irt.data.RegisterValue;
import irt.data.event.ValueChangeEvent;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.packet.PacketHeader;
import irt.data.packet.Payload;

public class DeviceDebagGetter extends GetterAbstract {

	private Object value;

	public DeviceDebagGetter(LinkHeader linkHeader, int index, int addr, short packetId, byte parameterId) {
		super(linkHeader, new RegisterValue(index, addr, null), Packet.IRT_SLCP_PACKET_ID_DEVICE_DEBAG, parameterId, packetId);
	}

	@Override
	public boolean set(Packet packet) {
		boolean isSet = false;
		if(isAddressEquals(packet)) {

			PacketHeader cph = packet.getHeader();

			short id = getPacketId();

			if(cph!=null && id==cph.getPacketId()){

				Object source = value;

				if(cph.getOption()>0 || cph.getType()!=Packet.IRT_SLCP_PACKET_TYPE_RESPONSE){
					if(cph.getType()!=Packet.IRT_SLCP_PACKET_TYPE_RESPONSE)
						source=new Byte((byte) -20);
					else
						source = new Byte((byte)-cph.getOption());
				}else{

					Payload cpl = packet.getPayload(getPacketParameterHeaderCode());


					if(cpl!=null && cpl.getParameterHeader().getSize()==12){
						source = cpl.getRegisterValue();
					}

				}
				if(source!=null && !source.equals(value)){
					value = source;
					fireValueChangeListener(new ValueChangeEvent(source, id));
				}

				isSet = true;
			}
		}
		return isSet;
	}
}
