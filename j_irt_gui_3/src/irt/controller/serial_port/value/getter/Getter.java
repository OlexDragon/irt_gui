package irt.controller.serial_port.value.getter;

import java.util.Arrays;

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

			short packetId = getPacketId();
			if(cph!=null && up!=null &&
					cph.getType()==Packet.IRT_SLCP_PACKET_TYPE_RESPONSE &&
						cph.getGroupId()==up.getHeader().getGroupId() &&
							cph.getPacketId()==packetId){

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
								logger.trace("Dump, source={}", source);
								tmp= value+1;//I need all dumps. So tmp!=value
								break;
							case PacketWork.PACKET_ID_ALARMS:
							case PacketWork.PACKET_ID_ALARMS_OWER_CURRENT:
							case PacketWork.PACKET_ID_ALARMS_OWER_TEMPERATURE:
							case PacketWork.PACKET_ID_ALARMS_PLL_OUT_OF_LOCK:
							case PacketWork.PACKET_ID_ALARMS_UNDER_CURRENT:
								source = pl.getArrayShort();
								logger.trace("PacketWork.PACKET_ID_ALARMS, {}", source);
								tmp = Arrays.hashCode((short[]) source);
								break;
							default:
								source = pl.getStringData();
								logger.trace("default, source=", source);
								tmp = source.hashCode();
							}
						}
					}
					isSet = true;
				}

				if(source!=null && tmp!=value){
					logger.trace("fireValueChangeListener(new ValueChangeEvent(source={}, packetId={}))", source, packetId);
					value = tmp;
					fireValueChangeListener(new ValueChangeEvent(source, packetId));
				}

			}
		}
		return isSet;
	}

}
