package irt.controller.serial_port.value.getter;

import irt.data.PacketThread;
import irt.data.PacketWork;
import irt.data.event.ValueChangeEvent;
import irt.data.network.NetworkAddress;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.packet.PacketHeader;
import irt.data.packet.Payload;

public class Getter extends GetterAbstract {

	private long value;

	public Getter(LinkHeader linkHeader, byte groupId,	byte packetParameterHeaderCode, short packetId) {
		super(linkHeader, Packet.IRT_SLCP_PACKET_TYPE_REQUEST, groupId, packetParameterHeaderCode, packetId);
	}

	public <T> Getter(LinkHeader linkHeader, byte groupId,	byte packetParameterHeaderCode, short packetId, T value) {
		super(linkHeader, Packet.IRT_SLCP_PACKET_TYPE_REQUEST, groupId, packetParameterHeaderCode, packetId, value);
	}

	@Override
	public boolean set(Packet packet) {
		boolean isSet = false;
		if(packet!=null) {

			PacketHeader cph = packet.getHeader();

			PacketThread upt = getPacketThread();
			Packet up = upt.getPacket();

			if(cph!=null && up!=null &&
					cph.getType()==Packet.IRT_SLCP_PACKET_TYPE_RESPONSE &&
						cph.getGroupId()==up.getHeader().getGroupId() &&
							cph.getPacketId()==getPacketId()){

				long tmp = value;
				Object source = null;

				byte error = cph.getOption();
				if(error>0){
					tmp = -error;
					source = cph.getOptionStr();
				}else{
					Payload pl = packet.getPayload(getPacketParameterHeaderCode());

					if(pl!=null){
						int size = pl.getParameterHeader().getSize();
						switch(size){
						case 4:
							tmp = (Integer) (source = new Integer(pl.getInt(0)));
							break;
						case 13:
							NetworkAddress networkAddress = new NetworkAddress();
							networkAddress.set(packet);
							source = networkAddress;
							tmp = networkAddress.hashCode();
							break;
						default:
							switch(cph.getPacketId()){
							case PacketWork.PACKET_ID_DUMP_DEVICE_DEBAG_DEVICE_INFO_0:
							case PacketWork.PACKET_ID_DUMP_DEVICE_DEBAG_DEVICE_INFO_1:
							case PacketWork.PACKET_ID_DUMP_REGISTER_1:
							case PacketWork.PACKET_ID_DUMP_REGISTER_2:
							case PacketWork.PACKET_ID_DUMP_REGISTER_3:
							case PacketWork.PACKET_ID_DUMP_REGISTER_4:
							case PacketWork.PACKET_ID_DUMP_REGISTER_5:
							case PacketWork.PACKET_ID_DUMP_REGISTER_6:
							case PacketWork.PACKET_ID_DUMP_REGISTER_100:
								source = pl.getStringData();
								tmp= value+1;//I need all dumps. So tmp!=value
								break;
							case PacketWork.PACKET_ID_ALARMS:
								source = pl.getArrayShort();
								tmp = source.hashCode();
								break;
							default:
								source = pl.getStringData();
								tmp = source.hashCode();
							}
							logger.trace("source: {}; Value: {}; tmp: {}; equales: {}; {}", source, value, tmp, value==tmp, packet);
						}
					}
					isSet = true;
				}

				if(source!=null && tmp!=value){
					value = tmp;
					fireValueChangeListener(new ValueChangeEvent(source, getPacketId()));
				}

			}
		}
		return isSet;
	}

}
