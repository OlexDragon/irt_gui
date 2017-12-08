
package irt.gui.data.packet.enums;

import irt.gui.errors.PacketParsingException;

public enum PacketId {

	//DEVICE_INFO
	DEVICE_INFO		(	PacketGroupId.DEVICE_INFO,		ParameterHeaderCode.DI_ALL),
	//DEVICE_DEBAG
	DEVICE_DEBAG_REGISTER			(	PacketGroupId.DEVICE_DEBAG	, ParameterHeaderCode.DD_READ_WRITE			),
	DEVICE_DEBAG_CALIBRATION_MODE	(	PacketGroupId.DEVICE_DEBAG	, ParameterHeaderCode.DD_CALIBRATION_MODE	),
	DEVICE_DEBAG_INFO				(	PacketGroupId.DEVICE_DEBAG	, ParameterHeaderCode.DD_INFO				),
	DEVICE_DEBAG_DUMP				(	PacketGroupId.DEVICE_DEBAG	, ParameterHeaderCode.DD_DUMP				),
	DEVICE_DEBAG_REGISTER_INDEXES	(	PacketGroupId.DEVICE_DEBAG	, ParameterHeaderCode.DD_REGISTER_INDEXES	),
	//CONFIGURATION
	CONFIGURATION_ATTENUATION		(	PacketGroupId.CONFIGURATION	, ParameterHeaderCode.CONF_ATTENURATION			),
	CONFIGURATION_ATTENUATION_FCM	(	PacketGroupId.CONFIGURATION	, ParameterHeaderCode.CONF_ATTENURATION_FCM		),
	CONFIGURATION_ATTENUATION_RANGE	(	PacketGroupId.CONFIGURATION	, ParameterHeaderCode.CONF_ATTENURATION_RANGE	),
	CONFIGURATION_10MHZ_SOURCE		(	PacketGroupId.CONFIGURATION	, ParameterHeaderCode.CONF_10MHZ_SOURCE			),
	CONFIGURATION_10MHZ_DAC			(	PacketGroupId.CONFIGURATION	, ParameterHeaderCode.CONF_10MHZ_DAC			),
	CONFIGURATION_10MHZ_DAC_RANGE	(	PacketGroupId.CONFIGURATION	, ParameterHeaderCode.CONF_10MHZ_DAC_RANGE	),
	CONFIGURATION_GAIN				(	PacketGroupId.CONFIGURATION	, ParameterHeaderCode.CONF_GAIN					),
	CONFIGURATION_GAIN_RANGE		(	PacketGroupId.CONFIGURATION	, ParameterHeaderCode.CONF_GAIN_RANGE			),
	CONFIGURATION_FREQUENCY			(	PacketGroupId.CONFIGURATION	, ParameterHeaderCode.CONF_FREQUENCY			),
	CONFIGURATION_FREQUENCY_FCM		(	PacketGroupId.CONFIGURATION	, ParameterHeaderCode.CONF_FREQUENCY_FCM		),
	CONFIGURATION_FREQUENCY_RANGE	(	PacketGroupId.CONFIGURATION	, ParameterHeaderCode.CONF_FREQUENCY_RANGE		),
	CONFIGURATION_FREQUENCY_RANGE_FCM(	PacketGroupId.CONFIGURATION	, ParameterHeaderCode.CONF_FREQUENCY_RANGE_FCM	),
	CONFIGURATION_MUTE				(	PacketGroupId.CONFIGURATION	, ParameterHeaderCode.CONF_MUTE					),
	CONFIGURATION_FCM_MUTE			(	PacketGroupId.CONFIGURATION	, ParameterHeaderCode.CONF_FCM_MUTE				),
	CONFIGURATION_LO_FREQUENCIES	(	PacketGroupId.CONFIGURATION	, ParameterHeaderCode.CONF_LO_FREQUENCIES		),
	CONFIGURATION_LO				(	PacketGroupId.CONFIGURATION	, ParameterHeaderCode.CONF_LO					),

	SAVE_CONFIG_PROFILE				(	PacketGroupId.CONFIG_PROFILE, ParameterHeaderCode.SAVE_CONFIG	),

	CONFIGURATION_NETWORK_ADDRESS	(	PacketGroupId.NETWORK		, ParameterHeaderCode.NETWORK_ADDRESS		),
	//Alarms
	ALARMS					(	PacketGroupId.ALARM,		ParameterHeaderCode.ALARM_IDs			),
	ALARM_SUMMARY_STATUS	(	PacketGroupId.ALARM,		ParameterHeaderCode.ALARM_SUMMARY_STATUS),
	ALARM_STATUS			(	PacketGroupId.ALARM,		ParameterHeaderCode.ALARM_STATUS		),
	ALARM_DESCRIPTION		(	PacketGroupId.ALARM,		ParameterHeaderCode.ALARM_DESCRIPTION	),
	ALARM_NAME				(	PacketGroupId.ALARM,		ParameterHeaderCode.ALARM_NAME	),
	//Converter
	DEVICE_DEBAG_CONVERTER_DAC	(	PacketGroupId.DEVICE_DEBAG,		ParameterHeaderCode.DD_CONVERTER_DAC),
	//Measurement
	MEASUREMENT_REFERENCE_LEVEL	(	PacketGroupId.MEASUREMENT,		ParameterHeaderCode.M_REFERENCE_LEVEL),
	MEASUREMENT_TEMPERATURE		(	PacketGroupId.MEASUREMENT,		ParameterHeaderCode.M_TEMPERATURE),
	MEASUREMENT_TEMPERATURE_PCS	(	PacketGroupId.MEASUREMENT,		ParameterHeaderCode.M_TEMPERATURE_PCS),
	MEASUREMENT_TEMPERATURE_MCU	(	PacketGroupId.MEASUREMENT,		ParameterHeaderCode.M_TEMPERATURE_MCU),
	MEASUREMENT_INPUT_POWER		(	PacketGroupId.MEASUREMENT,		ParameterHeaderCode.M_INPUT_POWER_BUC),
	MEASUREMENT_INPUT_POWER_FCM	(	PacketGroupId.MEASUREMENT,		ParameterHeaderCode.M_INPUT_POWER_FCM),
	MEASUREMENT_OUTPUT_POWER	(	PacketGroupId.MEASUREMENT,		ParameterHeaderCode.M_OUTPUT_POWER),
	MEASUREMENT_STATUS_BUC		(	PacketGroupId.MEASUREMENT,		ParameterHeaderCode.M_STATUS_BUC),
	MEASUREMENT_STATUS_FCM		(	PacketGroupId.MEASUREMENT,		ParameterHeaderCode.M_STATUS_FCM),
	//Production
	PRODUCTION_INITIALIZE_BIASES(	PacketGroupId.PRODUCTION,		ParameterHeaderCode.PRODUCTION_INITIALIZE_BIASES),
	PRODUCTION_UPDATE_FCM		(	PacketGroupId.PRODUCTION,		ParameterHeaderCode.PRODUCTION_CONNECT_FCM);

	private PacketGroupId packetGroupId;				public PacketGroupId 		getPacketGroupId() 		{ return packetGroupId; 		}
	private ParameterHeaderCode parameterHeaderCode;	public ParameterHeaderCode 	getParameterHeaderCode(){ return parameterHeaderCode; 	}

	private PacketId(PacketGroupId packetGroupId, ParameterHeaderCode parameterHeaderCode){
		this.packetGroupId = packetGroupId;
		this.parameterHeaderCode = parameterHeaderCode;
	}

	public ParameterHeaderCode valueOf(byte code) throws PacketParsingException {
		for(ParameterHeaderCode phc:ParameterHeaderCode.values())
			if(phc.getGroupId()==packetGroupId && phc.getValue()==code)
				return phc;

		throw new PacketParsingException("\n\tThe code(" + code + ") for packetGroupId(" + packetGroupId + ") do not exists");
	}

	@Override
	public String toString(){
		return "PacketId:" + name() + ":" + ordinal() + ", packetGroupId:" + packetGroupId + ", parameterHeaderCode:" + parameterHeaderCode;
	}

	public short getValue() {
		return (short) ordinal();
	}
}
