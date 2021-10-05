package irt.data.packet;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.listener.ValueChangeListener;
import irt.data.packet.PacketSuper.Priority;
import irt.data.packet.denice_debag.DeviceDebugPacket.DeviceDebugType;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.interfaces.PacketThreadWorker;

public interface PacketWork extends Comparable<PacketWork>{

	public enum AlarmsPacketIds{

		STATUS					((byte) 0, PacketIDs.ALARMS_STATUS					, false),
		PLL_OUT_OF_LOCK			((byte) 1, PacketIDs.ALARMS_PLL_OUT_OF_LOCK			, false),
		TEMPERATURE_ThRESHOLD_1	((byte) 2, PacketIDs.ALARMS_TEMPERATURE_ThRESHOLD_1	, false),
		TEMPERATURE_ThRESHOLD_2	((byte) 3, PacketIDs.ALARMS_TEMPERATURE_ThRESHOLD_2	, false),
		OVER_CURRENT_ALARM		((byte) 4, PacketIDs.ALARMS_OWER_CURRENT			, false),
		UNDER_CURRENT_ALARM		((byte) 5, PacketIDs.ALARMS_UNDER_CURRENT			, false),
		ALC_ERROR				((byte) 6, PacketIDs.ALARMS_ALC_ERROR				, false),
		OVER_TEMPERATURE_ALARM	((byte) 7, PacketIDs.ALARMS_OWER_TEMPERATURE		, false),
		TODO4					((byte) 8, PacketIDs.ALARMS_TODO4					, false),
		TODO5					((byte) 9, PacketIDs.ALARMS_TODO5					, false),
		HW_FAULT				((byte)10, PacketIDs.ALARMS_HARDWARE_FAULT			, false),
		REDUNDANCY_FAULT		((byte)11, PacketIDs.ALARMS_REDUNDANT_FAULT			, false),
		RF_OVERDRIVE			((byte)12, PacketIDs.ALARMS_RF_OVERDRIVEL			, false),
		ALL_IDs					((byte)13, PacketIDs.ALARMS_ALL_IDs					, false),
		BUC_B_COMMUNICATION_FAULT((byte)14, PacketIDs.ALARMS_BUC_B_COMMUNICATION_FAULT,false),
		BUC_B_SUMMARY			((byte)15, PacketIDs.ALARMS_BUC_B_SUMMARY			, false),
		BUC_S_COMMUNICATION_FAULT((byte)16, PacketIDs.ALARMS_BUC_S_COMMUNICATION_FAULT,false),
		BUC_S_SUMMARY			((byte)17, PacketIDs.ALARMS_BUC_S_SUMMARY			, false),
		TODO7					((byte)18, PacketIDs.ALARMS_TODO7					, false),
		TODO8					((byte)19, PacketIDs.ALARMS_TODO8					, false),
		TODO9					((byte)20, PacketIDs.ALARMS_TODO9					, false),
		TODO10					((byte)21, PacketIDs.ALARMS_TODO10					, false),
		LNB1_UNDER_CURRENT		((byte)30, PacketIDs.ALARMS_LNB1_UNDER_CURRENT		, false),
		LNB2_UNDER_CURRENT		((byte)31, PacketIDs.ALARMS_LNB2_UNDER_CURRENT		, false),
		PSU1					((byte)40, PacketIDs.ALARMS_PSU1					, false),
		PSU2					((byte)41, PacketIDs.ALARMS_PSU2					, false),

		DESCRIPTION_PLL_OUT_OF_LOCK			((byte) 1, PacketIDs.ALARMS_DESCRIPTION_PLL_OUT_OF_LOCK			, true),
		DESCRIPTION_TEMPERATURE_ThRESHOLD_1	((byte) 2, PacketIDs.ALARMS_DESCRIPTION_TEMPERATURE_ThRESHOLD_1	, true),
		DESCRIPTION_TEMPERATURE_ThRESHOLD_2	((byte) 3, PacketIDs.ALARMS_DESCRIPTION_TEMPERATURE_ThRESHOLD_2	, true),
		DESCRIPTION_OVER_CURRENT_ALARM		((byte) 4, PacketIDs.ALARMS_DESCRIPTION_OWER_CURRENT			, true),
		DESCRIPTION_UNDER_CURRENT_ALARM		((byte) 5, PacketIDs.ALARMS_DESCRIPTION_UNDER_CURRENT			, true),
		DESCRIPTION_ALC_ERROR				((byte) 6, PacketIDs.ALARMS_DESCRIPTION_ALC_ERROR				, true),
		DESCRIPTION_OVER_TEMPERATURE_ALARM	((byte) 7, PacketIDs.ALARMS_DESCRIPTION_OWER_TEMPERATURE		, true),
		DESCRIPTION_HW_FAULT				((byte)10, PacketIDs.ALARMS_DESCRIPTION_HARDWARE_FAULT			, true),
		DESCRIPTION_REDUNDANCY_FAULT		((byte)11, PacketIDs.ALARMS_DESCRIPTION_REDUNDANT_FAULT			, true),
		DESCRIPTION_RF_OVERDRIVE			((byte)12, PacketIDs.ALARMS_DESCRIPTION_RF_OVERDRIVEL			, true),
		DESCRIPTION_ALL_IDs					((byte)13, PacketIDs.ALARMS_DESCRIPTION_ALL_IDs					, true),
		DESCRIPTION_BUC_B_COMMUNICATION_FAULT((byte)14, PacketIDs.ALARMS_DESCRIPTION_BUC_B_COMMUNICATION_FAULT,true),
		DESCRIPTION_BUC_B_SUMMARY			((byte)15, PacketIDs.ALARMS_DESCRIPTION_BUC_B_SUMMARY			, true),
		DESCRIPTION_BUC_S_COMMUNICATION_FAULT((byte)16, PacketIDs.ALARMS_DESCRIPTION_BUC_S_COMMUNICATION_FAULT,true),
		DESCRIPTION_BUC_S_SUMMARY			((byte)17, PacketIDs.ALARMS_DESCRIPTION_BUC_S_SUMMARY			, true),
		DESCRIPTION_TODO7					((byte)18, PacketIDs.ALARMS_DESCRIPTION_TODO7					, true),
		DESCRIPTION_TODO8					((byte)19, PacketIDs.ALARMS_DESCRIPTION_TODO8					, true),
		DESCRIPTION_TODO9					((byte)20, PacketIDs.ALARMS_DESCRIPTION_TODO9					, true),
		DESCRIPTION_TODO10					((byte)21, PacketIDs.ALARMS_DESCRIPTION_TODO10					, true),
		DESCRIPTION_LNB1_UNDER_CURRENT		((byte)30, PacketIDs.ALARMS_DESCRIPTION_LNB1_UNDER_CURRENT		, true),
		DESCRIPTION_LNB2_UNDER_CURRENT		((byte)31, PacketIDs.ALARMS_DESCRIPTION_LNB2_UNDER_CURRENT		, true),
		DESCRIPTION_PSU1					((byte)40, PacketIDs.ALARMS_DESCRIPTION_PSU1					, true),
		DESCRIPTION_PSU2					((byte)41, PacketIDs.ALARMS_DESCRIPTION_PSU2					, true);

		private final static Logger logger = LogManager.getLogger();
		private final short alarmId;
		private final PacketIDs packetId;
		private final boolean isDescription;

		public PacketIDs getPacketId() {
			return packetId;
		}

		public short getAlarmId() {
			return alarmId;
		}

		private AlarmsPacketIds(short alarmId, PacketIDs packetId, boolean isDescription){
			this.alarmId = alarmId;
			this.packetId = packetId;
			this.isDescription = isDescription;
		}

		public static Optional<AlarmsPacketIds> valueOf(short alarmId, boolean description){
			final Optional<AlarmsPacketIds> oAlarmsPacketIds = Arrays.stream(values()).parallel().filter(a->a.isDescription==description).filter(a->a.alarmId==alarmId).findAny();

//			logger.error("alarmId: {}; {} : {}", alarmId, !oAlarmsPacketIds.isPresent(), oAlarmsPacketIds);
			if(!oAlarmsPacketIds.isPresent())
				logger.error("Alarm ID({}) does not exists", alarmId);

			return oAlarmsPacketIds;
		}
	}

	public enum DeviceDebugPacketIds{
		
		ADDR_0						(0		, 0		, PacketIDs.DEVICE_DEBUG_ADDR_0					, "DEVICE_DEBUG_ADDR_0"				),
		ADDR_1						(0		, 0		, PacketIDs.DEVICE_DEBUG_ADDR_1					, "DEVICE_DEBUG_ADDR_1"				),
		ADDR_2						(0		, 0		, PacketIDs.DEVICE_DEBUG_ADDR_2					, "DEVICE_DEBUG_ADDR_2"				),
		ADDR_3						(0		, 0		, PacketIDs.DEVICE_DEBUG_ADDR_3					, "DEVICE_DEBUG_ADDR_3"				),
		ADDR_4						(0		, 0		, PacketIDs.DEVICE_DEBUG_ADDR_4					, "DEVICE_DEBUG_ADDR_4"				),
		ADDR_8						(0		, 0		, PacketIDs.DEVICE_DEBUG_ADDR_8					, "DEVICE_DEBUG_ADDR_8"				),
		ADDR_11						(0		, 0		, PacketIDs.DEVICE_DEBUG_ADDR_11				, "DEVICE_DEBUG_ADDR_11"			),
		ADDR_100					(0		, 0		, PacketIDs.DEVICE_DEBUG_ADDR_100				, "DEVICE_DEBUG_ADDR_100"			),
		CALIBRATION_MODE			(0		, 0		, PacketIDs.DEVICE_DEBUG_CALIBRATION_MODE		, "DEVICE_DEBUG_CALIBRATION_MODE"	),

		INFO						(null	, null	, PacketIDs.DEVICE_DEBUG_INFO					, "DEVICE_DEBUG_DEVICE_INFO"		),
		DUMP_CONVERTER_INFO			(null	, 10	, PacketIDs.DEVICE_DEBUG_CONVERTER_INFO_DUMP	, "DUMP_CONVERTER_INFO"				),

		CPU_INFO					(null	, 0		, PacketIDs.DEVICE_DEBUG_CPU_INFO				, "DEVICE_DEBUG_CPU_INFO"			),
		HARDWARE_FAULTS_INFO		(null	, 1		, PacketIDs.DEVICE_DEBUG_HARDWARE_FAULTS_INFO	, "DEVICE_DEBUG_HARDWARE_FAULTS_INFO"),
		THRESHOLDS_INFO				(null	, 2		, PacketIDs.DEVICE_DEBUG_THRESHOLDS_INFO		, "DEVICE_DEBUG_THRESHOLDS_INFO"	),
		I2C1_INFO					(null	, 3		, PacketIDs.DEVICE_DEBUG_I2C1_INFO				, "DEVICE_DEBUG_I2C1_INFO"			),
		MUTE_INFO					(null	, 4		, PacketIDs.DEVICE_DEBUG_MUTE_INFO				, "DEVICE_DEBUG_MUTE_INFO"			),
		SCP_DEVICE_INFO				(null	, 10	, PacketIDs.DEVICE_DEBUG_SCP_DEVICE_INFO		, "DEVICE_DEBUG_SCP_DEVICE_INFO"	),

//		INFO_0					(0, 0, PacketIDs.DUMP_DEVICE_DEBUG_INFO_0				, "DUMP_DEVICE_DEBUG_INFO_0"			, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
//		INFO_1					(0, 1, PacketIDs.DUMP_DEVICE_DEBUG_INFO_1				, "DUMP_DEVICE_DEBUG_INFO_1"			, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
//		INFO_2					(0, 2, PacketIDs.DUMP_DEVICE_DEBUG_INFO_2				, "DUMP_DEVICE_DEBUG_INFO_2"			, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
//		INFO_3					(0, 3, PacketIDs.DUMP_DEVICE_DEBUG_INFO_3				, "DUMP_DEVICE_DEBUG_INFO_3"			, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
//		INFO_4					(0, 4, PacketIDs.DUMP_DEVICE_DEBUG_INFO_4				, "DUMP_DEVICE_DEBUG_INFO_4"			, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
//		INFO_10					(0, 10, PacketIDs.DUMP_DEVICE_DEBUG_INFO_10				, "DUMP_DEVICE_DEBUG_INFO_10"			, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
//		INFO_11					(0, 11, PacketIDs.DUMP_DEVICE_DEBUG_INFO_11				, "DUMP_DEVICE_DEBUG_INFO_11"			, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),

		DEVICEs_DUMP				(null	, 100	, PacketIDs.DEVICE_DEBUG_DEVICEs_DUMP			, "DEVICE_DEBUG_DEVICEs_DUMP"		),//TODO
		DEVICE1_DUMP				(null	, 1		, PacketIDs.DEVICE_DEBUG_DEVICE1_DUMP			, "DEVICE_DEBUG_DEVICE1_DUMP"		),
		DEVICE2_DUMP				(null	, 2		, PacketIDs.DEVICE_DEBUG_DEVICE2_DUMP			, "DEVICE_DEBUG_DEVICE2_DUMP"		),
		DEVICE3_DUMP				(null	, 7		, PacketIDs.DEVICE_DEBUG_DEVICE3_DUMP			, "DEVICE_DEBUG_DEVICE3_DUMP"		),
		HSS1_DUMP					(null	, 3		, PacketIDs.DEVICE_DEBUG_HSS1_DUMP				, "DEVICE_DEBUG_HSS1_DUMP"			),
		HSS2_DUMP					(null	, 4		, PacketIDs.DEVICE_DEBUG_HSS2_DUMP				, "DEVICE_DEBUG_HSS2_DUMP"			),
		VOLTAGES_DUMP				(null	, 5		, PacketIDs.DEVICE_DEBUG_VOLTAGES_DUMP			, "DEVICE_DEBUG_VOLTAGES_DUMP"		),
		PLL2_DUMP					(null	, 6		, PacketIDs.DEVICE_DEBUG_PLL2_DUMP				, "DEVICE_DEBUG_PLL2_DUMP"			),
		ADC_CHANNELS_DUMP			(null	, 10	, PacketIDs.DEVICE_DEBUG_ADC_CHANNELS_DUMP		, "DEVICE_DEBUG_ADC_CHANNELS_DUMP"	),
		ADC_CHANNELS_mV_DUMP		(null	, 11	, PacketIDs.DEVICE_DEBUG_ADC_CHANNELS_mV_DUMP	, "DEVICE_DEBUG_ADC_CHANNELS_mV_DUMP"),
		SIGNALS_CHANGE_COUNTERS_DUMP(null	, 20	, PacketIDs.DEVICE_DEBUG_CHANGE_COUNTERS_DUMP	, "DEVICE_DEBUG_CHANGE_COUNTERS_DUMP"),
		DP1_DUMP					(null	, 30	, PacketIDs.DEVICE_DEBUG_DP1_DUMP				, "DEVICE_DEBUG_DP1_DUMP"			),

		HELP						(0		, 0		, PacketIDs.DEVICE_DEBUG_HELP					, "DEVICE_DEBUG_HELP"				),

		HS1_CURRENT					(1		, 5		, PacketIDs.DEVICE_DEBUG_HS1_CURRENT			, "DEVICE_DEBUG_HS1_CURRENT"		),
		HS1_CURRENT_REMOTE_BIAS		(1		, 205	, PacketIDs.DEVICE_DEBUG_HS1_CURRENT_REMOTE_BIAS, "DEVICE_DEBUG_HS1_CURRENT_REMOTE_BIAS"),
		HS1_1_CURRENT_HP_BIAS		(16		, 20	, PacketIDs.DEVICE_DEBUG_HS1_1_CURRENT_HP_BIAS	, "DEVICE_DEBUG_HS1_1_CURRENT"		),
		HS1_2_CURRENT_HP_BIAS		(17		, 20	, PacketIDs.DEVICE_DEBUG_HS1_2_CURRENT_HP_BIAS	, "DEVICE_DEBUG_HS1_2_CURRENT"		),
//		HS1_CURRENT_HP_BIAS_REMOTE	(0		, 0		, PacketIDs.DEVICE_DEBUG_HS1_CURRENT			, "DEVICE_DEBUG_HS1_CURRENT"		),
		HS2_CURRENT					(2		, 5		, PacketIDs.DEVICE_DEBUG_HS2_CURRENT			, "DEVICE_DEBUG_HS2_CURRENT"		),
		HS2_CURRENT_REMOTE_BIAS		(2		, 205	, PacketIDs.DEVICE_DEBUG_HS2_CURRENT_REMOTE_BIAS, "DEVICE_DEBUG_HS2_CURRENT_REMOTE_BIAS"),
		HS2_1_CURRENT_HP_BIAS		(18		, 20	, PacketIDs.DEVICE_DEBUG_HS2_1_CURRENT_HP_BIAS	, "DEVICE_DEBUG_HS2_1_CURRENT_HP_BIAS"),
		HS2_2_CURRENT_HP_BIAS		(19		, 20	, PacketIDs.DEVICE_DEBUG_HS2_2_CURRENT_HP_BIAS	, "DEVICE_DEBUG_HS2_2_CURRENT_HP_BIAS"),
		HS3_1_CURRENT_HP_BIAS		(20		, 20	, PacketIDs.DEVICE_DEBUG_HS3_1_CURRENT_HP_BIAS	, "DEVICE_DEBUG_HS3_1_CURRENT_HP_BIAS"),
		HS3_2_CURRENT_HP_BIAS		(21		, 20	, PacketIDs.DEVICE_DEBUG_HS3_2_CURRENT_HP_BIAS	, "DEVICE_DEBUG_HS3_2_CURRENT_HP_BIAS"),
		HS4_1_CURRENT_HP_BIAS		(22		, 20	, PacketIDs.DEVICE_DEBUG_HS4_1_CURRENT_HP_BIAS	, "DEVICE_DEBUG_HS4_1_CURRENT_HP_BIAS"),
		HS4_2_CURRENT_HP_BIAS		(23		, 20	, PacketIDs.DEVICE_DEBUG_HS4_2_CURRENT_HP_BIAS	, "DEVICE_DEBUG_HS4_2_CURRENT_HP_BIAS"),

		OUTPUT_POWER				(3		, 5		, PacketIDs.DEVICE_DEBUG_OUTPUT_POWER			, "DEVICE_DEBUG_OUTPUT_POWER"		),
		OUTPUT_POWER_REMOTE_BIAS	(3		, 205	, PacketIDs.DEVICE_DEBUG_OUTPUT_POWER_REMOTE_BIAS, "DEVICE_DEBUG_OUTPUT_POWER_REMOTE_BIAS"),
		OUTPUT_POWER_HP_BIAS		(0		, 20	, PacketIDs.DEVICE_DEBUG_OUTPUT_POWER_HP_BIAS	, "DEVICE_DEBUG_OUTPUT_POWER_HP_BIAS"),

		TEMPERATURE					(4		, 5		, PacketIDs.DEVICE_DEBUG_TEMPERATURE			, "DEVICE_DEBUG_TEMPERATURE"		),
		TEMPERATURE_REMOTE_BIAS		(4		, 205	, PacketIDs.DEVICE_DEBUG_TEMPERATURE_REMOTE_BIAS, "DEVICE_DEBUG_TEMPERATURE_REMOTE_BIAS"),
		TEMPERATURE_HP_BIAS			(4		, 20	, PacketIDs.DEVICE_DEBUG_TEMPERATURE_HP_BIAS	, "DEVICE_DEBUG_TEMPERATURE_HP_BIAS"),

		NGLOBAL						(0		, 6		, PacketIDs.DEVICE_DEBUG_NGLOBAL				, "DEVICE_DEBUG_NGLOBAL"			),
		NGLOBAL_REMOTE_BIAS			(0		, 206	, PacketIDs.DEVICE_DEBUG_NGLOBAL_REMOTE_BIAS	, "DEVICE_DEBUG_NGLOBAL_REMOTE_BIAS"),

		POTENTIOMETER_N1		(0		, 0		, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N1		, "DEVICE_DEBUG_POTENTIOMETER_N1"	),
		POTENTIOMETER_N2		(0		, 0		, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N2		, "DEVICE_DEBUG_POTENTIOMETER_N2"	),
		POTENTIOMETER_N3		(0		, 0		, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N3		, "DEVICE_DEBUG_POTENTIOMETER_N3"	),
		POTENTIOMETER_N4		(0		, 0		, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N4		, "DEVICE_DEBUG_POTENTIOMETER_N4"	),
		POTENTIOMETER_N5		(0		, 0		, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N5		, "DEVICE_DEBUG_POTENTIOMETER_N5"	),
		POTENTIOMETER_N6		(0		, 0		, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N6		, "DEVICE_DEBUG_POTENTIOMETER_N6"	),
		POTENTIOMETER_N1_SET	(0		, 0		, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N1_SET	, "DEVICE_DEBUG_POTENTIOMETER_N1_SET"),
		POTENTIOMETER_N2_SET	(0		, 0		, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N2_SET	, "DEVICE_DEBUG_POTENTIOMETER_N2_SET"),
		POTENTIOMETER_N3_SET	(0		, 0		, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N3_SET	, "DEVICE_DEBUG_POTENTIOMETER_N3_SET"),
		POTENTIOMETER_N4_SET	(0		, 0		, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N4_SET	, "DEVICE_DEBUG_POTENTIOMETER_N4_SET"),
		POTENTIOMETER_N5_SET	(0		, 0		, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N5_SET	, "DEVICE_DEBUG_POTENTIOMETER_N5_SET"),
		POTENTIOMETER_N6_SET	(0		, 0		, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N6_SET	, "DEVICE_DEBUG_POTENTIOMETER_N6_SET"),
		SWITCH_N1				(1		, 3		, PacketIDs.DEVICE_DEBUG_SWITCH_N1				, "DEVICE_DEBUG_SWITCH_N1"			),
		SWITCH_N1_REMOTE_BIAS	(1		, 203	, PacketIDs.DEVICE_DEBUG_SWITCH_N1_REMOTE_BIAS	, "DEVICE_DEBUG_SWITCH_N1_REMOTE_BIAS"),
		SWITCH_N2				(1		, 4		, PacketIDs.DEVICE_DEBUG_SWITCH_N2				, "DEVICE_DEBUG_SWITCH_N2"			),
		SWITCH_N2_REMOTE_BIAS	(1		, 204	, PacketIDs.DEVICE_DEBUG_SWITCH_N2_REMOTE_BIAS	, "DEVICE_DEBUG_SWITCH_N2_REMOTE_BIAS"),
		CONVERTER_PLL_1			(0, 0, PacketIDs.DEVICE_DEBUG_CONVERTER_PLL_1			, "DEVICE_DEBUG_CONVERTER_PLL_1"	),
		CONVERTER_PLL_2			(0, 0, PacketIDs.DEVICE_DEBUG_CONVERTER_PLL_2			, "DEVICE_DEBUG_CONVERTER_PLL_2"	),

		PLL_REG					(0, 0, PacketIDs.FCM_DEVICE_DEBUG_PLL_REG				, "FCM_DEVICE_DEBUG_PLL_REG"		),
		PLL_REG_DOWN_GAIN		(0, 0, PacketIDs.FCM_DEVICE_DEBUG_PLL_REG_DOWN_GAIN	, "FCM_DEVICE_DEBUG_PLL_REG_DOWN_GAIN"	),
		PLL_REG_UP_GAIN			(0, 0, PacketIDs.FCM_DEVICE_DEBUG_PLL_REG_UP_GAIN		, "FCM_DEVICE_DEBUG_PLL_REG_UP_GAIN"),
		PLL_REG_OFFSET			(0, 0, PacketIDs.FCM_DEVICE_DEBUG_PLL_REG_OFFSET		, "FCM_DEVICE_DEBUG_PLL_REG_OFFSET"	),

		FCM_R31_ADC_INPUT_POWER	(15, 10	, PacketIDs.FCM_ADC_INPUT_POWER					, "FCM_ADC_INPUT_POWER"				),
		FCM_R31_ADC_OUTPUT_POWER( 9, 14	, PacketIDs.FCM_ADC_OUTPUT_POWER				, "FCM_ADC_OUTPUT_POWER"			),
		FCM_R31_ADC_TEMPERATURE	( 8, 10	, PacketIDs.FCM_ADC_TEMPERATURE					, "FCM_ADC_TEMPERATURE"				),
		FCM_R31_ADC_CLKREF_LVL	(11, 10	, PacketIDs.FCM_ADC_CLKREF_LVL					, "FCM_ADC_CLKREF_LVL"				),
		FCM_R31_ADC_5V5			( 3, 10	, PacketIDs.FCM_ADC_5V5							, "FCM_ADC_5V5"						),

		FCM_R31_ADC_INPUT_POWER_mV	(15, 11	, PacketIDs.FCM_ADC_INPUT_POWER				, "FCM_ADC_INPUT_POWER_mV"			),
		FCM_R31_ADC_OUTPUT_POWER_mV	( 9, 15	, PacketIDs.FCM_ADC_OUTPUT_POWER			, "FCM_ADC_OUTPUT_POWER_mV"			),
		FCM_R31_ADC_TEMPERATURE_mV	( 8, 11	, PacketIDs.FCM_ADC_TEMPERATURE				, "FCM_ADC_TEMPERATURE_mV"			),
		FCM_R31_ADC_CLKREF_LVL_mV	(11, 11	, PacketIDs.FCM_ADC_CLKREF_LVL				, "FCM_ADC_CLKREF_LVL_mV"			),
		FCM_R31_ADC_5V5_mV			( 3, 11	, PacketIDs.FCM_ADC_5V5						, "FCM_ADC_5V5_mV"					),

		FCM_ADC_INPUT_POWER		(0, 10	, PacketIDs.FCM_ADC_INPUT_POWER					, "FCM_ADC_INPUT_POWER"				),
		FCM_ADC_OUTPUT_POWER	(1, 10	, PacketIDs.FCM_ADC_OUTPUT_POWER				, "FCM_ADC_OUTPUT_POWER"			),
		FCM_ADC_TEMPERATURE		(2, 10	, PacketIDs.FCM_ADC_TEMPERATURE					, "FCM_ADC_TEMPERATURE"				),
		FCM_ADC_CURRENT			(4, 10	, PacketIDs.FCM_ADC_CURRENT						, "FCM_ADC_CURRENT"					),
		FCM_ADC_5V5				(6, 10	, PacketIDs.FCM_ADC_5V5							, "FCM_ADC_5V5"						),
		FCM_ADC_13v2			(7, 10	, PacketIDs.FCM_ADC_13v2						, "FCM_ADC_13v2"					),
		FCM_ADC_13V2_NEG		(8, 10	, PacketIDs.FCM_ADC_13V2_NEG					, "FCM_ADC_13V2_NEG"				),

		FCM_ADC_INPUT_POWER_mV	(0, 11	, PacketIDs.FCM_ADC_INPUT_POWER					, "FCM_ADC_INPUT_POWER_mV"			),
		FCM_ADC_OUTPUT_POWER_mV	(1, 11	, PacketIDs.FCM_ADC_OUTPUT_POWER				, "FCM_ADC_OUTPUT_POWER_mV"			),
		FCM_ADC_TEMPERATURE_mV	(2, 11	, PacketIDs.FCM_ADC_TEMPERATURE					, "FCM_ADC_TEMPERATURE_mV"			),
		FCM_ADC_CURRENT_mV		(4, 11	, PacketIDs.FCM_ADC_CURRENT						, "FCM_ADC_CURRENT_mV"				),
		FCM_ADC_5V5_mV			(6, 11	, PacketIDs.FCM_ADC_5V5							, "FCM_ADC_5V5_mV"					),
		FCM_ADC_13v2_mV			(7, 11	, PacketIDs.FCM_ADC_13v2						, "FCM_ADC_13v2_mV"					),
		FCM_ADC_13V2_NEG_mV		(8, 11	, PacketIDs.FCM_ADC_13V2_NEG					, "FCM_ADC_13V2_NEG_mV"				),

		DAC1					(0, 1	, PacketIDs.DEVICE_CONVERTER_DAC1				, "DEVICE_CONVERTER_DAC1"			),	// PACKET_ID_DEVICE_FCM_INDEX_1 + address
		DAC2					(0, 2	, PacketIDs.DEVICE_CONVERTER_DAC2				, "DEVICE_CONVERTER_DAC2"			),
		DAC3					(0, 3	, PacketIDs.DEVICE_CONVERTER_DAC3				, "DEVICE_CONVERTER_DAC3"			),
		DAC4					(0, 4	, PacketIDs.DEVICE_CONVERTER_DAC4				, "DEVICE_CONVERTER_DAC4"			),

		FCM_DAC1				(0, 1	, PacketIDs.DEVICE_CONVERTER_DAC1_FCM			, "DEVICE_CONVERTER_DAC1_FCM"		),	// PACKET_ID_DEVICE_FCM_INDEX_1 + address
		FCM_DAC2				(0, 2	, PacketIDs.DEVICE_CONVERTER_DAC2_FCM			, "DEVICE_CONVERTER_DAC2_FCM"		),
		FCM_DAC3				(0, 3	, PacketIDs.DEVICE_CONVERTER_DAC3_FCM			, "DEVICE_CONVERTER_DAC3_FCM"		),
		FCM_DAC4				(0, 4	, PacketIDs.DEVICE_CONVERTER_DAC4_FCM			, "DEVICE_CONVERTER_DAC4_FCM"		);

		private static final int INDEX = 2;
		private static final int ADDR = 1;

		private final static Logger logger = LogManager.getLogger();
		private final PacketIDs packetId;
		private final String text;
		private final Integer addr;
		private final Integer index;

		private DeviceDebugPacketIds(Integer index, Integer addr, PacketIDs packetId, String text){

			this.addr = addr;
			this.index = index;
			this.packetId = packetId;
			this.text = text;
		}

		public byte[] getPayloadData() {
			int caseIndex = 0;

			if(index!=null)
				caseIndex += ADDR;

			if(addr!=null)
				caseIndex += INDEX;

			switch(caseIndex){

			case ADDR:
				return ByteBuffer.allocate(4).putInt(index).array();

			case INDEX:
				return ByteBuffer.allocate(4).putInt(addr).array();

			case ADDR + INDEX:
				return ByteBuffer.allocate(8).putInt(addr).putInt(index).array();
			}
			return null;
		}

		public PacketIDs getPacketId() {
			return packetId;
		}

		public static Optional<DeviceDebugPacketIds> valueOf(short id){
			final Optional<DeviceDebugPacketIds> oAlarmsPacketIds = Arrays.stream(values()).parallel().filter(a->a.packetId.getId()==id).findAny();

			if(!oAlarmsPacketIds.isPresent())
				logger.warn(id);

			return oAlarmsPacketIds;
		}

		public static Optional<DeviceDebugPacketIds> valueOf(DeviceDebugType deviceDebugType, int index) {
			return Arrays
					.stream(
							values())
					.filter(
							pId->
							pId.packetId.getParameterCode()==deviceDebugType.getParameterCode())
					.filter(
							pId->
							pId.addr!=null)
					.filter(
							pId->
							pId.addr==index)
					.findAny();
		}

		public static Optional<DeviceDebugPacketIds> valueOf(PacketIDs packetId) {
			return Arrays.stream(values()).filter(pId->pId.packetId.equals(packetId)).findAny();
		}

		public Integer getIndex() {
			return addr;
		}

		@Override
		public String toString(){
			return text;
		}
	}

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
