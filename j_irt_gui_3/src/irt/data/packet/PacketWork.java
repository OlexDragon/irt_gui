package irt.data.packet;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.listener.ValueChangeListener;
import irt.data.packet.PacketSuper.Priority;
import irt.data.packet.alarm.AlarmDescriptionPacket;
import irt.data.packet.alarm.AlarmStatusPacket;
import irt.data.packet.alarm.AlarmsIDsPacket;
import irt.data.packet.alarm.AlarmsSummaryPacket;
import irt.data.packet.configuration.ALCEnablePacket;
import irt.data.packet.configuration.AttenuationPacket;
import irt.data.packet.configuration.AttenuationRangePacket;
import irt.data.packet.configuration.ConfifurationPacket;
import irt.data.packet.configuration.FrequencyPacket;
import irt.data.packet.configuration.LOFrequenciesPacket;
import irt.data.packet.configuration.LOPacket;
import irt.data.packet.configuration.LnbPowerPacket;
import irt.data.packet.configuration.LnbReferencePacket;
import irt.data.packet.configuration.LnbSwitchPacket;
import irt.data.packet.configuration.MuteControlPacket;
import irt.data.packet.configuration.Offset1to1toMultiPacket;
import irt.data.packet.configuration.RedundancyEnablePacket;
import irt.data.packet.configuration.RedundancyModePacket;
import irt.data.packet.configuration.RedundancyNamePacket;
import irt.data.packet.configuration.RedundancyStatusPacket;
import irt.data.packet.configuration.SpectrumInversionPacket;
import irt.data.packet.control.ActiveModulePacket;
import irt.data.packet.control.ModuleListPacket;
import irt.data.packet.denice_debag.CallibrationModePacket;
import irt.data.packet.denice_debag.DeviceDebugInfoPacket;
import irt.data.packet.denice_debag.DeviceDebugPacket.DeviceDebugType;
import irt.data.packet.denice_debag.RegisterPacket;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.interfaces.PacketThreadWorker;
import irt.data.packet.measurement.MeasurementPacket;
import irt.data.packet.network.NetworkAddressPacket;
import irt.data.packet.redundancy.RedundancyControllerStatusPacket;
import irt.data.packet.redundancy.SwitchoverModePacket;

public interface PacketWork extends Comparable<PacketWork>{

	public enum PacketIDs{

			UNNECESSARY	(null, null),

			DEVICE_INFO	(null, DeviceInfoPacket.parseValueFunction),

			ALARMS_IDs						(null, AlarmsIDsPacket.parseValueFunction),
			ALARMS_SUMMARY					(null, AlarmsSummaryPacket.parseValueFunction),
			ALARMS_OWER_CURRENT				(null, AlarmStatusPacket.parseValueFunction),
			ALARMS_UNDER_CURRENT			(null, AlarmStatusPacket.parseValueFunction),
			ALARMS_OWER_TEMPERATURE			(null, AlarmStatusPacket.parseValueFunction),
			ALARMS_PLL_OUT_OF_LOCK			(null, AlarmStatusPacket.parseValueFunction),
			ALARMS_HARDWARE_FAULT			(null, AlarmStatusPacket.parseValueFunction),
			ALARMS_REDUNDANT_FAULT			(null, AlarmStatusPacket.parseValueFunction),
			ALARMS_test						(null, AlarmStatusPacket.parseValueFunction),
			ALARMS_NO_INPUT_SIGNAL			(null, AlarmStatusPacket.parseValueFunction),
			ALARMS_RF_OVERDRIVEL			(null, AlarmStatusPacket.parseValueFunction),
			ALARMS_STATUS					(null, AlarmStatusPacket.parseValueFunction),
			ALARMS_TEMPERATURE_ThRESHOLD_1	(null, AlarmStatusPacket.parseValueFunction),
			ALARMS_TEMPERATURE_ThRESHOLD_2	(null, AlarmStatusPacket.parseValueFunction),
			ALARMS_ALC_ERROR				(null, AlarmStatusPacket.parseValueFunction),
			ALARMS_ALL_IDs					(null, AlarmStatusPacket.parseValueFunction),
			ALARMS_BUC_B_COMMUNICATION_FAULT(null, AlarmStatusPacket.parseValueFunction),
			ALARMS_BUC_B_SUMMARY			(null, AlarmStatusPacket.parseValueFunction),
			ALARMS_BUC_S_COMMUNICATION_FAULT(null, AlarmStatusPacket.parseValueFunction),
			ALARMS_TODO4					(null, AlarmStatusPacket.parseValueFunction),
			ALARMS_TODO5					(null, AlarmStatusPacket.parseValueFunction),
			ALARMS_BUC_S_SUMMARY			(null, AlarmStatusPacket.parseValueFunction),
			ALARMS_TODO7					(null, AlarmStatusPacket.parseValueFunction),
			ALARMS_TODO8					(null, AlarmStatusPacket.parseValueFunction),
			ALARMS_TODO9					(null, AlarmStatusPacket.parseValueFunction),

			ALARMS_DESCRIPTION_OWER_CURRENT				(null, AlarmDescriptionPacket.parseValueFunction),
			ALARMS_DESCRIPTION_UNDER_CURRENT			(null, AlarmDescriptionPacket.parseValueFunction),
			ALARMS_DESCRIPTION_OWER_TEMPERATURE			(null, AlarmDescriptionPacket.parseValueFunction),
			ALARMS_DESCRIPTION_PLL_OUT_OF_LOCK			(null, AlarmDescriptionPacket.parseValueFunction),
			ALARMS_DESCRIPTION_HARDWARE_FAULT			(null, AlarmDescriptionPacket.parseValueFunction),
			ALARMS_DESCRIPTION_REDUNDANT_FAULT			(null, AlarmDescriptionPacket.parseValueFunction),
			ALARMS_DESCRIPTION_test						(null, AlarmDescriptionPacket.parseValueFunction),
			ALARMS_DESCRIPTION_NO_INPUT_SIGNAL			(null, AlarmDescriptionPacket.parseValueFunction),
			ALARMS_DESCRIPTION_RF_OVERDRIVEL			(null, AlarmDescriptionPacket.parseValueFunction),
			ALARMS_DESCRIPTION_STATUS					(null, AlarmDescriptionPacket.parseValueFunction),
			ALARMS_DESCRIPTION_TEMPERATURE_ThRESHOLD_1	(null, AlarmDescriptionPacket.parseValueFunction),
			ALARMS_DESCRIPTION_TEMPERATURE_ThRESHOLD_2	(null, AlarmDescriptionPacket.parseValueFunction),
			ALARMS_DESCRIPTION_ALC_ERROR				(null, AlarmDescriptionPacket.parseValueFunction),
			ALARMS_DESCRIPTION_ALL_IDs					(null, AlarmDescriptionPacket.parseValueFunction),
			ALARMS_DESCRIPTION_BUC_B_COMMUNICATION_FAULT(null, AlarmDescriptionPacket.parseValueFunction),
			ALARMS_DESCRIPTION_BUC_B_SUMMARY			(null, AlarmDescriptionPacket.parseValueFunction),
			ALARMS_DESCRIPTION_BUC_S_COMMUNICATION_FAULT(null, AlarmDescriptionPacket.parseValueFunction),
			ALARMS_DESCRIPTION_TODO4					(null, AlarmDescriptionPacket.parseValueFunction),
			ALARMS_DESCRIPTION_TODO5					(null, AlarmDescriptionPacket.parseValueFunction),
			ALARMS_DESCRIPTION_BUC_S_SUMMARY			(null, AlarmDescriptionPacket.parseValueFunction),
			ALARMS_DESCRIPTION_TODO7					(null, AlarmDescriptionPacket.parseValueFunction),
			ALARMS_DESCRIPTION_TODO8					(null, AlarmDescriptionPacket.parseValueFunction),
			ALARMS_DESCRIPTION_TODO9					(null, AlarmDescriptionPacket.parseValueFunction),

			MEASUREMENT_ALL						(null, MeasurementPacket.parseValueFunction),
			MEASUREMENT_STATUS					(null, null),
			MEASUREMENT_INPUT_POWER				(null, null),
			MEASUREMENT_BAIAS_25W_OUTPUT_POWER	(null, null),
			MEASUREMENT_TEMPERATURE				(null, null),
			MEASUREMENT_UNIT_TEMPERATURE		(null, null),
			MEASUREMENT_CPU_TEMPERATURE			(null, null),
			MEASUREMENT_5V5						(null, null),
			MEASUREMENT_13V2					(null, null),
			MEASUREMENT_13V2_NEG				(null, null),
			MEASUREMENT_OUTPUT_POWER			(null, null),
			MEASUREMENT_SNB1_STATUS				(null, null),
			MEASUREMENT_SNB2_STATUS				(null, null),
			MEASUREMENT_WGS_POSITION			(null, null),

			CONFIGURATION_LO					(null, LOPacket.parseValueFunction),
			CONFIGURATION_LO_FREQUENCIES		(null, LOFrequenciesPacket.parseValueFunction),
			CONFIGURATION_MUTE					(null, MuteControlPacket.parseValueFunction),
			CONFIGURATION_GAIN					(null, AttenuationPacket.parseValueFunction),
			CONFIGURATION_GAIN_RANGE			(null, AttenuationRangePacket.parseValueFunction),
			CONFIGURATION_ATTENUATION			(null, AttenuationPacket.parseValueFunction),
			CONFIGURATION_ATTENUATION_RANGE		(null, AttenuationRangePacket.parseValueFunction),
			CONFIGURATION_FREQUENCY				(null, FrequencyPacket.parseValueFunction),
			CONFIGURATION_FREQUENCY_RANGE		(null, AttenuationRangePacket.parseValueFunction),
			STORE_CONFIG						(null, null),
			CONFIGURATION_FCM_LNB_POWER			(null, LnbPowerPacket.parseValueFunction),
			CONFIGURATION_GAIN_OFFSET			(null, ConfifurationPacket.parseValueFunction),
			CONFIGURATION_FCM_FLAGS				(null, null),
			CONFIGURATION_REDUNDANCY_ENABLE		(null, RedundancyEnablePacket	.parseValueFunction),
			CONFIGURATION_REDUNDANCY_MODE		(null, RedundancyModePacket		.parseValueFunction),
			CONFIGURATION_REDUNDANCY_NAME		(null, RedundancyNamePacket		.parseValueFunction),
			CONFIGURATION_REDUNDANCY_STATUS		(null, RedundancyStatusPacket	.parseValueFunction),
			CONFIGURATION_REDUNDANCY_SET_ONLINE	(null, null),
			CONFIGURATION_MUTE_OUTDOOR			(null, null),
			CONFIGURATION_ALC_ENABLE			(null, ALCEnablePacket			.parseValueFunction),
			CONFIGURATION_ALC_LEVEL				(null, null),
			CONFIGURATION_ALC_RANGE				(null, AttenuationRangePacket	.parseValueFunction),
			CONFIGURATION_DLRS_WGS_SWITCHOVER	(null, LnbSwitchPacket			.parseValueFunction),
			CONFIGURATION_SET_DLRS_WGS_SWITCHOVER(null, null),
			CONFIGURATION_SPECTRUM_INVERSION	(null, SpectrumInversionPacket	.parseValueFunction),
			CONFIGURATION_REFERENCE_CONTROL		(null, null),
			CONFIGURATION_FCM_LNB_REFERENCE		(null, LnbReferencePacket		.parseValueFunction),
			CONFIGURATION_OFFSET_RANGE			(null, AttenuationRangePacket	.parseValueFunction),
			CONFIGURATION_OFFSET_1_TO_MULTI		(null, Offset1to1toMultiPacket	.parseValueFunction),

			CONTROL_ACTIVE_MODULE	(null, ActiveModulePacket.parseValueFunction),
			CONTRO_MODULE_LIST		(null, ModuleListPacket.parseValueFunction),

			FCM_DEVICE_DEBUG_PLL_REG			(null, null),
			FCM_DEVICE_DEBUG_PLL_REG_DOWN_GAIN	(null, null),
			FCM_DEVICE_DEBUG_PLL_REG_UP_GAIN	(null, null),
			FCM_DEVICE_DEBUG_PLL_REG_OFFSET		(null, null),

			FCM_ADC_INPUT_POWER		(null, RegisterPacket.parseValueFunction),
			FCM_ADC_OUTPUT_POWER	(null, RegisterPacket.parseValueFunction),
			FCM_ADC_TEMPERATURE		(null, RegisterPacket.parseValueFunction),
			FCM_ADC_CURRENT			(null, RegisterPacket.parseValueFunction),
			FCM_ADC_5V5				(null, RegisterPacket.parseValueFunction),
			FCM_ADC_13v2			(null, RegisterPacket.parseValueFunction),
			FCM_ADC_13V2_NEG		(null, RegisterPacket.parseValueFunction),

			PRODUCTION_GENERIC_SET_1_INITIALIZE(null, null),

			NETWORK_ADDRESS		(null, NetworkAddressPacket.parseValueFunction),

			REDUNDANCY_STATUS			(null, RedundancyControllerStatusPacket.parseValueFunction),
			REDUNDANCY_SWITCHOVER		(null, null),
			REDUNDANCY_MODE				(null, RedundancyModePacket.parseValueFunction),
			REDUNDANCY_SWITCHOVER_MODE	(null, SwitchoverModePacket.parseValueFunction),

			DEVICES(null, null),
			DEVICE_DEBUG_CALIBRATION_MODE	(null, CallibrationModePacket.parseValueFunction),
			DEVICE_DEBUG_CONVERTER_PLL_1	(null, null),
			DEVICE_DEBUG_CONVERTER_PLL_2	(null, null),

			DEVICE_DEBUG_INFO					(null, DeviceDebugInfoPacket.parseValueFunction),
			DEVICE_DEBUG_CPU_INFO				(null, DeviceDebugInfoPacket.parseValueFunction),
			DEVICE_DEBUG_HARDWARE_FAULTS_INFO	(null, DeviceDebugInfoPacket.parseValueFunction),
			DEVICE_DEBUG_THRESHOLDS_INFO		(null, DeviceDebugInfoPacket.parseValueFunction),
			DEVICE_DEBUG_I2C1_INFO				(null, DeviceDebugInfoPacket.parseValueFunction),
			DEVICE_DEBUG_MUTE_INFO				(null, DeviceDebugInfoPacket.parseValueFunction),
			DEVICE_DEBUG_SCP_DEVICE_INFO		(null, DeviceDebugInfoPacket.parseValueFunction),

			DEVICE_DEBUG_PLL2_DUMP				(null, DeviceDebugInfoPacket.parseValueFunction),
			DEVICE_DEBUG_DEVICEs_DUMP			(null, DeviceDebugInfoPacket.parseValueFunction),
			DEVICE_DEBUG_DEVICE1_DUMP			(null, DeviceDebugInfoPacket.parseValueFunction),
			DEVICE_DEBUG_DEVICE2_DUMP			(null, DeviceDebugInfoPacket.parseValueFunction),
			DEVICE_DEBUG_DEVICE3_DUMP			(null, DeviceDebugInfoPacket.parseValueFunction),
			DEVICE_DEBUG_HSS1_DUMP				(null, DeviceDebugInfoPacket.parseValueFunction),
			DEVICE_DEBUG_HSS2_DUMP				(null, DeviceDebugInfoPacket.parseValueFunction),
			DEVICE_DEBUG_VOLTAGES_DUMP			(null, DeviceDebugInfoPacket.parseValueFunction),
			DEVICE_DEBUG_ADC_CHANNELS_DUMP		(null, DeviceDebugInfoPacket.parseValueFunction),
			DEVICE_DEBUG_ADC_CHANNELS_mV_DUMP	(null, DeviceDebugInfoPacket.parseValueFunction),
			DEVICE_DEBUG_CHANGE_COUNTERS_DUMP	(null, DeviceDebugInfoPacket.parseValueFunction),
			DEVICE_DEBUG_DP1_DUMP				(null, DeviceDebugInfoPacket.parseValueFunction),

			DEVICE_DEBUG_HELP			(null, DeviceDebugInfoPacket.parseValueFunction),
			DEVICE_POTENTIOMETERS_INIT	(null, null),

			DEVICE_FCM_INDEX_1	(null, null),
			//FCM packet IDs mixed index and address
			DEVICE_CONVERTER_DAC1_FCM(null, null),	// PACKET_ID_DEVICE_FCM_INDEX_1 + address
			DEVICE_CONVERTER_DAC2_FCM(null, null),
			DEVICE_CONVERTER_DAC3_FCM(null, null),
			DEVICE_CONVERTER_DAC4_FCM(null, null),

			//address = 0
			DEVICE_DEBUG_ADDR_0				(null, RegisterPacket.parseValueFunction),
			DEVICE_DEBUG_POTENTIOMETER_N2	(null, RegisterPacket.parseValueFunction),	//PACKET_ID_DEVICE_DEBUG_ADDR_0 + index
			DEVICE_DEBUG_POTENTIOMETER_N5	(null, RegisterPacket.parseValueFunction),
			DEVICE_DEBUG_POTENTIOMETER_N4	(null, RegisterPacket.parseValueFunction),


			//address = 1
			DEVICE_DEBUG_ADDR_1					(null, RegisterPacket.parseValueFunction),
			DEVICE_DEBUG_SWITCH_N1				(null, RegisterPacket.parseValueFunction),	//PACKET_ID_DEVICE_DEBUG_ADDR_1 + index
			DEVICE_DEBUG_SWITCH_N2				(null, RegisterPacket.parseValueFunction),
			DEVICE_DEBUG_HS1_CURRENT			(null, RegisterPacket.parseValueFunction),
			DEVICE_DEBUG_NGLOBAL				(null, RegisterPacket.parseValueFunction),
			DEVICE_DEBUG_SWITCH_N1_REMOTE_BIAS	(null, RegisterPacket.parseValueFunction),
			DEVICE_DEBUG_SWITCH_N2_REMOTE_BIAS	(null, RegisterPacket.parseValueFunction),
			DEVICE_DEBUG_HS1_CURRENT_REMOTE_BIAS(null, RegisterPacket.parseValueFunction),
			DEVICE_DEBUG_HS1_CURRENT_HP_BIAS	(null, RegisterPacket.parseValueFunction),
			DEVICE_DEBUG_NGLOBAL_REMOTE_BIAS	(null, RegisterPacket.parseValueFunction),

			//address = 2
			DEVICE_DEBUG_ADDR_2					(null, RegisterPacket.parseValueFunction),
			DEVICE_DEBUG_HS2_CURRENT			(null, RegisterPacket.parseValueFunction),	//PACKET_ID_DEVICE_DEBUG_ADDR_2 + index
			DEVICE_DEBUG_HS2_CURRENT_REMOTE_BIAS(null, RegisterPacket.parseValueFunction),
			DEVICE_DEBUG_HS2_CURRENT_HP_BIAS	(null, RegisterPacket.parseValueFunction),

			//address = 3
			DEVICE_DEBUG_ADDR_3						(null, RegisterPacket.parseValueFunction),
			DEVICE_DEBUG_POTENTIOMETER_N2_SET		(null, RegisterPacket.parseValueFunction),	//PACKET_ID_DEVICE_DEBUG_ADDR_3 + index
			DEVICE_DEBUG_POTENTIOMETER_N5_SET		(null, RegisterPacket.parseValueFunction),
			DEVICE_DEBUG_OUTPUT_POWER				(null, RegisterPacket.parseValueFunction),
			DEVICE_DEBUG_OUTPUT_POWER_REMOTE_BIAS	(null, RegisterPacket.parseValueFunction),
			DEVICE_DEBUG_OUTPUT_POWER_HP_BIAS		(null, RegisterPacket.parseValueFunction),
			DEVICE_DEBUG_POTENTIOMETER_N4_SET		(null, RegisterPacket.parseValueFunction),

			//address = 4
			DEVICE_DEBUG_ADDR_4					(null, RegisterPacket.parseValueFunction),
			DEVICE_DEBUG_TEMPERATURE			(null, RegisterPacket.parseValueFunction),	//PACKET_ID_DEVICE_DEBUG_ADDR_4 + index
			DEVICE_DEBUG_TEMPERATURE_REMOTE_BIAS(null, RegisterPacket.parseValueFunction),
			DEVICE_DEBUG_TEMPERATURE_HP_BIAS	(null, RegisterPacket.parseValueFunction),

			//address = 8
			DEVICE_DEBUG_ADDR_8					(null, RegisterPacket.parseValueFunction),
			DEVICE_DEBUG_POTENTIOMETER_N1		(null, RegisterPacket.parseValueFunction),	//PACKET_ID_DEVICE_DEBUG_ADDR_8 + index
			DEVICE_DEBUG_POTENTIOMETER_N6		(null, RegisterPacket.parseValueFunction),
			DEVICE_DEBUG_POTENTIOMETER_N3		(null, RegisterPacket.parseValueFunction),

			//address = 11
			DEVICE_DEBUG_ADDR_11				(null, RegisterPacket.parseValueFunction),
			DEVICE_DEBUG_POTENTIOMETER_N1_SET	(null, RegisterPacket.parseValueFunction),	//PACKET_ID_DEVICE_DEBUG_ADDR_11 + index
			DEVICE_DEBUG_POTENTIOMETER_N3_SET	(null, RegisterPacket.parseValueFunction),
			DEVICE_DEBUG_POTENTIOMETER_N6_SET	(null, RegisterPacket.parseValueFunction),

			//address = 100
			DEVICE_DEBUG_ADDR_100(null, RegisterPacket.parseValueFunction),
			DEVICE_CONVERTER_DAC1(null, RegisterPacket.parseValueFunction),
			DEVICE_CONVERTER_DAC2(null, RegisterPacket.parseValueFunction),
			DEVICE_CONVERTER_DAC3(null, RegisterPacket.parseValueFunction),
			DEVICE_CONVERTER_DAC4(null, RegisterPacket.parseValueFunction),

			DUMPS(null, null),
			DUMP_DEVICE_DEBUG_INFO_0(null, DeviceDebugInfoPacket.parseValueFunction),	//Index 0.  Board info, like: CPU frequency, CPU reset type, CPU clock source, Local temperature
			DUMP_DEVICE_DEBUG_INFO_1(null, DeviceDebugInfoPacket.parseValueFunction),	//Index 1.  Board error info
			DUMP_DEVICE_DEBUG_INFO_2(null, DeviceDebugInfoPacket.parseValueFunction),	//Index 2.  Threshold info, like:  Zero current, Over-current HSS1, Over-current HSS2, Over-temperature mute, Over-temperature unmute
			DUMP_DEVICE_DEBUG_INFO_3(null, DeviceDebugInfoPacket.parseValueFunction),	//Index 3.  I2C error statistics
			DUMP_DEVICE_DEBUG_INFO_4(null, DeviceDebugInfoPacket.parseValueFunction),	//Index 4.  Mute state machine flags in format: Flag_name: Current_flag (History_flag)
			DUMP_DEVICE_DEBUG_INFO_10(null, DeviceDebugInfoPacket.parseValueFunction),	//Index 10.  FCM info
			DUMP_DEVICE_DEBUG_INFO_11(null, DeviceDebugInfoPacket.parseValueFunction),	//Index 11. 
			DUMP_POWER(null, null),

			DUMP_REGISTER_1		(null, DeviceDebugInfoPacket.parseValueFunction),
			DUMP_REGISTER_2		(null, DeviceDebugInfoPacket.parseValueFunction),
			DUMP_REGISTER_3		(null, DeviceDebugInfoPacket.parseValueFunction),
			DUMP_REGISTER_4		(null, DeviceDebugInfoPacket.parseValueFunction),
			DUMP_REGISTER_5		(null, DeviceDebugInfoPacket.parseValueFunction),
			DUMP_REGISTER_6		(null, DeviceDebugInfoPacket.parseValueFunction),
			DUMP_REGISTER_7		(null, DeviceDebugInfoPacket.parseValueFunction),
			DUMP_REGISTER_10	(null, DeviceDebugInfoPacket.parseValueFunction),
			DUMP_REGISTER_100	(null, DeviceDebugInfoPacket.parseValueFunction),
			DUMP_REGISTER_201	(null, DeviceDebugInfoPacket.parseValueFunction),
			DUMP_REGISTER_202	(null, DeviceDebugInfoPacket.parseValueFunction),
			DUMP_REGISTER_207	(null, DeviceDebugInfoPacket.parseValueFunction),
			DUMP_REGISTER_220	(null, DeviceDebugInfoPacket.parseValueFunction),

			CLEAR_STATISTICS	(null, null),
			PROTO_RETRANSNIT	(null, RetransmitPacket.parseValueFunction);

		private final static Logger logger = LogManager.getLogger();

		private final Function<Packet, Optional<Object>> function;
		private final String text;

		private PacketIDs(String text, Function<Packet, Optional<Object>> function){
			this.function = function;
			this.text = text;
		}

		public short getId() {
			return (short) ordinal();
		}

		public boolean match(short id){
			return ((short)ordinal())==id;
		}

		public  Optional<Object> valueOf(Packet packet) {
			logger.trace(packet);
			try {
				return Optional.ofNullable(function).flatMap(f->f.apply(packet));
			}catch (Exception e) {
				logger.catching(e);
			}
			return Optional.empty();
		}

		public String toString(){
			return Optional.ofNullable(text).map(t->name() + "-" + text ).orElse(name() + "(" + ordinal() + ")");
		}

		public static String toString(short id){
			int intID = id&0xFF;
			final PacketIDs[] values = values();

			if(intID<values.length){
				final PacketIDs v = values[intID];
				return v.name() + "(" + v.ordinal() + ")";
			}else
				return  Integer.toString(intID);
		}

		public static Optional<PacketIDs> valueOf(short packetId) {
			final Optional<PacketIDs> optional = Arrays.stream(values()).parallel().filter(id->id.ordinal()==packetId).findAny();

			if(!optional.isPresent())
				logger.warn(packetId);

			return optional;
		}
	}

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
		DESCRIPTION_TODO9					((byte)20, PacketIDs.ALARMS_DESCRIPTION_TODO9					, true);

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

			if(!oAlarmsPacketIds.isPresent())
				logger.warn(alarmId);

			return oAlarmsPacketIds;
		}
	}

	public enum DeviceDebugPacketIds{
		
		ADDR_0						(0		, 0		, PacketIDs.DEVICE_DEBUG_ADDR_0					, "DEVICE_DEBUG_ADDR_0"					, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		ADDR_1						(0		, 0		, PacketIDs.DEVICE_DEBUG_ADDR_1					, "DEVICE_DEBUG_ADDR_1"					, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		ADDR_2						(0		, 0		, PacketIDs.DEVICE_DEBUG_ADDR_2					, "DEVICE_DEBUG_ADDR_2"					, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		ADDR_3						(0		, 0		, PacketIDs.DEVICE_DEBUG_ADDR_3					, "DEVICE_DEBUG_ADDR_3"					, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		ADDR_4						(0		, 0		, PacketIDs.DEVICE_DEBUG_ADDR_4					, "DEVICE_DEBUG_ADDR_4"					, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		ADDR_8						(0		, 0		, PacketIDs.DEVICE_DEBUG_ADDR_8					, "DEVICE_DEBUG_ADDR_8"					, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		ADDR_11						(0		, 0		, PacketIDs.DEVICE_DEBUG_ADDR_11				, "DEVICE_DEBUG_ADDR_11"				, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		ADDR_100					(0		, 0		, PacketIDs.DEVICE_DEBUG_ADDR_100				, "DEVICE_DEBUG_ADDR_100"				, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		CALIBRATION_MODE			(0		, 0		, PacketIDs.DEVICE_DEBUG_CALIBRATION_MODE		, "DEVICE_DEBUG_CALIBRATION_MODE"		, PacketImp.PARAMETER_DEVICE_DEBUG_CALIBRATION_MODE),

		INFO						(null	, null	, PacketIDs.DEVICE_DEBUG_INFO					, "DEVICE_DEBUG_DEVICE_INFO"			, PacketImp.PARAMETER_DEVICE_DEBUG_INFO),
		CPU_INFO					(null	, 0		, PacketIDs.DEVICE_DEBUG_CPU_INFO				, "DEVICE_DEBUG_CPU_INFO"				, PacketImp.PARAMETER_DEVICE_DEBUG_INFO),
		HARDWARE_FAULTS_INFO		(null	, 1		, PacketIDs.DEVICE_DEBUG_HARDWARE_FAULTS_INFO	, "DEVICE_DEBUG_HARDWARE_FAULTS_INFO"	, PacketImp.PARAMETER_DEVICE_DEBUG_INFO),
		THRESHOLDS_INFO				(null	, 2		, PacketIDs.DEVICE_DEBUG_THRESHOLDS_INFO		, "DEVICE_DEBUG_THRESHOLDS_INFO"		, PacketImp.PARAMETER_DEVICE_DEBUG_INFO),
		I2C1_INFO					(null	, 3		, PacketIDs.DEVICE_DEBUG_I2C1_INFO				, "DEVICE_DEBUG_I2C1_INFO"				, PacketImp.PARAMETER_DEVICE_DEBUG_INFO),
		MUTE_INFO					(null	, 4		, PacketIDs.DEVICE_DEBUG_MUTE_INFO				, "DEVICE_DEBUG_MUTE_INFO"				, PacketImp.PARAMETER_DEVICE_DEBUG_INFO),
		SCP_DEVICE_INFO				(null	, 10	, PacketIDs.DEVICE_DEBUG_SCP_DEVICE_INFO		, "DEVICE_DEBUG_SCP_DEVICE_INFO"		, PacketImp.PARAMETER_DEVICE_DEBUG_INFO),

//		INFO_0					(0, 0, PacketIDs.DUMP_DEVICE_DEBUG_INFO_0				, "DUMP_DEVICE_DEBUG_INFO_0"			, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
//		INFO_1					(0, 1, PacketIDs.DUMP_DEVICE_DEBUG_INFO_1				, "DUMP_DEVICE_DEBUG_INFO_1"			, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
//		INFO_2					(0, 2, PacketIDs.DUMP_DEVICE_DEBUG_INFO_2				, "DUMP_DEVICE_DEBUG_INFO_2"			, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
//		INFO_3					(0, 3, PacketIDs.DUMP_DEVICE_DEBUG_INFO_3				, "DUMP_DEVICE_DEBUG_INFO_3"			, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
//		INFO_4					(0, 4, PacketIDs.DUMP_DEVICE_DEBUG_INFO_4				, "DUMP_DEVICE_DEBUG_INFO_4"			, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
//		INFO_10					(0, 10, PacketIDs.DUMP_DEVICE_DEBUG_INFO_10				, "DUMP_DEVICE_DEBUG_INFO_10"			, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
//		INFO_11					(0, 11, PacketIDs.DUMP_DEVICE_DEBUG_INFO_11				, "DUMP_DEVICE_DEBUG_INFO_11"			, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),

		DEVICEs_DUMP				(null	, 100	, PacketIDs.DEVICE_DEBUG_DEVICEs_DUMP			, "DEVICE_DEBUG_DEVICEs_DUMP"			, PacketImp.PARAMETER_DEVICE_DEBUG_DUMP),//TODO
		DEVICE1_DUMP				(null	, 1		, PacketIDs.DEVICE_DEBUG_DEVICE1_DUMP			, "DEVICE_DEBUG_DEVICE1_DUMP"			, PacketImp.PARAMETER_DEVICE_DEBUG_DUMP),
		DEVICE2_DUMP				(null	, 2		, PacketIDs.DEVICE_DEBUG_DEVICE2_DUMP			, "DEVICE_DEBUG_DEVICE2_DUMP"			, PacketImp.PARAMETER_DEVICE_DEBUG_DUMP),
		DEVICE3_DUMP				(null	, 7		, PacketIDs.DEVICE_DEBUG_DEVICE3_DUMP			, "DEVICE_DEBUG_DEVICE3_DUMP"			, PacketImp.PARAMETER_DEVICE_DEBUG_DUMP),
		HSS1_DUMP					(null	, 3		, PacketIDs.DEVICE_DEBUG_HSS1_DUMP				, "DEVICE_DEBUG_HSS1_DUMP"				, PacketImp.PARAMETER_DEVICE_DEBUG_DUMP),
		HSS2_DUMP					(null	, 4		, PacketIDs.DEVICE_DEBUG_HSS2_DUMP				, "DEVICE_DEBUG_HSS2_DUMP"				, PacketImp.PARAMETER_DEVICE_DEBUG_DUMP),
		VOLTAGES_DUMP				(null	, 5		, PacketIDs.DEVICE_DEBUG_VOLTAGES_DUMP			, "DEVICE_DEBUG_VOLTAGES_DUMP"			, PacketImp.PARAMETER_DEVICE_DEBUG_DUMP),
		PLL2_DUMP					(null	, 6		, PacketIDs.DEVICE_DEBUG_PLL2_DUMP				, "DEVICE_DEBUG_PLL2_DUMP"				, PacketImp.PARAMETER_DEVICE_DEBUG_DUMP),
		ADC_CHANNELS_DUMP			(null	, 10	, PacketIDs.DEVICE_DEBUG_ADC_CHANNELS_DUMP		, "DEVICE_DEBUG_ADC_CHANNELS_DUMP"		, PacketImp.PARAMETER_DEVICE_DEBUG_DUMP),
		ADC_CHANNELS_mV_DUMP		(null	, 11	, PacketIDs.DEVICE_DEBUG_ADC_CHANNELS_mV_DUMP	, "DEVICE_DEBUG_ADC_CHANNELS_mV_DUMP"	, PacketImp.PARAMETER_DEVICE_DEBUG_DUMP),
		SIGNALS_CHANGE_COUNTERS_DUMP(null	, 20	, PacketIDs.DEVICE_DEBUG_CHANGE_COUNTERS_DUMP	, "DEVICE_DEBUG_CHANGE_COUNTERS_DUMP"	, PacketImp.PARAMETER_DEVICE_DEBUG_DUMP),
		DP1_DUMP					(null	, 30	, PacketIDs.DEVICE_DEBUG_DP1_DUMP				, "DEVICE_DEBUG_DP1_DUMP"				, PacketImp.PARAMETER_DEVICE_DEBUG_DUMP),

		HELP						(0		, 0		, PacketIDs.DEVICE_DEBUG_HELP					, "DEVICE_DEBUG_HELP"					, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),

		HS1_CURRENT					(1		, 5		, PacketIDs.DEVICE_DEBUG_HS1_CURRENT			, "DEVICE_DEBUG_HS1_CURRENT"			, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		HS1_CURRENT_REMOTE_BIAS		(1		, 205	, PacketIDs.DEVICE_DEBUG_HS1_CURRENT_REMOTE_BIAS, "DEVICE_DEBUG_HS1_CURRENT_REMOTE_BIAS", PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		HS1_CURRENT_HP_BIAS			(1		, 20	, PacketIDs.DEVICE_DEBUG_HS1_CURRENT_HP_BIAS	, "DEVICE_DEBUG_HS1_CURRENT"			, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
//		HS1_CURRENT_HP_BIAS_REMOTE	(0		, 0		, PacketIDs.DEVICE_DEBUG_HS1_CURRENT			, "DEVICE_DEBUG_HS1_CURRENT"			, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		HS2_CURRENT					(2		, 5		, PacketIDs.DEVICE_DEBUG_HS2_CURRENT			, "DEVICE_DEBUG_HS2_CURRENT"			, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		HS2_CURRENT_REMOTE_BIAS		(2		, 205	, PacketIDs.DEVICE_DEBUG_HS2_CURRENT_REMOTE_BIAS, "DEVICE_DEBUG_HS2_CURRENT_REMOTE_BIAS", PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		HS2_CURRENT_HP_BIAS			(2		, 20	, PacketIDs.DEVICE_DEBUG_HS1_CURRENT_HP_BIAS	, "DEVICE_DEBUG_HS1_CURRENT_HP_BIAS"	, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),

		OUTPUT_POWER				(3		, 5		, PacketIDs.DEVICE_DEBUG_OUTPUT_POWER			, "DEVICE_DEBUG_OUTPUT_POWER"			, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		OUTPUT_POWER_REMOTE_BIAS	(3		, 205	, PacketIDs.DEVICE_DEBUG_OUTPUT_POWER_REMOTE_BIAS, "DEVICE_DEBUG_OUTPUT_POWER_REMOTE_BIAS", PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		OUTPUT_POWER_HP_BIAS		(0		, 20	, PacketIDs.DEVICE_DEBUG_OUTPUT_POWER_HP_BIAS	, "DEVICE_DEBUG_OUTPUT_POWER_HP_BIAS"	, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),

		TEMPERATURE					(4		, 5		, PacketIDs.DEVICE_DEBUG_TEMPERATURE			, "DEVICE_DEBUG_TEMPERATURE"			, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		TEMPERATURE_REMOTE_BIAS		(4		, 205	, PacketIDs.DEVICE_DEBUG_TEMPERATURE_REMOTE_BIAS, "DEVICE_DEBUG_TEMPERATURE_REMOTE_BIAS", PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		TEMPERATURE_HP_BIAS			(4		, 20	, PacketIDs.DEVICE_DEBUG_TEMPERATURE_HP_BIAS	, "DEVICE_DEBUG_TEMPERATURE_HP_BIAS"	, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),

		NGLOBAL						(0		, 6		, PacketIDs.DEVICE_DEBUG_NGLOBAL				, "DEVICE_DEBUG_NGLOBAL"				, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		NGLOBAL_REMOTE_BIAS			(0		, 206	, PacketIDs.DEVICE_DEBUG_NGLOBAL_REMOTE_BIAS	, "DEVICE_DEBUG_NGLOBAL_REMOTE_BIAS"	, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),

		POTENTIOMETER_N1		(0		, 0		, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N1		, "DEVICE_DEBUG_POTENTIOMETER_N1"		, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		POTENTIOMETER_N2		(0		, 0		, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N2		, "DEVICE_DEBUG_POTENTIOMETER_N2"		, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		POTENTIOMETER_N3		(0		, 0		, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N3		, "DEVICE_DEBUG_POTENTIOMETER_N3"		, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		POTENTIOMETER_N4		(0		, 0		, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N4		, "DEVICE_DEBUG_POTENTIOMETER_N4"		, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		POTENTIOMETER_N5		(0		, 0		, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N5		, "DEVICE_DEBUG_POTENTIOMETER_N5"		, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		POTENTIOMETER_N6		(0		, 0		, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N6		, "DEVICE_DEBUG_POTENTIOMETER_N6"		, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		POTENTIOMETER_N1_SET	(0		, 0		, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N1_SET	, "DEVICE_DEBUG_POTENTIOMETER_N1_SET"	, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		POTENTIOMETER_N2_SET	(0		, 0		, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N2_SET	, "DEVICE_DEBUG_POTENTIOMETER_N2_SET"	, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		POTENTIOMETER_N3_SET	(0		, 0		, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N3_SET	, "DEVICE_DEBUG_POTENTIOMETER_N3_SET"	, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		POTENTIOMETER_N4_SET	(0		, 0		, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N4_SET	, "DEVICE_DEBUG_POTENTIOMETER_N4_SET"	, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		POTENTIOMETER_N5_SET	(0		, 0		, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N5_SET	, "DEVICE_DEBUG_POTENTIOMETER_N5_SET"	, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		POTENTIOMETER_N6_SET	(0		, 0		, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N6_SET	, "DEVICE_DEBUG_POTENTIOMETER_N6_SET"	, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		SWITCH_N1				(1		, 3		, PacketIDs.DEVICE_DEBUG_SWITCH_N1				, "DEVICE_DEBUG_SWITCH_N1"				, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		SWITCH_N1_REMOTE_BIAS	(1		, 203	, PacketIDs.DEVICE_DEBUG_SWITCH_N1_REMOTE_BIAS	, "DEVICE_DEBUG_SWITCH_N1_REMOTE_BIAS"	, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		SWITCH_N2				(1		, 4		, PacketIDs.DEVICE_DEBUG_SWITCH_N2				, "DEVICE_DEBUG_SWITCH_N2"				, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		SWITCH_N2_REMOTE_BIAS	(1		, 204	, PacketIDs.DEVICE_DEBUG_SWITCH_N2_REMOTE_BIAS	, "DEVICE_DEBUG_SWITCH_N2_REMOTE_BIAS"	, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		CONVERTER_PLL_1			(0, 0, PacketIDs.DEVICE_DEBUG_CONVERTER_PLL_1			, "DEVICE_DEBUG_CONVERTER_PLL_1"		, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		CONVERTER_PLL_2			(0, 0, PacketIDs.DEVICE_DEBUG_CONVERTER_PLL_2			, "DEVICE_DEBUG_CONVERTER_PLL_2"		, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),

		PLL_REG					(0, 0, PacketIDs.FCM_DEVICE_DEBUG_PLL_REG				, "FCM_DEVICE_DEBUG_PLL_REG"			, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		PLL_REG_DOWN_GAIN		(0, 0, PacketIDs.FCM_DEVICE_DEBUG_PLL_REG_DOWN_GAIN	, "FCM_DEVICE_DEBUG_PLL_REG_DOWN_GAIN"	, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		PLL_REG_UP_GAIN			(0, 0, PacketIDs.FCM_DEVICE_DEBUG_PLL_REG_UP_GAIN		, "FCM_DEVICE_DEBUG_PLL_REG_UP_GAIN"	, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		PLL_REG_OFFSET			(0, 0, PacketIDs.FCM_DEVICE_DEBUG_PLL_REG_OFFSET		, "FCM_DEVICE_DEBUG_PLL_REG_OFFSET"		, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),

		FCM_ADC_INPUT_POWER		(0, 10	, PacketIDs.FCM_ADC_INPUT_POWER					, "FCM_ADC_INPUT_POWER"					, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		FCM_ADC_OUTPUT_POWER	(1, 10	, PacketIDs.FCM_ADC_OUTPUT_POWER				, "FCM_ADC_OUTPUT_POWER"				, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		FCM_ADC_TEMPERATURE		(2, 10	, PacketIDs.FCM_ADC_TEMPERATURE					, "FCM_ADC_TEMPERATURE"					, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		FCM_ADC_CURRENT			(4, 10	, PacketIDs.FCM_ADC_CURRENT						, "FCM_ADC_CURRENT"						, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		FCM_ADC_5V5				(6, 10	, PacketIDs.FCM_ADC_5V5							, "FCM_ADC_5V5"							, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		FCM_ADC_13v2			(7, 10	, PacketIDs.FCM_ADC_13v2						, "FCM_ADC_13v2"						, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		FCM_ADC_13V2_NEG		(8, 10	, PacketIDs.FCM_ADC_13V2_NEG					, "FCM_ADC_13V2_NEG"					, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),

		DAC1					(0, 1	, PacketIDs.DEVICE_CONVERTER_DAC1				, "DEVICE_CONVERTER_DAC1"								, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),	// PACKET_ID_DEVICE_FCM_INDEX_1 + address
		DAC2					(0, 2	, PacketIDs.DEVICE_CONVERTER_DAC2				, "DEVICE_CONVERTER_DAC2"								, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		DAC3					(0, 3	, PacketIDs.DEVICE_CONVERTER_DAC3				, "DEVICE_CONVERTER_DAC3"								, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		DAC4					(0, 4	, PacketIDs.DEVICE_CONVERTER_DAC4				, "DEVICE_CONVERTER_DAC4"								, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),

		FCM_DAC1				(0, 1	, PacketIDs.DEVICE_CONVERTER_DAC1_FCM			, "DEVICE_CONVERTER_DAC1_FCM"							, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),	// PACKET_ID_DEVICE_FCM_INDEX_1 + address
		FCM_DAC2				(0, 2	, PacketIDs.DEVICE_CONVERTER_DAC2_FCM			, "DEVICE_CONVERTER_DAC2_FCM"							, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		FCM_DAC3				(0, 3	, PacketIDs.DEVICE_CONVERTER_DAC3_FCM			, "DEVICE_CONVERTER_DAC3_FCM"							, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE),
		FCM_DAC4				(0, 4	, PacketIDs.DEVICE_CONVERTER_DAC4_FCM			, "DEVICE_CONVERTER_DAC4_FCM"							, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE);

		private static final int INDEX = 2;

		private static final int ADDR = 1;

		private final static Logger logger = LogManager.getLogger();
		private final PacketIDs packetId;
		private final String text;
		private final Integer index;
		private final Integer addr;
		private byte parameterCode;

		private DeviceDebugPacketIds(Integer addr, Integer index, PacketIDs packetId, String text, byte parameterCode){

			this.index = index;
			this.addr = addr;
			this.packetId = packetId;
			this.text = text;
			this.parameterCode = parameterCode;
		}

		public byte[] getPayloadData() {
			int caseIndex = 0;

			if(addr!=null)
				caseIndex += ADDR;

			if(index!=null)
				caseIndex += INDEX;

			switch(caseIndex){

			case ADDR:
				return ByteBuffer.allocate(4).putInt(addr).array();

			case INDEX:
				return ByteBuffer.allocate(4).putInt(index).array();

			case ADDR + INDEX:
				return ByteBuffer.allocate(8).putInt(index).putInt(addr).array();
			}
			return null;
		}

		public PacketIDs getPacketId() {
			return packetId;
		}

		public byte getParameterCode() {
			return parameterCode;
		}

		public static Optional<DeviceDebugPacketIds> valueOf(short id){
			final Optional<DeviceDebugPacketIds> oAlarmsPacketIds = Arrays.stream(values()).parallel().filter(a->a.packetId.getId()==id).findAny();

			if(!oAlarmsPacketIds.isPresent())
				logger.warn(id);

			return oAlarmsPacketIds;
		}

		public static Optional<DeviceDebugPacketIds> valueOf(DeviceDebugType deviceDebugType, int index) {
			return Arrays.stream(values()).filter(pId->pId.parameterCode==deviceDebugType.getParameterCode()).filter(pId->pId.index!=null).filter(pId->pId.index==index).findAny();
		}

		public static Optional<DeviceDebugPacketIds> valueOf(PacketIDs packetId) {
			return Arrays.stream(values()).filter(pId->pId.packetId.equals(packetId)).findAny();
		}

		public Integer getIndex() {
			return index;
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
