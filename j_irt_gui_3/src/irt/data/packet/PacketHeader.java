package irt.data.packet;

import irt.controller.DumpControllers;
import irt.data.PacketWork;

import java.util.Arrays;

public class PacketHeader{

//	private final Logger logger = (Logger) LogManager.getLogger();

	public static final int SIZE = 7;
	byte[] packetHeader;

	public PacketHeader(byte[] hrader) {
		if(hrader!=null && hrader.length>=SIZE)
			packetHeader = Arrays.copyOf(hrader, SIZE);
	}

	public PacketHeader() {
	}

/*	private byte	type;		0
 * 	private short 	packetId;	1,2
	private byte 	groupId;	3
	private short 	reserved;	4,5
	private byte 	code; 		6
*/
	public byte[]	asBytes()		{ return packetHeader;		}
	public byte		getPacketType	()		{ return packetHeader[0];	}
	public short	getPacketId()	{ return (short) Packet.shiftAndAdd(Arrays.copyOfRange(packetHeader, 1, 3));	}
	public byte		getParameter()	{ return packetHeader[3];	}
	public short	getReserved()	{ return (short) Packet.shiftAndAdd(Arrays.copyOfRange(packetHeader, 4, 6));	}
	public byte		getOption()		{ return packetHeader[6];	}

	public byte[] set(byte[]data){
		if(data!=null && data.length>=SIZE)
			packetHeader = Arrays.copyOf(data, SIZE);

		return data!=null && data.length>SIZE ? Arrays.copyOfRange(data, SIZE, data.length) : null;
	}

	public void setType		(byte type) 				{ packetHeader[0] = type;}
	public void setPacketId	(short irtSlcpPacketId)		{ System.arraycopy(Packet.toBytes(irtSlcpPacketId), 0, packetHeader, 1, 2);	}
	public void setGroupId	(byte irtSlcpPacketGroupId) { packetHeader[3] = irtSlcpPacketGroupId;}
	public void setOption	(byte option)			 	{ packetHeader[6] = option;	}

	@Override
	public String toString() {
		return "PacketHeader [type="+getTypeStr()+",packetId=" +getPacketIdStr()+",groupId=" +getGroupIdStr()+",reserved=" +getReserved()+",option=" +getOptionStr()+"]";
	}

	public String getPacketIdStr() {
		String packetIdStr = null;
		if (packetHeader != null) {
			short packetId = getPacketId();
			switch (packetId) {
			case PacketWork.PACKET_ID_CONFIGURATION_BAIAS_25W_MUTE:
				packetIdStr = "Mute("+packetId+")";
				break;
			case PacketWork.PACKET_ID_DEVICE_DEBAG_POTENTIOMETER_N1:
			case PacketWork.PACKET_ID_DEVICE_DEBAG_POTRNTIOMETER_N1_SET:
				packetIdStr = "Potentiometer_N1("+packetId+")";
				break;
			case PacketWork.PACKET_ID_DEVICE_DEBAG_POTENTIOMETER_N2:
			case PacketWork.PACKET_ID_DEVICE_DEBAG_POTRNTIOMETER_N2_SET:
				packetIdStr = "Potentiometer_N2("+packetId+")";
				break;
			case PacketWork.PACKET_ID_DEVICE_DEBAG_POTENTIOMETER_N3:
			case PacketWork.PACKET_ID_DEVICE_DEBAG_POTRNTIOMETER_N3_SET:
				packetIdStr = "Potentiometer_ N3("+packetId+")";
				break;
			case PacketWork.PACKET_ID_DEVICE_DEBAG_POTENTIOMETER_N4:
			case PacketWork.PACKET_ID_DEVICE_DEBAG_POTRNTIOMETER_N4_SET:
				packetIdStr = "Potentiometer_N4("+packetId+")";
				break;
			case PacketWork.PACKET_ID_DEVICE_DEBAG_POTENTIOMETER_N5:
			case PacketWork.PACKET_ID_DEVICE_DEBAG_POTRNTIOMETER_N5_SET:
				packetIdStr = "Potentiometer_N5("+packetId+")";
				break;
			case PacketWork.PACKET_ID_DEVICE_DEBAG_POTENTIOMETER_N6:
			case PacketWork.PACKET_ID_DEVICE_DEBAG_POTRNTIOMETER_N6_SET:
				packetIdStr = "Potentiometer_N6("+packetId+")";
				break;
			case PacketWork.PACKET_ID_DEVICE_DEBAG_SWITCH_N1:
				packetIdStr = "Switch_N1("+packetId+")";
				break;
			case PacketWork.PACKET_ID_DEVICE_DEBAG_SWITCH_N2:
				packetIdStr = "Switch_N2("+packetId+")";
				break;
			case PacketWork.PACKET_ID_DEVICE_DEBAG_HS1_CURRENT:
				packetIdStr = "HS1_Current("+packetId+")";
				break;
			case PacketWork.PACKET_ID_DEVICE_DEBAG_HS2_CURRENT:
				packetIdStr = "HS2_Current("+packetId+")";
				break;
			case PacketWork.PACKET_ID_MEASUREMENT_OUTPUT_POWER:
			case PacketWork.PACKET_ID_MEASUREMENT_BAIAS_25W_OUTPUT_POWER:
			case PacketWork.PACKET_ID_DEVICE_DEBAG_OUTPUT_POWER:
				packetIdStr = "OutputPower("+packetId+")";
				break;
			case PacketWork.PACKET_ID_DEVICE_DEBAG_TEMPERATURE:
				packetIdStr = "Temperature("+packetId+")";
				break;
			case PacketWork.PACKET_ID_DEVICE_DEBAG_NGLOBAL:
				packetIdStr = "nGlobal("+packetId+")";
				break;
			case PacketWork.PACKET_ID_MEASUREMENT_INPUT_POWER:
				packetIdStr = "InputPower("+packetId+")";
				break;
			case PacketWork.PACKET_ID_MEASUREMENT_STATUS:
				packetIdStr = "Status("+packetId+")";
				break;
			case PacketWork.PACKET_ID_MEASUREMENT_CPU_TEMPERATURE:
				packetIdStr = "CPU Temperature("+packetId+")";
				break;
			case PacketWork.PACKET_ID_MEASUREMENT_UNIT_TEMPERATURE:
				packetIdStr = "Unit Temperature("+packetId+")";
				break;
			case PacketWork.PACKET_ID_ALARMS_IDs:
				packetIdStr = "Alarms("+packetId+")";
				break;
			case PacketWork.PACKET_ID_ALARMS_OWER_CURRENT:
				packetIdStr = "Alarm - Over Current("+packetId+")";
				break;
			case PacketWork.PACKET_ID_ALARMS_OWER_TEMPERATURE:
				packetIdStr = "Alarm - Over Temperature("+packetId+")";
				break;
			case PacketWork.PACKET_ID_ALARMS_PLL_OUT_OF_LOCK:
				packetIdStr = "Alarm - PLL out of Lock("+packetId+")";
				break;
			case PacketWork.PACKET_ID_ALARMS_UNDER_CURRENT:
				packetIdStr = "Alarm - Under Current("+packetId+")";
				break;
			case PacketWork.PACKET_ID_ALARMS_HARDWARE_FAULT:
				packetIdStr = "Alarm - Other("+packetId+")";
				break;
			case PacketWork.PACKET_ID_ALARMS_SUMMARY:
				packetIdStr = "Alarm - Summary("+packetId+")";
				break;
			case PacketWork.PACKET_ID_ALARMS_REDUNDANT_FAULT:
				packetIdStr = "Redundancy Fault ("+packetId+")";
				break;
			case PacketWork.PACKET_ID_MEASUREMENT_BIAS_25W_TEMPERATURE:
				packetIdStr = "Bias Temperature("+packetId+")";
				break;
			case PacketWork.PACKET_DEVICE_INFO:
				packetIdStr = "Device Info("+packetId+")";
				break;
			case PacketWork.PACKET_ID_CONFIGURATION_GAIN:
				packetIdStr = "Gain("+packetId+")";
				break;
			case PacketWork.PACKET_ID_CONFIGURATION_GAIN_RANGE:
				packetIdStr = "GainRange("+packetId+")";
				break;
			case PacketWork.PACKET_ID_CONFIGURATION_ATTENUATION:
				packetIdStr = "Attenuation("+packetId+")";
				break;
			case PacketWork.PACKET_ID_CONFIGURATION_ATTENUATION_RANGE:
				packetIdStr = "AttenuationRange("+packetId+")";
				break;
			case PacketWork.PACKET_ID_CONFIGURATION_FCM_FLAGS:
				packetIdStr = "FCM FLAGS("+packetId+")";
				break;
			case PacketWork.PACKET_ID_DEVICE_CONVERTER_DAC1:
				packetIdStr = "DAC 1("+packetId+")";
				break;
			case PacketWork.PACKET_ID_DEVICE_DEBAG_CONVERTER_DAC2:
				packetIdStr = "DAC 2("+packetId+")";
				break;
			case PacketWork.PACKET_ID_DEVICE_DEBAG_CONVERTER_DAC3:
				packetIdStr = "DAC 3("+packetId+")";
				break;
			case PacketWork.PACKET_ID_DEVICE_DEBAG_CONVERTER_DAC4:
				packetIdStr = "DAC 4("+packetId+")";
				break;
			case PacketWork.PACKET_ID_MEASUREMENT_13V2:
				packetIdStr = "13.2V("+packetId+")";
				break;
			case PacketWork.PACKET_ID_MEASUREMENT_13V2_NEG:
				packetIdStr = "-13.2V("+packetId+")";
				break;
			case PacketWork.PACKET_ID_MEASUREMENT_5V5:
				packetIdStr = "5.5V("+packetId+")";
				break;
			case PacketWork.PACKET_ID_DEVICE_DEBAG_CALIBRATION_MODE:
				packetIdStr = "CalibrationMode("+packetId+")";
				break;
			case PacketWork.PACKET_ID_DEVICE_DEVICE_DEBAG_CONVERTER_PLL_1:
				packetIdStr = "PLL 1("+packetId+")";
				break;
			case PacketWork.PACKET_ID_DEVICE_DEVICE_DEBAG_CONVERTER_PLL_2:
				packetIdStr = "Pll 2("+packetId+")";
				break;
			case PacketWork.PACKET_ID_CONFIGURATION_FREQUENCY:
				packetIdStr = "Frequency("+packetId+")";
				break;
			case PacketWork.PACKET_ID_CONFIGURATION_FREQUENCY_RANGE:
				packetIdStr = "FrequencyRange("+packetId+")";
				break;
			case PacketWork.PACKET_ID_CONFIGURATION_LO_BIAS_BOARD:
				packetIdStr = "LO Bais Board("+packetId+")";
				break;
			case PacketWork.PACKET_ID_CONFIGURATION_LO_FREQUENCIES:
				packetIdStr = "LO Frequencies("+packetId+")";
				break;
			case PacketWork.PACKET_ID_CONFIGURATION_GAIN_OFFSET:
				packetIdStr = "Gain Offset("+packetId+")";
				break;
			case PacketWork.PACKET_ID_CONFIGURATION_LNB:
				packetIdStr = "LNB On/Off("+packetId+")";
				break;
			case PacketWork.PACKET_ID_STORE_CONFIG:
				packetIdStr = "Store Config("+packetId+")";
				break;
			case PacketWork.PACKET_ID_FCM_ADC_13v2:
				packetIdStr = "ADC 13.2V("+packetId+")";
				break;
			case PacketWork.PACKET_ID_FCM_ADC_13V2_NEG:
				packetIdStr = "ADC -13.2V("+packetId+")";
				break;
			case PacketWork.PACKET_ID_FCM_ADC_5V5:
				packetIdStr = "ADC 5.5V("+packetId+")";
				break;
			case PacketWork.PACKET_ID_FCM_ADC_CURRENT:
				packetIdStr = "ADC Current("+packetId+")";
				break;
			case PacketWork.PACKET_ID_FCM_ADC_INPUT_POWER:
				packetIdStr = "ADC Input Power("+packetId+")";
				break;
			case PacketWork.PACKET_ID_FCM_ADC_OUTPUT_POWER:
				packetIdStr = "ADC Output Power("+packetId+")";
				break;
			case PacketWork.PACKET_ID_FCM_ADC_TEMPERATURE:
				packetIdStr = "ADC Temperature("+packetId+")";
				break;
			case PacketWork.PACKET_ID_FCM_DEVICE_DEBAG_PLL_REG:
				packetIdStr = "PLL reg.N9("+packetId+")";
				break;
			case PacketWork.PACKET_ID_DEVICE_DEBAG_DEVICE_INFO:
				packetIdStr = "Device Debug Info("+packetId+")";
				break;
			case PacketWork.PACKET_ID_PRODUCTION_GENERIC_SET_1_INITIALIZE:
				packetIdStr = "Initialize("+packetId+")";
				break;
			case PacketWork.PACKET_NETWORK_ADDRESS:
				packetIdStr = "Network Address("+packetId+")";
				break;
			case PacketWork.PACKET_ID_DUMP_DEVICE_DEBAG_DEVICE_INFO_0:
			case PacketWork.PACKET_ID_DUMP_DEVICE_DEBAG_DEVICE_INFO_1:
			case PacketWork.PACKET_ID_DUMP_DEVICE_DEBAG_DEVICE_INFO_2:
			case PacketWork.PACKET_ID_DUMP_DEVICE_DEBAG_DEVICE_INFO_3:
			case PacketWork.PACKET_ID_DUMP_DEVICE_DEBAG_DEVICE_INFO_4:
			case PacketWork.PACKET_ID_DUMP_DEVICE_DEBAG_DEVICE_INFO_10:
			case PacketWork.PACKET_ID_DUMP_REGISTER_1:
			case PacketWork.PACKET_ID_DUMP_REGISTER_2:
			case PacketWork.PACKET_ID_DUMP_REGISTER_3:
			case PacketWork.PACKET_ID_DUMP_REGISTER_4:
			case PacketWork.PACKET_ID_DUMP_REGISTER_5:
			case PacketWork.PACKET_ID_DUMP_REGISTER_6:
			case PacketWork.PACKET_ID_DUMP_REGISTER_7:
			case PacketWork.PACKET_ID_DUMP_REGISTER_100:
				packetIdStr = "Dump "+DumpControllers.parseId(packetId);
				break;
			case PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_ENABLE:
				packetIdStr = "Redundancy Enable ("+packetId+")";
				break;
			case PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_MODE:
				packetIdStr = "Redundancy Mode ("+packetId+")";
				break;
			case PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_NAME:
				packetIdStr = "Redundancy Name ("+packetId+")";
				break;
			case PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_STAT:
				packetIdStr = "Redundancy Status ("+packetId+")";
				break;
			case PacketWork.PACKET_ID_PROTOCOL_ADDRESS:
				packetIdStr = "Unit Address ("+packetId+")";
				break;
			default:
				packetIdStr = ""+packetId;
			}
		}
		return packetIdStr;
	}

	public String getOptionStr() {
		return packetHeader!=null ? getOptionStr(getOption()) : null;

	}

	public static String getOptionStr(byte code) {

		if(code<0)
			code = (byte) -code;

		String codeStr = null;
		switch (code) {
		case Packet.ERROR_NO_ERROR:
			codeStr = "No error(" + code + ")";
			break;
		case 1:
			codeStr = "System internal(" + code + ")";
			break;
		case 2:
			codeStr = "Write error(" + code + ")";
			break;
		case 3:
			codeStr = "Function not implemented(" + code + ")";
			break;
		case 4:
			codeStr = "Value outside of valid range(" + code + ")";
			break;
		case 5:
			codeStr = "Requested information can’t be generated(" + code + ")";
			break;
		case 6:
			codeStr = "Command can’t be executed(" + code + ")";
			break;
		case 7:
			codeStr = "Invalid data format(" + code + ")";
			break;
		case 8:
			codeStr = "Invalid value(" + code + ")";
			break;
		case 9:
			codeStr = "Not enough memory (" + code + ")";
			break;
		case Packet.ERROR_REQUESTED_ELEMENT_NOT_FOUND:
			codeStr = "Requested element not found(" + code + ")";
			break;
		case 11:
			codeStr = "Timed out(" + code + ")";
			break;
		case 20:
			codeStr = "Communication problem(" + code + ")";
			break;
		default:
			codeStr = "" + code;
		}
		return codeStr;
	}

	public String getTypeStr() {
		String typeStr = null;
		if(packetHeader!=null)
		switch(getPacketType()){
		case Packet.IRT_SLCP_PACKET_TYPE_SPONTANEOUS:
			typeStr = "Spontaneous("+ Packet.IRT_SLCP_PACKET_TYPE_SPONTANEOUS+")";
			break;
		case Packet.IRT_SLCP_PACKET_TYPE_RESPONSE:
			typeStr = "Response("+ Packet.IRT_SLCP_PACKET_TYPE_RESPONSE+")";
			break;
		case Packet.IRT_SLCP_PACKET_TYPE_REQUEST:
			typeStr = "Request("+ Packet.IRT_SLCP_PACKET_TYPE_REQUEST+")";
			break;
		case Packet.IRT_SLCP_PACKET_TYPE_COMMAND:
			typeStr = "Command("+ Packet.IRT_SLCP_PACKET_TYPE_COMMAND+")";
			break;
		case Packet.IRT_SLCP_PACKET_TYPE_ACK:
			typeStr = "Acknowledgement("+ Packet.IRT_SLCP_PACKET_TYPE_ACK+")";
			break;
		default:
			typeStr = ""+(getPacketType()&0xFF);
		}
		return typeStr;
	}

	private String getGroupIdStr() {
		String typeStr = null;
		if(packetHeader!=null)
		switch(getParameter()){
		case Packet.IRT_SLCP_PACKET_ID_ALARM:
			typeStr = "Alarm("+ Packet.IRT_SLCP_PACKET_ID_ALARM+")";
			break;
		case Packet.IRT_SLCP_PACKET_ID_CONFIGURATION:
			typeStr = "Configuration("+ Packet.IRT_SLCP_PACKET_ID_CONFIGURATION+")";
			break;
		case Packet.IRT_SLCP_PACKET_ID_FILETRANSFER:
			typeStr = "FileTranster("+ Packet.IRT_SLCP_PACKET_ID_FILETRANSFER+")";
			break;
		case Packet.IRT_SLCP_PACKET_ID_MEASUREMENT:
			typeStr = "Measurement("+ Packet.IRT_SLCP_PACKET_ID_MEASUREMENT+")";
			break;
		case Packet.IRT_SLCP_PACKET_ID_RESET:
			typeStr = "Reset("+ Packet.IRT_SLCP_PACKET_ID_RESET+")";
			break;
		case Packet.IRT_SLCP_PACKET_ID_DEVICE_INFO:
			typeStr = "DeviceInfo("+ Packet.IRT_SLCP_PACKET_ID_DEVICE_INFO+")";
			break;
		case Packet.IRT_SLCP_PACKET_ID_CONFIG_PROFILE:
			typeStr = "SaveConfigProfile("+ Packet.IRT_SLCP_PACKET_ID_CONFIG_PROFILE+")";
			break;
		case Packet.IRT_SLCP_PACKET_ID_PROTOCOL:
			typeStr = "Protocol("+ Packet.IRT_SLCP_PACKET_ID_PROTOCOL+")";
			break;
		case Packet.IRT_SLCP_PACKET_ID_DEVELOPER_GENERIC_SET_1:
			typeStr = "DeveloperGeneric("+ Packet.IRT_SLCP_PACKET_ID_DEVELOPER_GENERIC_SET_1+")";
			break;
		case Packet.IRT_SLCP_PACKET_ID_DEVICE_DEBAG:
			typeStr = "Device Debug("+ Packet.IRT_SLCP_PACKET_ID_DEVICE_DEBAG+")";
			break;
		default:
			typeStr = ""+(getPacketType()&0xFF);
		}
		return typeStr;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(packetHeader);
	}

	@Override
	public boolean equals(Object obj) {
		return obj!=null ? obj.hashCode()==hashCode() : false;
	}
}
