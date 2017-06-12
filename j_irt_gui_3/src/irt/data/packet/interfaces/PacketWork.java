package irt.data.packet.interfaces;

import java.util.Arrays;
import java.util.Optional;

import irt.data.listener.ValueChangeListener;
import irt.data.packet.Packet;
import irt.data.packet.PacketAbstract.Priority;

public interface PacketWork extends Comparable<PacketWork>{

	public static final short PACKET_UNNECESSARY = 0;

	public static final short PACKET_ID_DEVICE_INFO = 1;

	public static final short PACKET_ID_MEASUREMENT_ALL 					= 9,
							PACKET_ID_MEASUREMENT_STATUS 					= 10,
							PACKET_ID_MEASUREMENT_INPUT_POWER				= 11,
							PACKET_ID_MEASUREMENT_BAIAS_25W_OUTPUT_POWER 	= 12,
							PACKET_ID_MEASUREMENT_TEMPERATURE 				= 13,
							PACKET_ID_MEASUREMENT_UNIT_TEMPERATURE			= 14,
							PACKET_ID_MEASUREMENT_CPU_TEMPERATURE			= 15,
							PACKET_ID_MEASUREMENT_5V5 						= 16,
							PACKET_ID_MEASUREMENT_13V2 						= 17,
							PACKET_ID_MEASUREMENT_13V2_NEG					= 18,
							PACKET_ID_MEASUREMENT_OUTPUT_POWER 				= 19,
							PACKET_ID_MEASUREMENT_SNB1_STATUS 				= 180,
							PACKET_ID_MEASUREMENT_SNB2_STATUS 				= 181,
							PACKET_ID_MEASUREMENT_WGS_POSITION 				= 182;

	public static final short 	PACKET_ID_CONFIGURATION_LO		= 20,
								PACKET_ID_CONFIGURATION_LO_FREQUENCIES 		= 21,
								PACKET_ID_CONFIGURATION_MUTE 				= 22,
								PACKET_ID_CONFIGURATION_GAIN				= 23,
								PACKET_ID_CONFIGURATION_GAIN_RANGE 			= 24,
								PACKET_ID_CONFIGURATION_ATTENUATION			= 25,
								PACKET_ID_CONFIGURATION_ATTENUATION_RANGE 	= 26,
								PACKET_ID_CONFIGURATION_FREQUENCY 			= 27,
								PACKET_ID_CONFIGURATION_FREQUENCY_RANGE 	= 28,
								PACKET_ID_STORE_CONFIG						= 29,
								PACKET_ID_CONFIGURATION_FCM_LNB_POWER		= 30,
								PACKET_ID_CONFIGURATION_GAIN_OFFSET			= 31,
								PACKET_ID_CONFIGURATION_FCM_FLAGS 			= 32,
								PACKET_ID_CONFIGURATION_REDUNDANCY_ENABLE	= 33,
								PACKET_ID_CONFIGURATION_REDUNDANCY_MODE		= 34,
								PACKET_ID_CONFIGURATION_REDUNDANCY_NAME		= 35,
								PACKET_ID_CONFIGURATION_REDUNDANCY_STATUS	= 36,
								PACKET_ID_CONFIGURATION_REDUNDANCY_SET_ONLINE= 37,
								PACKET_ID_CONFIGURATION_MUTE_OUTDOOR		= 38,
								PACKET_ID_CONFIGURATION_ALC_ENABLE			= 39,
								PACKET_ID_CONFIGURATION_ALC_ENABLE_COMAND	= 170,
								PACKET_ID_CONFIGURATION_ALC_LEVEL			= 171,
								PACKET_ID_CONFIGURATION_ALC_RANGE			= 172,
								PACKET_ID_CONFIGURATION_DLRS_WGS_SWITCHOVER	= 173,
								PACKET_ID_CONFIGURATION_SET_DLRS_WGS_SWITCHOVER = 174,
								PACKET_ID_CONFIGURATION_SPECTRUM_INVERSION 		= 175,
								PACKET_ID_CONFIGURATION_SET_SPECTRUM_INVERSION 	= 176,
								PACKET_ID_CONFIGURATION_REFERENCE_CONTROL		= 177,
								PACKET_ID_CONFIGURATION_FCM_LNB_REFERENCE		= 178;

	public static final short 	PACKET_ID_DEVICE_DEBUG_POTENTIOMETER_N1 		= 40,
								PACKET_ID_DEVICE_DEBUG_POTRNTIOMETER_N1_SET 	= 41,
								PACKET_ID_DEVICE_DEBUG_POTENTIOMETER_N2 		= 42,
								PACKET_ID_DEVICE_DEBUG_POTRNTIOMETER_N2_SET 	= 43,
								PACKET_ID_DEVICE_DEBUG_POTENTIOMETER_N3 		= 44,
								PACKET_ID_DEVICE_DEBUG_POTRNTIOMETER_N3_SET		= 45,
								PACKET_ID_DEVICE_DEBUG_POTENTIOMETER_N4 		= 46,
								PACKET_ID_DEVICE_DEBUG_POTRNTIOMETER_N4_SET		= 47,
								PACKET_ID_DEVICE_DEBUG_POTENTIOMETER_N5 		= 48,
								PACKET_ID_DEVICE_DEBUG_POTRNTIOMETER_N5_SET		= 49,
								PACKET_ID_DEVICE_DEBUG_POTENTIOMETER_N6 		= 50,
								PACKET_ID_DEVICE_DEBUG_POTRNTIOMETER_N6_SET		= 51,
								PACKET_ID_DEVICE_DEBUG_SWITCH_N1 				= 52,
								PACKET_ID_DEVICE_DEBUG_SWITCH_N2 				= 53,
								PACKET_ID_DEVICE_DEBUG_HS1_CURRENT 				= 54,
								PACKET_ID_DEVICE_DEBUG_HS2_CURRENT 				= 55,
								PACKET_ID_DEVICE_DEBUG_OUTPUT_POWER 			= 56,
								PACKET_ID_DEVICE_DEBUG_TEMPERATURE 				= 57,
								PACKET_ID_DEVICE_DEBUG_NGLOBAL 					= 58,
								PACKET_ID_DEVICE_CONVERTER_DAC1 				= 59,
								PACKET_ID_DEVICE_CONVERTER_DAC2 				= 60,
								PACKET_ID_DEVICE_CONVERTER_DAC3 				= 61,
								PACKET_ID_DEVICE_CONVERTER_DAC4 				= 62,
								PACKET_ID_DEVICE_DEBUG_CALIBRATION_MODE 		= 63,
								PACKET_ID_DEVICE_DEVICE_DEBUG_CONVERTER_PLL_1 	= 64,
								PACKET_ID_DEVICE_DEVICE_DEBUG_CONVERTER_PLL_2 	= 65,
								PACKET_ID_DEVICE_DEBUG_DEVICE_INFO				= 66,
								PACKET_ID_DEVICE_POTENTIOMETERS_INIT			= 67;

	public static final short 	PACKET_ID_FCM_DEVICE_DEBUG_PLL_REG 	= 70;

	public static final short 	PACKET_ID_FCM_ADC_INPUT_POWER 	= 80,
								PACKET_ID_FCM_ADC_OUTPUT_POWER	= 81,
								PACKET_ID_FCM_ADC_TEMPERATURE 	= 82,
								PACKET_ID_FCM_ADC_CURRENT 		= 83,
								PACKET_ID_FCM_ADC_5V5 			= 84,
								PACKET_ID_FCM_ADC_13v2 			= 85,
								PACKET_ID_FCM_ADC_13V2_NEG		= 86;

//Dumps to file
	public static final short	PACKET_ID_DUMP_DEVICE_DEBUG_DEVICE_INFO_0	= 90,//Index 0.  Board info, like: CPU frequency, CPU reset type, CPU clock source, Local temperature
								PACKET_ID_DUMP_DEVICE_DEBUG_DEVICE_INFO_1	= 91,//Index 1.  Board error info
								PACKET_ID_DUMP_DEVICE_DEBUG_DEVICE_INFO_2	= 92,//Index 2.  Threshold info, like:  Zero current, Over-current HSS1, Over-current HSS2, Over-temperature mute, Over-temperature unmute
								PACKET_ID_DUMP_DEVICE_DEBUG_DEVICE_INFO_3	= 93,//Index 3.  I2C error statistics
								PACKET_ID_DUMP_DEVICE_DEBUG_DEVICE_INFO_4	= 94,//Index 4.  Mute state machine flags in format: Flag_name: Current_flag (History_flag)
								PACKET_ID_DUMP_DEVICE_DEBUG_DEVICE_INFO_10	= 99,//Index 10.  FCM info
								PACKET_ID_DUMP_REGISTER_100					= 100,
								PACKET_ID_DUMP_REGISTER_1					= 101,
								PACKET_ID_DUMP_REGISTER_2					= 102,
								PACKET_ID_DUMP_REGISTER_3					= 103,
								PACKET_ID_DUMP_REGISTER_4					= 104,
								PACKET_ID_DUMP_REGISTER_5					= 105,
								PACKET_ID_DUMP_REGISTER_6					= 106,
								PACKET_ID_DUMP_REGISTER_7					= 107,
								PACKET_ID_DUMP_REGISTER_10					= 110,
								PACKET_ID_CLEAR_STATISTICS 					= 111,
								PACKET_ID_DUMP_REGISTER_201					= 112,
								PACKET_ID_DUMP_REGISTER_202					= 113,
								PACKET_ID_DUMP_REGISTER_207					= 114,
								PACKET_ID_DUMP_REGISTER_220					= 115,
								PACKET_ID_DUMP_POWER						= 116;

	public static final short PACKET_ID_PRODUCTION_GENERIC_SET_1_INITIALIZE = 120;

	public static final short PACKET_ID_NETWORK_ADDRESS = 130;


	public static final short 	PACKET_ID_ALARMS_IDs = 140,
								PACKET_ID_ALARMS_SUMMARY			= 141,
								PACKET_ID_ALARMS_OWER_CURRENT		= 142,
								PACKET_ID_ALARMS_UNDER_CURRENT		= 143,
								PACKET_ID_ALARMS_OWER_TEMPERATURE	= 144,
								PACKET_ID_ALARMS_PLL_OUT_OF_LOCK	= 145,
								PACKET_ID_ALARMS_HARDWARE_FAULT		= 146,
								PACKET_ID_ALARMS_REDUNDANT_FAULT	= 147,
								PACKET_ID_ALARMS_test				= 148,
								PACKET_ID_NO_INPUT_SIGNAL 			= 149,
								PACKET_ID_RF_OVERDRIVEL 			= 160;

	public enum AlarmsPacketIds{

		INDEFINED				((byte) 0, PACKET_UNNECESSARY),
		PLL_OUT_OF_LOCK			((byte) 1, PACKET_ID_ALARMS_PLL_OUT_OF_LOCK),
		OVER_CURRENT_ALARM		((byte) 4, PACKET_ID_ALARMS_OWER_CURRENT),
		UNDER_CURRENT_ALARM		((byte) 5, PACKET_ID_ALARMS_UNDER_CURRENT),
		OVER_TEMPERATURE_ALARM	((byte) 7, PACKET_ID_ALARMS_OWER_TEMPERATURE),
		HW_FAULT				((byte)10, PACKET_ID_ALARMS_HARDWARE_FAULT),
		REDUNDANCY_FAULT		((byte)11, PACKET_ID_ALARMS_REDUNDANT_FAULT),
		RF_OVERDRIVE			((byte)12, PACKET_ID_RF_OVERDRIVEL);

		private short alarmId;
		private short packetId;

		public short getPacketId() {
			return packetId;
		}

		private AlarmsPacketIds(short alarmId, short packetId){
			this.alarmId = alarmId;
			this.packetId = packetId;
		}

		public static Optional<AlarmsPacketIds> valueOf(short alarmId){
			return Arrays.stream(values()).parallel().filter(a->a.alarmId==alarmId).findAny();
		}
	}

	public static final short	PACKET_ID_PROTOCOL_ADDRESS	= 150;

	/*		reserved for configuration from 170 to 179		*/
	/*		reserved for measurement from 180 to 189		*/

	public Priority 			getPriority();
	public PacketThreadWorker 	getPacketThread();
	public void 				addVlueChangeListener(ValueChangeListener valueChangeListener);
	public void 				removeVlueChangeListener(ValueChangeListener valuechangelistener);
	public boolean 				set(Packet packet);
	public void 				clear();
	public void 				removeVlueChangeListeners();
	public boolean 				isAddressEquals(Packet packet);
}
