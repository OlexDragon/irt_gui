package irt.controller.serial_port.value.seter;

import irt.controller.serial_port.value.Getter.GetterAbstract;
import irt.controller.translation.Translation;
import irt.data.IdValue;
import irt.data.IdValueForComboBox;
import irt.data.PacketThread;
import irt.data.PacketWork;
import irt.data.Range;
import irt.data.event.ValueChangeEvent;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.packet.PacketHeader;
import irt.data.packet.Payload;
import irt.data.value.Value;
import irt.data.value.ValueFrequency;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;

public class ConfigurationSetter extends SetterAbstract {

	private long value = Integer.MIN_VALUE;

	/**
	 * @param packetParameterHeaderCode by default set to Packet.IRT_SLCP_PARAMETER_FCM_CONFIG_FREQUENCY
	 */
	public ConfigurationSetter() {
		this(null);
	}

	/**
	 * @param linkHeader - address
	 * @param packetParameterHeaderCode by default set to Packet.IRT_SLCP_PARAMETER_25W_BAIS_CONFIGURATION_LO_FREQUENCIES
	 */
	public ConfigurationSetter(LinkHeader linkHeader) {
		this(linkHeader,
				linkHeader!=null ? Packet.IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_LO_FREQUENCIES :
					Packet.IRT_SLCP_PARAMETER_FCM_CONFIG_FREQUENCY_RANGE,
						PacketWork.PACKET_ID_CONFIGURATION_LO_FREQUENCIES);
	}

	public ConfigurationSetter(LinkHeader linkHeader, byte packetParameterHeaderCode, short packetId) {
		super(linkHeader, Packet.IRT_SLCP_PACKET_ID_CONFIGURATION, packetParameterHeaderCode, packetId);
	}

	@Override
	public void preparePacketToSend(Object value) {

		short id = ((IdValue)value).getID();
		PacketThread pt = getPacketThread();
		LinkHeader lh = pt.getLinkHeader();

		switch(id){
		case PacketWork.PACKET_ID_CONFIGURATION__LNB:
			pt.preparePacket(Packet.IRT_SLCP_PARAMETER_FCM_CONFIG_BUC_ENABLE, (byte)((IdValue)value).getValue());
			break;
		case PacketWork.PACKET_ID_CONFIGURATION_LO_BIAS_BOARD:
			pt.preparePacket(Packet.IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_LO_SET, value!=null
																						? (Byte)((IdValue)value).getValue()
																								: null);
			break;
		case PacketWork.PACKET_ID_CONFIGURATION_BAIAS_25W_MUTE:
			pt.preparePacket(lh!=null ? Packet.IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_MUTE : Packet.IRT_SLCP_PARAMETER_FCM_CONFIG_MUTE_CONTROL, (byte)(((boolean)((IdValue)value).getValue()) ? 1 : 0));
			break;
		case PacketWork.PACKET_ID_CONFIGURATION_GAIN:
			Value v = (Value)((IdValue)value).getValue();
			pt.preparePacket(Packet.IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_GAIN, v!=null ? (short)v.getValue() : null);
			break;
		case PacketWork.PACKET_ID_CONFIGURATION_ATTENUATION:
			v = (Value)((IdValue)value).getValue();
			pt.preparePacket(((GetterAbstract)this).getPacketParameterHeaderCode(), v!=null ? (short)v.getValue() : null);
			break;
		case PacketWork.PACKET_ID_CONFIGURATION_FREQUENCY:
			v = (Value)((IdValue)value).getValue();
			pt.preparePacket(((GetterAbstract)this).getPacketParameterHeaderCode(), v!=null ? v.getValue() : null);
			break;
		case PacketWork.PACKET_ID_CONFIGURATION__GAIN_OFFSET:
			v = (Value)((IdValue)value).getValue();
			pt.preparePacket(((GetterAbstract)this).getPacketParameterHeaderCode(), v!=null ? (short)v.getValue() : null);
		}
	}

	@Override
	public boolean set(Packet packet) {
		boolean isSet = false;
		if(packet!=null){
			PacketHeader ph = packet.getHeader();
			short packetId = getPacketId();
			if(ph!=null &&
					ph.getGroupId()==Packet.IRT_SLCP_PACKET_ID_CONFIGURATION &&
							ph.getPacketId()==packetId && packet.getPayloads()!=null){

				long tmp = value;
				Object source = null;

				if(ph.getOption()>0 || ph.getType()!=Packet.IRT_SLCP_PACKET_TYPE_RESPONSE){

					tmp = -ph.getOption();
					if(tmp>=0)
						tmp = -20; //Communication error
					source = new Byte((byte) tmp);

				}else{
					Payload pl = packet.getPayload(getPacketParameterHeaderCode());
					if(pl!=null && pl.getBuffer()!=null) {
						isSet = true;
						int size = pl.getParameterHeader().getSize();
						switch(size){
						case 1:
							source = new Byte((byte) (tmp = pl.getByte()));	//Mute
							tmp = ((Byte)source).hashCode();
							break;
						case 2:
							source = new Integer((int) (tmp = pl.getShort(0))); //
							tmp = ((Integer)source).hashCode();
							break;
						case 4:
						case 16:
							source = new Range(pl);
							tmp = ((Range)source).hashCode();
							break;
						case 8:
							source = new Long(tmp = pl.getLong());
							tmp = ((Long)source).hashCode();
							break;
						case 9:
						case 18:// byte 1 - determinant, next 8 bytes - frequency
						case 27:
						case 36:
						case 45:
							DefaultComboBoxModel<IdValue> dcbm = new DefaultComboBoxModel<>();
							for(byte i=0; i<size; i+=8) {
								byte id = pl.getByte(i++);
								IdValue iv = new IdValueForComboBox(id, Translation.getValue(String.class, "lo", "LO")+":"+id+" - "+new ValueFrequency(pl.getLong(i), 0, Long.MAX_VALUE).toString());
								dcbm.addElement(iv);
							}
							source = dcbm;
							tmp++;//make different from value
							break;
						case 32:
						case 48:
						case 64:
						case 80:
							List<String> list = new ArrayList<>();
							long[] longs = pl.getArrayLong();
							for(long l:longs){
								String value = new ValueFrequency(l, Long.MIN_VALUE, Long.MAX_VALUE).toString();
								if(!list.contains(value))
									list.add(value);
							}
							if(list.size()>0){
								source = list.toArray();
								tmp = list.toString().hashCode();
							}
							break;
						default:
							isSet = false;
						}
					}
				}

				if(tmp!=value && source!=null){
					fireValueChangeListener(new ValueChangeEvent(source, packetId));
					value=tmp;
				}
			}
		}

		return isSet;
	}
}
