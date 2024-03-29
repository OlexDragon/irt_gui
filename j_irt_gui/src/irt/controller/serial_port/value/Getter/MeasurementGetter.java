package irt.controller.serial_port.value.Getter;

import irt.data.PacketWork;
import irt.data.event.ValueChangeEvent;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.packet.PacketHeader;
import irt.data.packet.ParameterHeader;
import irt.data.packet.Payload;

public class MeasurementGetter extends GetterAbstract {

	private Object value = Integer.MIN_VALUE;
	private byte packetPayloadParameterHeaderCode;

	/**
	 * use to get converter status bits
	 */
	public MeasurementGetter() {
		this(null, Packet.IRT_SLCP_PARAMETER_FCM_MEASUREMENT_STATUS, PacketWork.PACKET_ID_MEASUREMENT_STATUS);
	}
	/**
	 * use to get bias board status bits
	 */
	public MeasurementGetter(LinkHeader linkHeader) {
		this(linkHeader, Packet.IRT_SLCP_PARAMETER_25W_BAIS_MEASUREMENT_STATUS, PacketWork.PACKET_ID_MEASUREMENT_STATUS);
	}

	public MeasurementGetter(byte packetPayloadParameterHeaderCode, short pacetId) {
		this(null, packetPayloadParameterHeaderCode, pacetId);
	}

	public MeasurementGetter(LinkHeader linkHeader, byte packetPayloadParameterHeaderCode, short pacetId) {
		super(linkHeader, Packet.IRT_SLCP_PACKET_ID_MEASUREMENT, packetPayloadParameterHeaderCode, pacetId);
		this.packetPayloadParameterHeaderCode = packetPayloadParameterHeaderCode;
	}

	@Override
	public boolean set(Packet packet) {
		boolean isSet = false;			
		if(packet!=null){
			PacketHeader ph = packet.getHeader();
			if(ph!=null){
				short packetId = ph.getPacketId();
				if(ph.getGroupId()==Packet.IRT_SLCP_PACKET_ID_MEASUREMENT && packet.getPayloads()!=null && packetId==getPacketId()){
					Object source = null;
					byte option = ph.getOption();
					if(option>0 || ph.getType()!=Packet.IRT_SLCP_PACKET_TYPE_RESPONSE){
						source = new Byte((byte) (option>0 ? -option : -20));//-20 no answer
					}else{
						Payload pl = packet.getPayload(packetPayloadParameterHeaderCode);
						if(pl!=null){
							ParameterHeader parH = pl.getParameterHeader();
							if(parH.getSize()==4)
								source = new Long(pl.getInt(0));
							else
								source = new Long(pl.getShort(0));
						}
					}
					if(source!=null && !source.equals(value)){
						value = source;
						fireValueChangeListener(new ValueChangeEvent(source, packetId));
					}

					isSet = true;
				}
			}
		}
		return isSet;
	}
}
