package irt.data.packet;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.packet.alarm.AlarmDescriptionPacket;
import irt.data.packet.alarm.AlarmStatusPacket;
import irt.data.packet.alarm.AlarmsIDsPacket;
import irt.data.packet.alarm.AlarmsSummaryPacket;
import irt.data.packet.configuration.ALCEnablePacket;
import irt.data.packet.configuration.AttenuationPacket;
import irt.data.packet.configuration.AttenuationRangePacket;
import irt.data.packet.configuration.ConfigurationPacket;
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
import irt.data.packet.configuration.LnbStatusPacket;
import irt.data.packet.control.ActiveModulePacket;
import irt.data.packet.control.ModuleListPacket;
import irt.data.packet.denice_debag.CallibrationModePacket;
import irt.data.packet.denice_debag.DeviceDebugInfoPacket;
import irt.data.packet.denice_debag.RegisterPacket;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.interfaces.PacketThreadWorker;
import irt.data.packet.measurement.MeasurementPacket;
import irt.data.packet.network.NetworkAddressPacket;
import irt.data.packet.redundancy.RedundancyControllerStatusPacket;
import irt.data.packet.redundancy.SwitchoverModePacket;

public enum PacketID {

	UNNECESSARY	( null, null, null, null, null, null),

	DEVICE_INFO	( null, null, null, DeviceInfoPacket.parseValueFunction, 1000L, 2000),

	ALARMS_ALL_IDs					( null, null,  null, AlarmsIDsPacket.parseValueFunction, null, 50),
	ALARMS_SUMMARY					( null, null, null, AlarmsSummaryPacket.parseValueFunction, null, null),
	ALARMS_OWER_CURRENT				( null, null, null, AlarmStatusPacket.parseValueFunction, null, 6),
	ALARMS_UNDER_CURRENT			( null, null, null, AlarmStatusPacket.parseValueFunction, null, 6),
	ALARMS_OWER_TEMPERATURE			( null, null, null, AlarmStatusPacket.parseValueFunction, null, 6),
	ALARMS_PLL_OUT_OF_LOCK			( null, null, null, AlarmStatusPacket.parseValueFunction, null, 6),
	ALARMS_HARDWARE_FAULT			( null, null, null, AlarmStatusPacket.parseValueFunction, null, 6),
	ALARMS_REDUNDANT_FAULT			( null, null, null, AlarmStatusPacket.parseValueFunction, null, 6),
	ALARMS_test						( null, null, null, AlarmStatusPacket.parseValueFunction, null, 6),
	ALARMS_NO_INPUT_SIGNAL			( null, null, null, AlarmStatusPacket.parseValueFunction, null, 6),
	ALARMS_RF_OVERDRIVEL			( null, null, null, AlarmStatusPacket.parseValueFunction, null, 6),
	ALARMS_STATUS					( null, null, null, AlarmStatusPacket.parseValueFunction, null, 6),
	ALARMS_TEMPERATURE_ThRESHOLD_1	( null, null, null, AlarmStatusPacket.parseValueFunction, null, 6),
	ALARMS_TEMPERATURE_ThRESHOLD_2	( null, null, null, AlarmStatusPacket.parseValueFunction, null, 6),
	ALARMS_ALC_ERROR				( null, null, null, AlarmStatusPacket.parseValueFunction, null, 6),
	ALARMS_BUC_B_COMMUNICATION_FAULT( null, null, null, AlarmStatusPacket.parseValueFunction, null, 6),
	ALARMS_BUC_B_SUMMARY			( null, null, null, AlarmStatusPacket.parseValueFunction, null, 6),
	ALARMS_BUC_S_COMMUNICATION_FAULT( null, null, null, AlarmStatusPacket.parseValueFunction, null, 6),
	ALARMS_TODO4					( null, null, null, AlarmStatusPacket.parseValueFunction, null, 6),
	ALARMS_TODO5					( null, null, null, AlarmStatusPacket.parseValueFunction, null, 6),
	ALARMS_BUC_S_SUMMARY			( null, null, null, AlarmStatusPacket.parseValueFunction, null, 6),
	ALARMS_TODO7					( null, null, null, AlarmStatusPacket.parseValueFunction, null, 6),
	ALARMS_TODO8					( null, null, null, AlarmStatusPacket.parseValueFunction, null, 6),
	ALARMS_TODO9					( null, null, null, AlarmStatusPacket.parseValueFunction, null, 6),
	ALARMS_TODO10					( null, null, null, AlarmStatusPacket.parseValueFunction, null, 6),
	ALARMS_LNB1_UNDER_CURRENT		( null, null, null, AlarmDescriptionPacket.parseValueFunction, null, 6),
	ALARMS_LNB2_UNDER_CURRENT		( null, null, null, AlarmDescriptionPacket.parseValueFunction, null, 6),
	ALARMS_LNBS_UNDER_CURRENT		( null, null, null, AlarmDescriptionPacket.parseValueFunction, null, 6),
	ALARMS_PSU1						( null, null, null, AlarmDescriptionPacket.parseValueFunction, null, 6),
	ALARMS_PSU2						( null, null, null, AlarmDescriptionPacket.parseValueFunction, null, 6),

	ALARMS_DESCRIPTION_OWER_CURRENT				( null, null, null, AlarmDescriptionPacket.parseValueFunction, null, 30),
	ALARMS_DESCRIPTION_UNDER_CURRENT			( null, null, null, AlarmDescriptionPacket.parseValueFunction, null, 30),
	ALARMS_DESCRIPTION_OWER_TEMPERATURE			( null, null, null, AlarmDescriptionPacket.parseValueFunction, null, 30),
	ALARMS_DESCRIPTION_PLL_OUT_OF_LOCK			( null, null, null, AlarmDescriptionPacket.parseValueFunction, null, 30),
	ALARMS_DESCRIPTION_HARDWARE_FAULT			( null, null, null, AlarmDescriptionPacket.parseValueFunction, null, 30),
	ALARMS_DESCRIPTION_REDUNDANT_FAULT			( null, null, null, AlarmDescriptionPacket.parseValueFunction, null, 30),
	ALARMS_DESCRIPTION_test						( null, null, null, AlarmDescriptionPacket.parseValueFunction, null, 30),
	ALARMS_DESCRIPTION_NO_INPUT_SIGNAL			( null, null, null, AlarmDescriptionPacket.parseValueFunction, null, 30),
	ALARMS_DESCRIPTION_RF_OVERDRIVEL			( null, null, null, AlarmDescriptionPacket.parseValueFunction, null, 30),
	ALARMS_DESCRIPTION_STATUS					( null, null, null, AlarmDescriptionPacket.parseValueFunction, null, 30),
	ALARMS_DESCRIPTION_TEMPERATURE_ThRESHOLD_1	( null, null, null, AlarmDescriptionPacket.parseValueFunction, null, 30),
	ALARMS_DESCRIPTION_TEMPERATURE_ThRESHOLD_2	( null, null, null, AlarmDescriptionPacket.parseValueFunction, null, 30),
	ALARMS_DESCRIPTION_ALC_ERROR				( null, null, null, AlarmDescriptionPacket.parseValueFunction, null, 30),
	ALARMS_DESCRIPTION_ALL_IDs					( null, null, null, AlarmDescriptionPacket.parseValueFunction, null, 30),
	ALARMS_DESCRIPTION_BUC_B_COMMUNICATION_FAULT( null, null, null, AlarmDescriptionPacket.parseValueFunction, null, 30),
	ALARMS_DESCRIPTION_BUC_B_SUMMARY			( null, null, null, AlarmDescriptionPacket.parseValueFunction, null, 30),
	ALARMS_DESCRIPTION_BUC_S_COMMUNICATION_FAULT( null, null, null, AlarmDescriptionPacket.parseValueFunction, null, 30),
	ALARMS_DESCRIPTION_TODO4					( null, null, null, AlarmDescriptionPacket.parseValueFunction, null, 30),
	ALARMS_DESCRIPTION_TODO5					( null, null, null, AlarmDescriptionPacket.parseValueFunction, null, 30),
	ALARMS_DESCRIPTION_BUC_S_SUMMARY			( null, null, null, AlarmDescriptionPacket.parseValueFunction, null, 30),
	ALARMS_DESCRIPTION_TODO7					( null, null, null, AlarmDescriptionPacket.parseValueFunction, null, 30),
	ALARMS_DESCRIPTION_TODO8					( null, null, null, AlarmDescriptionPacket.parseValueFunction, null, 30),
	ALARMS_DESCRIPTION_TODO9					( null, null, null, AlarmDescriptionPacket.parseValueFunction, null, 30),
	ALARMS_DESCRIPTION_TODO10					( null, null, null, AlarmDescriptionPacket.parseValueFunction, null, 30),
	ALARMS_DESCRIPTION_LNB1_UNDER_CURRENT		( null, null, null, AlarmDescriptionPacket.parseValueFunction, null, 30),
	ALARMS_DESCRIPTION_LNB2_UNDER_CURRENT		( null, null, null, AlarmDescriptionPacket.parseValueFunction, null, 30),
	ALARMS_DESCRIPTION_LNBS_UNDER_CURRENT		( null, null, null, AlarmDescriptionPacket.parseValueFunction, null, 30),
	ALARMS_DESCRIPTION_PSU1						( null, null, null, AlarmDescriptionPacket.parseValueFunction, null, 30),
	ALARMS_DESCRIPTION_PSU2						( null, null, null, AlarmDescriptionPacket.parseValueFunction, null, 30),

	MEASUREMENT_ALL						( null, null, null, MeasurementPacket.parseValueFunction, null, null),
	MEASUREMENT_STATUS					( null, null, null, null, null, null),
	MEASUREMENT_INPUT_POWER				( null, null, null, null, null, null),
	MEASUREMENT_BAIAS_25W_OUTPUT_POWER	( null, null, null, null, null, null),
	MEASUREMENT_TEMPERATURE				( null, null, null, null, null, null),
	MEASUREMENT_UNIT_TEMPERATURE		( null, null, null, null, null, null),
	MEASUREMENT_CPU_TEMPERATURE			( null, null, null, null, null, null),
	MEASUREMENT_5V5						( null, null, null, null, null, null),
	MEASUREMENT_13V2					( null, null, null, null, null, null),
	MEASUREMENT_13V2_NEG				( null, null, null, null, null, null),
	MEASUREMENT_OUTPUT_POWER			( null, null, null, null, null, null),
	MEASUREMENT_SNB1_STATUS				( null, null, null, null, null, null),
	MEASUREMENT_SNB2_STATUS				( null, null, null, null, null, null),
	MEASUREMENT_WGS_POSITION			( null, null, null, null, null, null),

	CONFIGURATION_LO					( null, null, null, LOPacket.parseValueFunction, null, 8),
	CONFIGURATION_LO_FREQUENCIES		( null, null, null, LOFrequenciesPacket.parseValueFunction, null, 80),
	CONFIGURATION_MUTE					( null, null, null, MuteControlPacket.parseValueFunction, null, null),
	CONFIGURATION_GAIN					( null, null, null, AttenuationPacket.parseValueFunction, null, null),
	CONFIGURATION_GAIN_RANGE			( null, null, null, AttenuationRangePacket.parseValueFunction, null, null),
	CONFIGURATION_ATTENUATION			( null, null, null, AttenuationPacket.parseValueFunction, null, null),
	CONFIGURATION_ATTENUATION_RANGE		( null, null, null, AttenuationRangePacket.parseValueFunction, null, null),
	CONFIGURATION_FREQUENCY				( null, null, null, FrequencyPacket.parseValueFunction, null, 8),
	CONFIGURATION_FREQUENCY_RANGE		( null, null, null, AttenuationRangePacket.parseValueFunction, null, 16),
	STORE_CONFIG						( null, null, null, null, null, null),
	CONFIGURATION_FCM_LNB_POWER			( null, null, null, LnbPowerPacket.parseValueFunction, null, null),
	CONFIGURATION_GAIN_OFFSET			( null, null, null, ConfigurationPacket.parseValueFunction, null, null),
	CONFIGURATION_FCM_FLAGS				( null, null, null, null, null, null),
	CONFIGURATION_REDUNDANCY_ENABLE		( null, null, null, RedundancyEnablePacket	.parseValueFunction, null, null),
	CONFIGURATION_REDUNDANCY_MODE		( null, null, null, RedundancyModePacket		.parseValueFunction, null, null),
	CONFIGURATION_REDUNDANCY_NAME		( null, null, null, RedundancyNamePacket		.parseValueFunction, null, null),
	CONFIGURATION_REDUNDANCY_STATUS		( null, null, null, RedundancyStatusPacket	.parseValueFunction, null, null),
	CONFIGURATION_REDUNDANCY_SET_ONLINE	( null, null, null, null, null, null),
	CONFIGURATION_MUTE_OUTDOOR			( null, null, null, null, null, null),
	CONFIGURATION_ALC_ENABLE			( null, null, null, ALCEnablePacket			.parseValueFunction, null, null),
	CONFIGURATION_ALC_LEVEL				( null, null, null, null, null, null),
	CONFIGURATION_ALC_RANGE				( null, null, null, AttenuationRangePacket	.parseValueFunction, null, null),
	CONFIGURATION_DLRS_WGS_SWITCHOVER	( null, null, null, LnbSwitchPacket			.parseValueFunction, null, null),
	CONFIGURATION_SET_DLRS_WGS_SWITCHOVER( null, null, null, null, null, null),
	CONFIGURATION_SPECTRUM_INVERSION	( null, null, null, SpectrumInversionPacket	.parseValueFunction, null, null),
	CONFIGURATION_REFERENCE_CONTROL		( null, null, null, null, null, null),
	CONFIGURATION_FCM_LNB_REFERENCE		( null, null, null, LnbReferencePacket		.parseValueFunction, null, null),
	CONFIGURATION_OFFSET_RANGE			( null, null, null, AttenuationRangePacket	.parseValueFunction, null, null),
	CONFIGURATION_OFFSET_1_TO_MULTI		( null, null, null, Offset1to1toMultiPacket	.parseValueFunction, null, null),
	CONFIGURATION_LNB_LO_SELECT			( PacketGroupIDs.CONFIGURATION, PacketImp.PARAMETER_CONFIG_LNB_LO_SELECT, null, ConfigurationPacket	.parseValueFunction, null, null),
	CONFIGURATION_LNB_STATUS			( null, null, null, LnbStatusPacket.parseValueFunction, null, null),
	CONFIGURATION_LNB_SWITCH_OVER		( null, null, null, LnbStatusPacket.parseValueFunction, null, null),

	CONTROL_ACTIVE_MODULE	( null, null, null, ActiveModulePacket.parseValueFunction, null, null),
	CONTROL_MODULE_LIST		( null, null, null, ModuleListPacket.parseValueFunction, null, 30),

	FCM_DEVICE_DEBUG_PLL_REG			( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, null, null, null),
	FCM_DEVICE_DEBUG_PLL_REG_DOWN_GAIN	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, null, null, null),
	FCM_DEVICE_DEBUG_PLL_REG_UP_GAIN	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, null, null, null),
	FCM_DEVICE_DEBUG_PLL_REG_OFFSET		( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, null, null, null),

	FCM_ADC_INPUT_POWER		( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	FCM_ADC_OUTPUT_POWER	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	FCM_ADC_TEMPERATURE		( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	FCM_ADC_CLKREF_LVL		( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	FCM_ADC_CURRENT			( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	FCM_ADC_5V5				( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	FCM_ADC_13v2			( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	FCM_ADC_13V2_NEG		( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),

	PRODUCTION_GENERIC_SET_1_INITIALIZE( PacketGroupIDs.PRODUCTION_GENERIC_SET_1, PacketImp.PARAMETER_ID_PRODUCTION_GENERIC_SET_1_DP_INIT, null, null, 5000L, null),

	NETWORK_ADDRESS		( null, null, null, NetworkAddressPacket.parseValueFunction, null, 13),

	REDUNDANCY_STATUS			( null, null, null, RedundancyControllerStatusPacket.parseValueFunction, null, null),
	REDUNDANCY_SWITCHOVER		( null, null, null, null, null, null),
	REDUNDANCY_MODE				( null, null, null, RedundancyModePacket.parseValueFunction, null, null),
	REDUNDANCY_SWITCHOVER_MODE	( null, null, null, SwitchoverModePacket.parseValueFunction, null, null),

	DEVICES( PacketGroupIDs.DEVICE_DEBUG, null, null, null, null, null),
	DEVICE_DEBUG_CALIBRATION_MODE	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_CALIBRATION_MODE, null, CallibrationModePacket.parseValueFunction, null, null),
	DEVICE_DEBUG_CONVERTER_PLL_1	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, null, null, null),
	DEVICE_DEBUG_CONVERTER_PLL_2	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, null, null, null),

	DEVICE_DEBUG_INFO					( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_INFO, null, DeviceDebugInfoPacket.parseValueFunction, 1000L, 2000),
	DEVICE_DEBUG_INFO_FOR_DUMP			( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_INFO, null, DeviceDebugInfoPacket.parseValueFunction, 1000L, 2000),
	DEVICE_DEBUG_CPU_INFO				( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_INFO, null, DeviceDebugInfoPacket.parseValueFunction, 1000L, 500),
	DEVICE_DEBUG_HARDWARE_FAULTS_INFO	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_INFO, null, DeviceDebugInfoPacket.parseValueFunction, 1000L, 500),
	DEVICE_DEBUG_THRESHOLDS_INFO		( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_INFO, null, DeviceDebugInfoPacket.parseValueFunction, 1000L, 1000),
	DEVICE_DEBUG_I2C1_INFO				( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_INFO, null, DeviceDebugInfoPacket.parseValueFunction, 1000L, 2000),
	DEVICE_DEBUG_MUTE_INFO				( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_INFO, null, DeviceDebugInfoPacket.parseValueFunction, 1000L, 2000),
	DEVICE_DEBUG_SCP_DEVICE_INFO		( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_INFO, null, DeviceDebugInfoPacket.parseValueFunction, 1000L, 2000),
	DEVICE_DEBUG_CONVERTER_INFO_DUMP	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_INFO, null, DeviceDebugInfoPacket.parseValueFunction, 1000L, 2000),

	DEVICE_DEBUG_PLL2_DUMP				( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_DUMP, null, DeviceDebugInfoPacket.parseValueFunction, 1000L, 2000),
	DEVICE_DEBUG_DEVICEs_DUMP			( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_DUMP, null, DeviceDebugInfoPacket.parseValueFunction, 1000L, 2000),
	DEVICE_DEBUG_DEVICE1_DUMP			( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_DUMP, null, DeviceDebugInfoPacket.parseValueFunction, 1000L, 2000),
	DEVICE_DEBUG_DEVICE2_DUMP			( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_DUMP, null, DeviceDebugInfoPacket.parseValueFunction, 1000L, 1000),
	DEVICE_DEBUG_DEVICE3_DUMP			( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_DUMP, null, DeviceDebugInfoPacket.parseValueFunction, 1000L, 2000),
	DEVICE_DEBUG_HSS1_DUMP				( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_DUMP, null, DeviceDebugInfoPacket.parseValueFunction, 1000L, 400),
	DEVICE_DEBUG_HSS2_DUMP				( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_DUMP, null, DeviceDebugInfoPacket.parseValueFunction, 1000L, 400),
	DEVICE_DEBUG_VOLTAGES_DUMP			( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_DUMP, null, DeviceDebugInfoPacket.parseValueFunction, 1000L, 2000),
	DEVICE_DEBUG_ADC_CHANNELS_DUMP		( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_DUMP, null, DeviceDebugInfoPacket.parseValueFunction, 1000L, 2000),
	DEVICE_DEBUG_ADC_CHANNELS_mV_DUMP	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_DUMP, null, DeviceDebugInfoPacket.parseValueFunction, 1000L, 500),
	DEVICE_DEBUG_CHANGE_COUNTERS_DUMP	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_DUMP, null, DeviceDebugInfoPacket.parseValueFunction, 1000L, 2000),
	DEVICE_DEBUG_DP1_DUMP				( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_DUMP, null, DeviceDebugInfoPacket.parseValueFunction, 1000L, 2000),

	DEVICE_DEBUG_HELP			( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, DeviceDebugInfoPacket.parseValueFunction, 1000L, 5000),

	DEVICE_POTENTIOMETERS_INIT	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, null, null, null),

	DEVICE_FCM_INDEX_1	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, null, null, null),
	//FCM packet IDs mixed index and address
	DEVICE_CONVERTER_DAC1_FCM( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, null, null, null),	// PACKET_ID_DEVICE_FCM_INDEX_1 + address
	DEVICE_CONVERTER_DAC2_FCM( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, null, null, null),
	DEVICE_CONVERTER_DAC3_FCM( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, null, null, null),
	DEVICE_CONVERTER_DAC4_FCM( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, null, null, null),

	//address = 0
	DEVICE_DEBUG_ADDR_0				( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, null),
	DEVICE_DEBUG_POTENTIOMETER_N1		( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),	//PACKET_ID_DEVICE_DEBUG_ADDR_8 + index
	DEVICE_DEBUG_POTENTIOMETER_N1_SET	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),	//PACKET_ID_DEVICE_DEBUG_ADDR_11 + index
	DEVICE_DEBUG_POTENTIOMETER_N1_SAVE	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, 200L, 12),	//PACKET_ID_DEVICE_DEBUG_ADDR_11 + index
	DEVICE_DEBUG_POTENTIOMETER_N2		( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),	//PACKET_ID_DEVICE_DEBUG_ADDR_0 + index
	DEVICE_DEBUG_POTENTIOMETER_N2_SET	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),	//PACKET_ID_DEVICE_DEBUG_ADDR_3 + index
	DEVICE_DEBUG_POTENTIOMETER_N2_SAVE	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, 200L, 12),	//PACKET_ID_DEVICE_DEBUG_ADDR_3 + index
	DEVICE_DEBUG_POTENTIOMETER_N3		( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_DEBUG_POTENTIOMETER_N3_SET	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_DEBUG_POTENTIOMETER_N3_SAVE	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, 200L, 12),
	DEVICE_DEBUG_POTENTIOMETER_N4		( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_DEBUG_POTENTIOMETER_N4_SET	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_DEBUG_POTENTIOMETER_N4_SAVE	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, 200L, 12),
	DEVICE_DEBUG_POTENTIOMETER_N5		( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_DEBUG_POTENTIOMETER_N5_SET	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_DEBUG_POTENTIOMETER_N5_SAVE	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, 200L, 12),
	DEVICE_DEBUG_POTENTIOMETER_N6		( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_DEBUG_POTENTIOMETER_N6_SET	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_DEBUG_POTENTIOMETER_N6_SAVE	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, 200L, 12),
	DEVICE_DEBUG_POTENTIOMETER_N7		( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_DEBUG_POTENTIOMETER_N7_SET	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_DEBUG_POTENTIOMETER_N7_SAVE	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, 200L, 12),
	DEVICE_DEBUG_POTENTIOMETER_N8		( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_DEBUG_POTENTIOMETER_N8_SET	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_DEBUG_POTENTIOMETER_N8_SAVE	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, 200L, 12),


	//address = 1
	DEVICE_DEBUG_ADDR_1					( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, null),
	DEVICE_DEBUG_SWITCH_N1				( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),	//PACKET_ID_DEVICE_DEBUG_ADDR_1 + index
	DEVICE_DEBUG_SWITCH_N2				( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_DEBUG_HS1_CURRENT			( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_DEBUG_NGLOBAL				( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_DEBUG_SWITCH_N1_REMOTE_BIAS	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_DEBUG_SWITCH_N2_REMOTE_BIAS	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_DEBUG_HS1_CURRENT_REMOTE_BIAS( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_DEBUG_HS1_1_CURRENT_HP_BIAS	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_DEBUG_HS1_2_CURRENT_HP_BIAS	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_DEBUG_NGLOBAL_REMOTE_BIAS	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),

	//address = 2
	DEVICE_DEBUG_ADDR_2					( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, null),
	DEVICE_DEBUG_HS2_CURRENT			( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),	//PACKET_ID_DEVICE_DEBUG_ADDR_2 + index
	DEVICE_DEBUG_HS2_CURRENT_REMOTE_BIAS( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_DEBUG_HS2_1_CURRENT_HP_BIAS	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_DEBUG_HS2_2_CURRENT_HP_BIAS	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_DEBUG_HS3_1_CURRENT_HP_BIAS	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_DEBUG_HS3_2_CURRENT_HP_BIAS	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_DEBUG_HS4_1_CURRENT_HP_BIAS	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_DEBUG_HS4_2_CURRENT_HP_BIAS	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),

	//address = 3
	DEVICE_DEBUG_ADDR_3						( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, null),
	DEVICE_DEBUG_OUTPUT_POWER				( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_DEBUG_OUTPUT_POWER_REMOTE_BIAS	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_DEBUG_OUTPUT_POWER_HP_BIAS		( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),

	//address = 4
	DEVICE_DEBUG_ADDR_4					( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, null),
	DEVICE_DEBUG_TEMPERATURE			( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),	//PACKET_ID_DEVICE_DEBUG_ADDR_4 + index
	DEVICE_DEBUG_TEMPERATURE_REMOTE_BIAS( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_DEBUG_TEMPERATURE_HP_BIAS	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),

	//address = 8
	DEVICE_DEBUG_ADDR_8					( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, null),

	//address = 11
	DEVICE_DEBUG_ADDR_11				( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, null),
	DEVICE_DEBUG_PACKET					( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),	//used in DeviceDebugPanel

	//address = 100
	DEVICE_DEBUG_ADDR_100		( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, null),
	DEVICE_CONVERTER_DAC1		( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_CONVERTER_DAC1_SET	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_CONVERTER_DAC2		( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_CONVERTER_DAC2_SET	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_CONVERTER_DAC3		( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_CONVERTER_DAC3_SET	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_CONVERTER_DAC4		( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),
	DEVICE_CONVERTER_DAC4_SET	( PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, null, RegisterPacket.parseValueFunction, null, 12),

	DUMPS( PacketGroupIDs.DEVICE_DEBUG, null, null, null, null, null),
	DUMP_DEVICE_DEBUG_INFO_0( PacketGroupIDs.DEVICE_DEBUG, null, null, DeviceDebugInfoPacket.parseValueFunction, null, null),	//Index 0.  Board info, like: CPU frequency, CPU reset type, CPU clock source, Local temperature
	DUMP_DEVICE_DEBUG_INFO_1( PacketGroupIDs.DEVICE_DEBUG, null, null, DeviceDebugInfoPacket.parseValueFunction, null, null),	//Index 1.  Board error info
	DUMP_DEVICE_DEBUG_INFO_2( PacketGroupIDs.DEVICE_DEBUG, null, null, DeviceDebugInfoPacket.parseValueFunction, null, null),	//Index 2.  Threshold info, like:  Zero current, Over-current HSS1, Over-current HSS2, Over-temperature mute, Over-temperature unmute
	DUMP_DEVICE_DEBUG_INFO_3( PacketGroupIDs.DEVICE_DEBUG, null, null, DeviceDebugInfoPacket.parseValueFunction, null, null),	//Index 3.  I2C error statistics
	DUMP_DEVICE_DEBUG_INFO_4( PacketGroupIDs.DEVICE_DEBUG, null, null, DeviceDebugInfoPacket.parseValueFunction, null, null),	//Index 4.  Mute state machine flags in format: Flag_name: Current_flag ( null, History_flag, null)
	DUMP_DEVICE_DEBUG_INFO_10( PacketGroupIDs.DEVICE_DEBUG, null, null, DeviceDebugInfoPacket.parseValueFunction, null, null),	//Index 10.  FCM info
	DUMP_DEVICE_DEBUG_INFO_11( PacketGroupIDs.DEVICE_DEBUG, null, null, DeviceDebugInfoPacket.parseValueFunction, null, null),	//Index 11. 
	DUMP_POWER( PacketGroupIDs.DEVICE_DEBUG, null, null, null, null, null),

	DUMP_REGISTER_1		( PacketGroupIDs.DEVICE_DEBUG, null, null, DeviceDebugInfoPacket.parseValueFunction, null, null),
	DUMP_REGISTER_2		( PacketGroupIDs.DEVICE_DEBUG, null, null, DeviceDebugInfoPacket.parseValueFunction, null, null),
	DUMP_REGISTER_3		( PacketGroupIDs.DEVICE_DEBUG, null, null, DeviceDebugInfoPacket.parseValueFunction, null, null),
	DUMP_REGISTER_4		( PacketGroupIDs.DEVICE_DEBUG, null, null, DeviceDebugInfoPacket.parseValueFunction, null, null),
	DUMP_REGISTER_5		( PacketGroupIDs.DEVICE_DEBUG, null, null, DeviceDebugInfoPacket.parseValueFunction, null, null),
	DUMP_REGISTER_6		( PacketGroupIDs.DEVICE_DEBUG, null, null, DeviceDebugInfoPacket.parseValueFunction, null, null),
	DUMP_REGISTER_7		( PacketGroupIDs.DEVICE_DEBUG, null, null, DeviceDebugInfoPacket.parseValueFunction, null, null),
	DUMP_REGISTER_10	( PacketGroupIDs.DEVICE_DEBUG, null, null, DeviceDebugInfoPacket.parseValueFunction, null, null),
	DUMP_REGISTER_100	( PacketGroupIDs.DEVICE_DEBUG, null, null, DeviceDebugInfoPacket.parseValueFunction, null, null),
	DUMP_REGISTER_201	( PacketGroupIDs.DEVICE_DEBUG, null, null, DeviceDebugInfoPacket.parseValueFunction, null, null),
	DUMP_REGISTER_202	( PacketGroupIDs.DEVICE_DEBUG, null, null, DeviceDebugInfoPacket.parseValueFunction, null, null),
	DUMP_REGISTER_207	( PacketGroupIDs.DEVICE_DEBUG, null, null, DeviceDebugInfoPacket.parseValueFunction, null, null),
	DUMP_REGISTER_220	( PacketGroupIDs.DEVICE_DEBUG, null, null, DeviceDebugInfoPacket.parseValueFunction, null, null),

	CLEAR_STATISTICS	( null, null, null, null, null, null),
	PROTO_RETRANSNIT	( null, null, null, RetransmitPacket.parseValueFunction, null, null);

private final static Logger logger = LogManager.getLogger();

private final PacketGroupIDs packetGroupIDs;
private final Byte parameterCode;
private final Function<Packet, Optional<Object>> function;
private final String text;
private final Long timeout;
private final Integer maxSize;

private PacketID( PacketGroupIDs packetGroupIDs, Byte parameterCode, String text, Function<Packet, Optional<Object>> function, Long timeout, Integer maxSize){
	this.packetGroupIDs = packetGroupIDs;
	this.parameterCode = parameterCode;
	this.function = function;
	this.text = text;
	this.timeout = timeout;
	this.maxSize = maxSize;
}

public short getId() {
	return (short) ordinal();
}

public boolean match(short id){
	return ((short)ordinal())==id;
}

public boolean match(Packet packet){
	short id = Optional.ofNullable(packet).map(Packet::getHeader).map(PacketHeader::getPacketId).orElse((short) -1);
	return match(id);
}

public boolean match(PacketWork packetWork) {
	final Packet packet = Optional
			.of(packetWork)
			.map(PacketWork::getPacketThread)
			.map(PacketThreadWorker::getPacket)
			.orElse(null);
	return match(packet);
}

public PacketGroupIDs getPacketGroupIDs() {
	return packetGroupIDs;
}

public Byte getParameterCode() {
	return parameterCode;
}

public  Optional<Object> valueOf(Packet packet) {

	try {

		return Optional.ofNullable(function).flatMap(f->f.apply(packet));

	}catch (Exception e) {

		logger.catching(e);
		return Optional.empty();
	}
}

public Long getTimeout() {
	return Optional.ofNullable(timeout).orElse(100L);
}

public int getMaxSize() {
	return Optional.ofNullable(maxSize).orElse(4);
}

public String toString(){
	return Optional.ofNullable(text).map(t->name() + "-" + text ).orElse(name() + "(" + ordinal() + ")");
}

public static String toString(short id){
	int intID = id&0xFF;
	final PacketID[] values = values();

	if(intID<values.length){
		final PacketID v = values[intID];
		return v.name() + "(" + v.ordinal() + ")";
	}else
		return  Integer.toString(intID);
}

public static Optional<PacketID> valueOf(short packetId) {
	final Optional<PacketID> optional = Arrays.stream(values()).parallel().filter(id->id.ordinal()==packetId).findAny();

	if(!optional.isPresent())
		logger.warn(packetId);

	return optional;
}
}
