package irt.controller.serial_port.value.setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.swing.DefaultComboBoxModel;

import irt.controller.serial_port.value.getter.GetterAbstract;
import irt.controller.translation.Translation;
import irt.data.IdValue;
import irt.data.IdValueForComboBox;
import irt.data.LinkedPacketThread;
import irt.data.PacketThread;
import irt.data.Range;
import irt.data.event.ValueChangeEvent;
import irt.data.packet.LinkHeader;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketIDs;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketGroupIDs;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.Packet;
import irt.data.value.Value;
import irt.data.value.ValueFrequency;

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
	 * @param packetParameterHeaderCode by default set to Packet.IRT_SLCP_PARAMETER_25W_BIAS_CONFIGURATION_LO_FREQUENCIES
	 */
	public ConfigurationSetter(LinkHeader linkHeader) {
		this(linkHeader,
				linkHeader!=null && linkHeader.getAddr()!=0 ? PacketImp.PARAMETER_ID_CONFIGURATION_LO_FREQUENCIES :
					PacketImp.PARAMETER_CONFIG_FCM_FREQUENCY_RANGE,
					PacketIDs.CONFIGURATION_LO_FREQUENCIES);
	}

	public ConfigurationSetter(LinkHeader linkHeader, byte packetParameterHeaderCode, PacketIDs packetID) {
		super(linkHeader, PacketGroupIDs.CONFIGURATION.getId(), packetParameterHeaderCode, packetID.getId());
	}

	@Override
	public void preparePacketToSend(Object value) {

		if (value instanceof IdValue) {
			int id = ((IdValue) value).getID()&0xFF;
			PacketThread pt = packetThread;
			LinkHeader lh = pt.getLinkHeader();

			final PacketIDs[] values = PacketIDs.values();

			if(id<values.length)
			switch (values[id]) {
			case CONFIGURATION_FCM_LNB_POWER:
//			case PacketImp.PARAMETER_CONFIG_LNB_POWER:
				logger.trace(pt.getClass().getSimpleName());
				pt.preparePacket(PacketImp.PARAMETER_CONFIG_LNB_POWER, (byte) ((IdValue) value).getValue());
				break;
			case CONFIGURATION_LO:
				pt.preparePacket(PacketImp.PARAMETER_ID_CONFIGURATION_LO_SET, value != null ? (Byte) ((IdValue) value).getValue() : null);
				break;
			case CONFIGURATION_MUTE_OUTDOOR:
			case CONFIGURATION_MUTE:
				pt.preparePacket(lh != null && PacketIDs.CONFIGURATION_MUTE_OUTDOOR.match((short) id) ? PacketImp.PARAMETER_ID_CONFIGURATION_MUTE : PacketImp.PARAMETER_CONFIG_FCM_MUTE_CONTROL,
						(byte) (((boolean) ((IdValue) value).getValue()) ? 1 : 0));
				break;
			case CONFIGURATION_GAIN:
				Value v = (Value) ((IdValue) value).getValue();
				pt.preparePacket(PacketImp.PARAMETER_ID_CONFIGURATION_GAIN, v != null ? (short) v.getValue() : null);
				break;
			case CONFIGURATION_ALC_LEVEL:
			case CONFIGURATION_ATTENUATION:
				v = (Value) ((IdValue) value).getValue();
				pt.preparePacket(((GetterAbstract) this).getPacketParameterHeaderCode(), v != null ? (short) v.getValue() : null);
				break;
			case CONFIGURATION_FREQUENCY:
				v = (Value) ((IdValue) value).getValue();
				pt.preparePacket(((GetterAbstract) this).getPacketParameterHeaderCode(), v != null ? v.getValue() : null);
				break;
			case CONFIGURATION_GAIN_OFFSET:
				v = (Value) ((IdValue) value).getValue();
				pt.preparePacket(((GetterAbstract) this).getPacketParameterHeaderCode(), v != null ? (short) v.getValue() : null);
			default:
			}
		}else if(value instanceof Integer){
			byte[] data = packetThread.getData();
			int length = data.length;
			data[length-1] = 4;
			data = Arrays.copyOf(data, length+4);
			int v = (int)value;
			if(v!=0){
				byte[] bytes = PacketImp.toBytes(v);
				System.arraycopy(bytes, 0, data, length, bytes.length);
			}

			if(packetThread instanceof LinkedPacketThread)
				data[4] = PacketImp.PACKET_TYPE_COMMAND;
			else
				data[0] = PacketImp.PACKET_TYPE_COMMAND;

			logger.trace("data={}", data);

			packetThread.setData(data);
		}else if(value instanceof Byte){
			byte[] data = packetThread.getData();
			int length = data.length;
			data[length-1] = 1;
			data = Arrays.copyOf(data, length+1);
			data[length] = (byte)value;

			if(packetThread instanceof LinkedPacketThread)
				data[4] = PacketImp.PACKET_TYPE_COMMAND;
			else
				data[0] = PacketImp.PACKET_TYPE_COMMAND;

			packetThread.setData(data);

			logger.trace("\n\tpacketThread={}", packetThread);
		}else
			logger.warn("Not Posible to prepare Packet. value is {}", value.getClass());
	}

	@Override
	public boolean set(Packet packet) {
		boolean isSet = false;
		if(isAddressEquals(packet)){
			PacketHeader ph = packet.getHeader();
			final int intId = getPacketId()&0xFF;
			final PacketIDs[] values = PacketIDs.values();
			PacketIDs packetId = Optional.of(intId).filter(i->i<values.length).map(i->values[i]).orElse(PacketIDs.UNNECESSARY);
			if(ph!=null &&
					PacketGroupIDs.CONFIGURATION.match(ph.getGroupId()) &&
							packetId.match(ph.getPacketId()) && packet.getPayloads()!=null){

				long tmp = value;
				Object source = null;

				if(ph.getError()>0 || ph.getPacketType()!=PacketImp.PACKET_TYPE_RESPONSE){

					tmp = -ph.getError();
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
