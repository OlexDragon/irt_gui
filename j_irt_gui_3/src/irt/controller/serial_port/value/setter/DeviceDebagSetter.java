package irt.controller.serial_port.value.setter;

import org.apache.logging.log4j.LogManager;

import irt.data.PacketThread;
import irt.data.RegisterValue;
import irt.data.event.ValueChangeEvent;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.packet.PacketHeader;
import irt.data.packet.Payload;
import irt.data.value.Value;

public class DeviceDebagSetter extends SetterAbstract {

	private int hashCode;

	public DeviceDebagSetter(LinkHeader linkHeader, int index, short packetId, byte parameterId) {
		this(linkHeader, index, 1, packetId, parameterId);
	}

	public DeviceDebagSetter(LinkHeader linkHeader, int index, int addr, short packetId, byte parameterId) {
		super(linkHeader, new RegisterValue(index, addr, null), Packet.GROUP_ID_DEVICE_DEBAG, parameterId, packetId,
				LogManager.getLogger());
	}

	public DeviceDebagSetter(LinkHeader linkHeader,int index, int addr, short packetId, byte parameterId, int value) {
		super(linkHeader, new RegisterValue(index, addr, new Value(value, 0, Long.MAX_VALUE, 0)), Packet.PACKET_TYPE_COMMAND, Packet.GROUP_ID_DEVICE_DEBAG, parameterId, packetId,
				LogManager.getLogger());
	}

	@Override
	public void preparePacketToSend(Object value) {

		PacketThread pt = getPacketThread();
		pt.preparePacket(getPacketParameterHeaderCode(), (RegisterValue)value);
	}

	@Override
	public boolean set(Packet packet) {
		boolean isSet = false;
		if(isAddressEquals(packet)) {

			PacketHeader cph = packet.getHeader();

			PacketThread upt = getPacketThread();
			Packet up = upt.getPacket();

			if(cph!=null && up!=null && up.getHeader().getPacketId()==cph.getPacketId()){

				Object source = null;

				if(cph.getOption()>0 || cph.getPacketType()!=Packet.PACKET_TYPE_RESPONSE){
					source = new Byte((byte) -cph.getOption());
					if((Byte)source==0)
						source=-20;
					hashCode = source.hashCode()+1;// hashCode != source.hashCode()
				}else{

					Payload cpl = packet.getPayload(getPacketParameterHeaderCode());

					if(cpl!=null && cpl.getParameterHeader().getSize()>0){

						source = cpl.getRegisterValue();
					}else
						source = new Boolean(true);

					if(source!=null && source.hashCode()!=hashCode){
							fireValueChangeListener(new ValueChangeEvent(source, cph.getPacketId()));
							hashCode = source.hashCode();
					}

					isSet = true;
				}
			}
		}
		return isSet;
	}

}