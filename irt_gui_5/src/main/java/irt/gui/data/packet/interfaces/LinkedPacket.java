
package irt.gui.data.packet.interfaces;

import java.util.List;
import java.util.Observer;

import irt.gui.data.packet.LinkHeader;
import irt.gui.data.packet.Packet;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.Payload;
import irt.gui.errors.PacketParsingException;

public interface LinkedPacket extends Comparable<LinkedPacket>{

	LinkHeader		getLinkHeader();
	PacketHeader	getPacketHeader();
	List<Payload>	getPayloads();
	byte[]			toBytes();

	byte[] 			getAnswer();
	void 			setAnswer(byte[] data);

	byte[] 			getAcknowledgement();

	void 			addObserver(Observer observer);

	public static final int PACKET_ACKNOWLEDGEMENT_SIZE = 11;

	public enum PacketType{
		SPONTANEOUS		((byte)0),		/* Spontaneous message, generated by device. */
		RESPONSE		((byte)1),		/* Response, generated as response to command or status request. */
		REQUEST			((byte)2),		/* Status request. */
		COMMAND			((byte)3),		/* Command. */
		ACKNOWLEGEMENT	((byte)0xFF);	/* Layer 2 acknowlegement. */

		private byte value; 		public byte getValue() { return value; }

		private PacketType(byte value){
			this.value = value;
		}

		@Override
		public String toString(){
			return name() + ":" + value;
		}
	}

	public enum PacketId{
		//DEVICE_INFO
		DEVICE_INFO		(	PacketGroupId.DEVICE_INFO,		ParameterHeaderCode.DI_ALL),
		//DEVICE_DEBAG
		DEVICE_DEBAG_POTENTIOMETER		(	PacketGroupId.DEVICE_DEBAG,		ParameterHeaderCode.DD_READ_WRITE		),
		DEVICE_DEBAG_CALIBRATION_MODE	(	PacketGroupId.DEVICE_DEBAG,		ParameterHeaderCode.DD_CALIBRATION_MODE	),
		//CONFIGURATION
		CONFIGURATION_ATTENUATION		(	PacketGroupId.CONFIGURATION,	ParameterHeaderCode.CONF_ATTENURATION		),
		CONFIGURATION_ATTENUATION_RANGE	(	PacketGroupId.CONFIGURATION,	ParameterHeaderCode.CONF_ATTENURATION_RABGE	),
		//Alarms
		ALARMS					(	PacketGroupId.ALARM,		ParameterHeaderCode.ALARM_IDs			),
		ALARM_SUMMARY_STATUS	(	PacketGroupId.ALARM,		ParameterHeaderCode.ALARM_SUMMARY_STATUS),
		ALARM_STATUS			(	PacketGroupId.ALARM,		ParameterHeaderCode.ALARM_STATUS		),
		ALARM_DESCRIPTION		(	PacketGroupId.ALARM,		ParameterHeaderCode.ALARM_DESCRIPTION	),
		ALARM_NAME				(	PacketGroupId.ALARM,		ParameterHeaderCode.ALARM_NAME	),
		//Converter
		DEVICE_DEBAG_CONVERTER_DAC	(	PacketGroupId.DEVICE_DEBAG,		ParameterHeaderCode.DD_CONVERTER_DAC),
		//Measurement
		MEASUREMENT_TEMPERATURE	(	PacketGroupId.MEASUREMENT,		ParameterHeaderCode.M_TEMPERATURE);

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

	public enum PacketGroupId{
		NONE			((byte) 0),		/* Reserved for special use. */
		ALARM			((byte) 1),		/* Alarm: message content is product specific. */
		CONFIGURATION	((byte) 2),		/* Configuration: content is product specific. */
		FILETRANSFER	((byte) 3),		/* File transfer: software upgrade command (optional). */
		MEASUREMENT		((byte) 4),		/* Measurement: device status, content is product specific. */
		RESET			((byte) 5),		/* Device reset: generic command. */
		DEVICE_INFO		((byte) 8),		/* Device information: generic command. */
		CONFIG_PROFILE	((byte) 9),		/* Save configuration: generic command. */
		DEVICE_DEBAG	((byte)61);		/* Device Debug. */

		byte value = 0; 			public byte getValue() { return value; }

		private PacketGroupId(byte value){
			this.value = value;
		}

		@Override
		public String toString(){
			return name() + ":" + value;
		}
	}

	public enum ParameterHeaderCode{
		//Device debug
		DD_READ_WRITE			(	Packet.PARAMETER_DEVICE_DEBAG_READ_WRITE			, PacketGroupId.DEVICE_DEBAG	),
		DD_CALIBRATION_MODE		(	Packet.PARAMETER_DEVICE_DEBAG_CALIBRATION_MODE		, PacketGroupId.DEVICE_DEBAG	),
		//device info
		DI_DEVICE_TYPE			(	Payload.DI_DEVICE_TYPE				, PacketGroupId.DEVICE_INFO		),
		DI_DEVICE_SN			(	Payload.DI_DEVICE_SN				, PacketGroupId.DEVICE_INFO		),
		DI_FIRMWARE_VERSION		(	Payload.DI_FIRMWARE_VERSION			, PacketGroupId.DEVICE_INFO		),
		DI_FIRMWARE_BUILD_DATE	(	Payload.DI_FIRMWARE_BUILD_DATE		, PacketGroupId.DEVICE_INFO		),
		DI_UNIT_UPTIME_COUNTER	(	Payload.DI_UNIT_UPTIME_COUNTER		, PacketGroupId.DEVICE_INFO		),
		DI_UNIT_NAME			(	Payload.DI_UNIT_NAME				, PacketGroupId.DEVICE_INFO		),
		DI_UNIT_PART_NUMBER		(	Payload.DI_UNIT_PART_NUMBER			, PacketGroupId.DEVICE_INFO		),
		DI_ALL					(	Packet.IRT_SLCP_PARAMETER_ALL		, PacketGroupId.DEVICE_INFO		),
		//Configuration
		CONF_ATTENURATION_RABGE	(	Packet.PARAMETER_CONFIG_FCM_ATTENUATION_RANGE		, PacketGroupId.CONFIGURATION	),
		CONF_ATTENURATION		(	Packet.PARAMETER_PICOBUC_CONFIGURATION_ATTENUATION	, PacketGroupId.CONFIGURATION	),
		//Alarms
		ALARM_IDs				(	Packet.ALARMS_IDs				, PacketGroupId.ALARM),
		ALARM_SUMMARY_STATUS	(	Packet.ALARM_SUMMARY_STATUS		, PacketGroupId.ALARM),
		ALARM_CONFIG			(	Packet.ALARM_CONFIG				, PacketGroupId.ALARM),
		ALARM_STATUS			(	Packet.ALARM_STATUS				, PacketGroupId.ALARM),
		ALARM_NAME				(	Packet.ALARM_NAME				, PacketGroupId.ALARM),
		ALARM_DESCRIPTION		(	Packet.ALARM_DESCRIPTION		, PacketGroupId.ALARM),
		//Converter
		DD_CONVERTER_DAC	(Packet.PARAMETER_CONVERTER_DAC		, PacketGroupId.DEVICE_DEBAG),
		//Measurement
		M_TEMPERATURE	(Packet.PARAMETER_MEASUREMENT_TEMPERATURE	, PacketGroupId.MEASUREMENT);

		private byte value; 			public byte 			getValue() 	{ return value; 	}
		private PacketGroupId groupId;	public PacketGroupId 	getGroupId(){ return groupId; 	}

		private ParameterHeaderCode(byte value, PacketGroupId groupId){
			this.value = value;
			this.groupId = groupId;
		}

		@Override
		public String toString(){
			return name() + ":" + value;
		}
	}

	public enum PacketErrors{
		NO_ERROR					((byte) 0),
		SYSTEM_INTERNAL				((byte) 1),
		WRITE_ERROR					((byte) 2),
		FUNCTION_NOT_IMPLEMENTED	((byte) 3),
		VALUE_OUTSIDE_OF_VALID_RANGE((byte) 4),
		CAN_NOT_BE_GENERATED		((byte) 5),
		CAN_NOT_BE_EXECUTED			((byte) 6),
		INVALID_DATA_FORMAT			((byte) 7),
		INVALID_VALUE				((byte) 8),
		NOT_ENOUGH_MEMORY			((byte) 9),
		REQUESTED_ELEMENT_NOT_FOUND	((byte)10),
		TIMED_OUT					((byte)11),
		COMMUNICATION_PROBLEM		((byte) 20),
		UNKNOWN						(Byte.MAX_VALUE);

		private byte value; 		public byte getValue()	{ return value; }

		private PacketErrors(byte value){
			this.value = value;
		}

		public static PacketErrors valueOf(short value){
			PacketErrors packetError = UNKNOWN;

			for(PacketErrors p:values())
				if(p.getValue()==value){
					packetError = p;
					break;
				}

			return packetError;
		}

		@Override
		public String toString(){
			return name() + "(" + value + ")";
		}
	}
}