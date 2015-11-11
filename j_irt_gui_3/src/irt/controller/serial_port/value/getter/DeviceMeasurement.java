package irt.controller.serial_port.value.getter;

import java.util.List;

import irt.data.PacketThread;
import irt.data.PacketWork;
import irt.data.event.ValueChangeEvent;
import irt.data.packet.Packet;
import irt.data.packet.PacketImp;
import irt.data.packet.Payload;

public class DeviceMeasurement extends GetterAbstract {

	private short unitTemperature;
	private short spuTemperature;
	private byte statusBits;
	private short inputPower;
	private short outputPower;
	private short pos5V5;
	private short pos13V2;
	private short neg13V2;
	private short current;

	public DeviceMeasurement(PacketThread packetThread) {
		super(null, (byte)0, (byte)0, PacketWork.PACKET_UNNECESSARY);
	}

	@Override
	public boolean set(Packet packet) {
		boolean isSet = false;
		if(isAddressEquals(packet) && packet.getHeader()!=null &&
				packet.getHeader().getPacketType()==PacketImp.PACKET_TYPE_RESPONSE &&
					packet.getHeader().getGroupId()==PacketImp.GROUP_ID_MEASUREMENT &&
						packet.getHeader().getPacketId()==getPacketId()){
			List<Payload> payloads = packet.getPayloads();
			if(payloads!=null)
			for(Payload pl:payloads) {
				short sh;
				switch(pl.getParameterHeader().getCode()){
				case Payload.DEVICE_MESUREMENT_SUMMARY_ALARM_BITS:
					logger.warn("not used: DEVICE_MESUREMENT_SUMMARY_ALARM_BITS");
					break;
				case Payload.DEVICE_MESUREMENT_STATUS_BITS		:
					byte b;
					if((b=pl.getByte())!=statusBits){
						statusBits = b;
						fireValueChangeListener(new ValueChangeEvent("Status Bits", b));
					}
					break;
				case Payload.DEVICE_MESUREMENT_UNIT_TEMPERATURE	:
					if((sh = (short) pl.getLong())!=unitTemperature){
						unitTemperature = sh;
						fireValueChangeListener(new ValueChangeEvent("Unit Temperature", sh));
					}
					break;
				case Payload.DEVICE_MESUREMENT_INPUT_POVER		:
					if((sh = (short) pl.getLong())!=inputPower){
						inputPower = sh;
						fireValueChangeListener(new ValueChangeEvent("Output Power", sh));
					}
					break;
				case Payload.DEVICE_MESUREMENT_OUTPUT_POVER		:
					if((sh = (short) pl.getLong())!=outputPower){
						outputPower = sh;
						fireValueChangeListener(new ValueChangeEvent("Input Power", sh));
					}
					break;
				case Payload.DEVICE_MESUREMENT_MONITOR_5V5		:
					if((sh = (short) pl.getLong())!=pos5V5){
						pos5V5 = sh;
						fireValueChangeListener(new ValueChangeEvent("5V5", sh));
					}
					break;
				case Payload.DEVICE_MESUREMENT_MONITOR_13V2_POS	:
					if((sh = (short) pl.getLong())!=pos13V2){
						pos13V2 = sh;
						fireValueChangeListener(new ValueChangeEvent("13V2", sh));
					}
					break;
				case Payload.DEVICE_MESUREMENT_MONITOR_13V2_NEG	:
					if((sh = (short) pl.getLong())!=neg13V2){
						neg13V2 = sh;
						fireValueChangeListener(new ValueChangeEvent("-13V2", sh));
					}
					break;
				case Payload.DEVICE_MESUREMENT_MONITOR__CURRENT	:
					if((sh = (short) pl.getLong())!=current){
						current = sh;
						fireValueChangeListener(new ValueChangeEvent("Current", sh));
					}
					break;
				case Payload.DEVICE_MESUREMENT_SPU_TEMPERATURE	:
					if((sh = (short) pl.getLong())!=spuTemperature){
						spuTemperature = sh;
						fireValueChangeListener(new ValueChangeEvent("CPU Temperature", sh));
					}
				}
			}
			isSet = true;
		}
		return isSet;
	}
//
//	@Override
//	public byte getPacketID() {
//		return Packet.IRT_SLCP_PACKET_ID_MEASUREMENT;
//	}
//
//	@Override
//	public byte getPayloadParameterHeaderCode() {
//		return Packet.IRT_SLCP_PARAMETER_ALL;
//	}
}
