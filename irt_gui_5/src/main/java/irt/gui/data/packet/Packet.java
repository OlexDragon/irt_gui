/*
 * IRT Management Protocol Specification.docx
 * 
 * 3. Application Layer
 * 3.1 Packet structure
 * Serial line protocol management packet consists of header and one or multiple parameters. 
 */
package irt.gui.data.packet;

import java.util.Arrays;

import irt.gui.data.packet.interfaces.LinkedPacket.PacketErrors;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketId;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketType;
import irt.gui.data.packet.observable.PacketAbstract;

public class Packet {

	public static final int NO_ERROR = 0;

	public static final byte FLAG_SEQUENCE	= 0x7E;
	public static final byte CONTROL_ESCAPE= 0x7D;

	// What to do
	public static final byte
							OPTYPE_SHOW_DEVICE = 1;
	public static final byte
							OPTYPE_CONFIG_GET_ALL			= 100,
							OPTYPE_CONFIG_SET_FREQUENCY		= 101,
							OPTYPE_CONFIG_GET_FREQUENCY		= 102,
							OPTYPE_CONFIG_SET_GAIN			= 103,
							OPTYPE_CONFIG_GET_GAIN			= 104,
							OPTYPE_CONFIG_SET_MUTE			= 105,
							OPTYPE_CONFIG_GET_MUTE			= 106,
							OPTYPE_CONFIG_SET_ATTENUATION	= 107,
							OPTYPE_CONFIG_GET_ATTENUATION	= 108,
							OPTYPE_CONFIG_GET_ATTEN_RANGE 	= 109,
							OPTYPE_CONFIG_SET_BUC_PS_ENABLE	= 110,
							OPTYPE_CONFIG_GET_BUC_PS_ENABLE	= 111,
							OPTYPE_CONFIG_GAIN_RANGE		= 112,
							OPTYPE_DAC1_VALUE_GET			= 113,
							OPTYPE_DAC2_VALUE_GET			= 114,
							OPTYPE_DAC3_VALUE_GET			= 115,
							OPTYPE_DAC4_VALUE_GET			= 116,
							OPTYPE_DAC1_VALUE_SET			= 117,
							OPTYPE_DAC2_VALUE_SET			= 118,
							OPTYPE_DAC3_VALUE_SET			= 119,
							OPTYPE_DAC4_VALUE_SET			= 120,
					
							OPTYPE_STATUS_GET_ALL			= 124,
							OPTYPE_SAVE_CONFIG				= 125,
							OPTYPE_SHOW_ALARMS				= 126,
							OPTYPE_ENVIRONMENT_CONFIGURATION= 127;

	public static final short
							OPTYPE_TEST_SET1_PLL_SET		= 10000,
							OPTYPE_DAC_VALUE_SET			= 10001,
							OPTYPE_DAC_VALUE_GET			= 10002,
							OPTYPE_PLL_REGISTER_VALUE_SET	= 10003,
							OPTYPE_VCO_SELECT_SET			= 10004,
							OPTYPE_BAND_SELECT_SET			= 10005,
							OPTYPE_CALIBRATION_ON_OFF 		= 10006;

	/* 3.2 Packet header 
	 * Packet header includes the following fields:
	 */
	public static final byte
		PACKET_TYPE_SPONTANEOUS= 0x0,		/* Spontaneous message, generated by device. */
		PACKET_TYPE_RESPONSE	= 0x1,		/* Response, generated as response to command or status request. */
		PACKET_TYPE_REQUEST	= 0x2,		/* Status request. */
		PACKET_TYPE_COMMAND	= 0x3,		/* Command. */
		PACKET_TYPE_ACK		= (byte) 0xFF;/* Layer 2 acknowlegement. */

	/*	User interface.
	 *  Packet ID.
	 *  Packet ID represents unique identifier of �command/request � response� transaction.
	 *  Packet ID is generated on the client side and is copied to response message on the server side.
	 *   Acknowledgement message always contains ID of the received packet acknowledgement was sent on. */
	public static final byte
		IRT_SLCP_PACKET_ID_NONE	= 0,		/* Reserved for special use. */
		GROUP_ID_ALARM			= 1,		/* Alarm: message content is product specific. */
		GROUP_ID_CONFIGURATION	= 2,		/* Configuration: content is product specific. */
		GROUP_ID_FILETRANSFER	= 3,		/* File transfer: software upgrade command (optional). */
		GROUP_ID_MEASUREMENT	= 4,		/* Measurement: device status, content is product specific. */
		GROUP_ID_RESET			= 5,		/* Device reset: generic command. */
		GROUP_ID_DEVICE_INFO	= 8,		/* Device information: generic command. */
		GROUP_ID_CONFIG_PROFILE	= 9,		/* Save configuration: generic command. */
		GROUP_ID_DEVICE_DEBAG	= 61,		/* Device Debug. */

	/* Protocol */
		IRT_SLCP_GROUP_ID_PROTOCOL = 10, /* Packet protocol parameters configuration and monitoring. */

	/* Network */
		IRT_SLCP_PACKET_ID_NETWORK = 11, /* Network configuration. */

	/* backwards compatibility - to be deleted */
		IRT_SLCP_PACKET_ID_PRODUCTION_GENERIC_SET_1 = 100,
		IRT_SLCP_GROUP_ID_DEVELOPER_GENERIC_SET_1 = 120;
	/* Parameter general types definition. */
	public static final byte
		IRT_SLCP_PARAMETER_NONE		= 0,
		IRT_SLCP_PARAMETER_ALL		= (byte) 255;

	/* Measurement codes. */
	public static final byte
			PARAMETER_MEASUREMENT_INPUT_POWER	= 1,
			PARAMETER_MEASUREMENT_OUTPUT_POWER	= 2,
			PARAMETER_MEASUREMENT_STATUS		= 4,
			PARAMETER_MEASUREMENT_WGS_POSITION	= 4,
			PARAMETER_MEASUREMENT_LNB1_STATUS	= 5,
			PARAMETER_MEASUREMENT_LNB2_STATUS	= 6,
			IRT_SLCP_PARAMETER_MEASUREMENT_PICOBUC_ALL = IRT_SLCP_PARAMETER_ALL; /* Read all available measurements. */

	public static final byte
		PARAMETER_MEASUREMENT_FCM_NONE = IRT_SLCP_PARAMETER_NONE,
		PARAMETER_MEASUREMENT_FCM_SUMMARY_ALARM		= 1,	//Flags
		PARAMETER_MEASUREMENT_FCM_STATUS			= 2,
		PARAMETER_MEASUREMENT_TEMPERATURE			= 3,
		PARAMETER_MEASUREMENT_FCM_INPUT_POWER		= 4,
		PARAMETER_MEASUREMENT_FCM_OUTPUT_POWER		= 5,
		PARAMETER_MEASUREMENT_FCM_MON_5V5			= 6,
		PARAMETER_MEASUREMENT_FCM_MON_13V2_POS		= 7,
		PARAMETER_MEASUREMENT_FCM_MON_13V2_NEG		= 8,
		PARAMETER_MEASUREMENT_FCM_CURRENT			= 9,
		PARAMETER_MEASUREMENT_FCM_TEMPERATURE_CPU	= 10,
		PARAMETER_MEASUREMENT_FCM_ALL = IRT_SLCP_PARAMETER_ALL;	/* Read all available measurements. */

	/*Device Debug - IRT_SLCP_PACKET_ID_DEVICE_DEBAG*/
	public static final byte
	PARAMETER_DEVICE_DEBAG_INFO 		= 1,		/* device information: parts, firmware and etc. */
	PARAMETER_DEVICE_DEBAG_DUMP 		= 2,		/* dump of registers for specified device index */
	PARAMETER_DEVICE_DEBAG_READ_WRITE 	= 3,		/* registers read/write operations */
	PARAMETER_DEVICE_DEBAG_INDEX 		= 4,		/* device index information print */
	PARAMETER_DEVICE_DEBAG_CALIBRATION_MODE = 5,	/* calibration mode */
	PARAMETER_DEVICE_DEBUG_ENVIRONMENT_IO = 10;	/* operations with environment variables */

	/* Configuration codes. */
	public static final byte
		PARAMETER_CONFIG_FCM_NONE = (IRT_SLCP_PARAMETER_NONE),
		PARAMETER_CONFIG_FCM_GAIN					= 1,
		PARAMETER_CONFIG_FCM_ATTENUATION			= 2,
		PARAMETER_CONFIG_FCM_FREQUENCY				= 3,
		PARAMETER_CONFIG_FCM_FREQUENCY_RANGE		= 4,
		PARAMETER_CONFIG_FCM_GAIN_RANGE				= 5,
		PARAMETER_CONFIG_FCM_ATTENUATION_RANGE		= 6,
		PARAMETER_CONFIG_FCM_MUTE_CONTROL			= 7,
		PARAMETER_CONFIG_BUC_ENABLE					= 8,
		PARAMETER_CONFIG_FCM_FLAGS 					= 9,
		PARAMETER_CONFIG_FCM_GAIN_OFFSET			= 10,
		PARAMETER_CONFIG_FCM_ALC_ENABLED			= 12,
		PARAMETER_CONFIG_FCM_ALC_LEVEL				= 13,
		PARAMETER_CONFIG_FCM_ALC_LEVEL_RANGE		= 14,
		PARAMETER_CONFIG_DLRS_WGS_SWITCHOVER		= 14,
		PARAMETER_CONFIG_FCM_ALC_OVERDRIVE_PROTECTION_ENABLED	= 15,
		PARAMETER_CONFIG_FCM_ALC_OVERDRIVE_PROTECTION_THRESHOLD	= 16,
		PARAMETER_CONFIG_FCM_ALC_OVERDRIVE_PROTECTION_THRESHOLD_RANGE= 17,
		PARAMETER_CONFIG_FCM_ALL = IRT_SLCP_PARAMETER_ALL;		/* Read all available parameters. */

	/* Test. */
	public static final byte
		IRT_SLCP_PARAMETER_DEVELOPER_GENERIC_SET_1_DAC_CONFIG = 1;

	public static final byte
	PARAMETER_PRODUCTION_GENERIC_SET_1_CALIBRATION_MODE_CONFIG = 1,
	PARAMETER_PRODUCTION_GENERIC_SET_1_ENVIRONMENT_CONFIG = 2;

	/* Configuration saving parameter codes. */
	public static final byte
		PACKET_ID_CONFIG_PROFILE_NONE = (IRT_SLCP_PARAMETER_NONE),
		PACKET_ID_CONFIG_PROFILE_SAVE = 1;


	/* Network */
	public static final byte
		IRTSCP_PARAMETER_ID_NETWORK_ADDRESS = 1; /* Network configuration. */

	protected static final String SAVE_CONFIG = null;

	public static final int PACKET_ID = 0;

	public static final byte ERROR_NO_ERROR						= 0,	//No error (positive acknowledge). Indicates successful completion of command or request.
							ERROR_INTERNAL_ERROR				= 1,	//System internal error during operation.
							ERROR_WRITE_ERROR					= 2,	//Non-volatile memory write error.
							ERROR_FUNCTION_NOT_IMPLEMENTED		= 3,	//Function not implemented.
							ERROR_VALUE_OUT_OF_RANGE			= 4,	//Value outside of valid range.
							ERROR_INFORMATION_CANNOT_BE_GENERATED=5,	//Requested information can�t be generated
							ERROR_COMMAND_CANNOT_BE_EXECUTED	= 6,	//Command can�t be executed.
							ERROR_INVALID_DATA_FORMAT			= 7,	//Invalid data format.
							ERROR_INVALID_VALUE					= 8,	//Invalid value, same as �Value out of range� error, but more generic. 
							ERROR_NO_MEMORY						= 9,	//Not enough memory for operation.
							ERROR_REQUESTED_ELEMENT_NOT_FOUND	= 10,	//Requested element not found.
							ERROR_TIMED_OUT						= 11;	//Timed out

	//PicoBUC Bias board
	public static final byte IRT_SLCP_PARAMETER_PICOBUC_LO_SELECT = 1;

	public static final byte IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_LO_SET 		= 1,
							IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_MUTE 			= 2,
							IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_GAIN 			= 3,
							PARAMETER_PICOBUC_CONFIGURATION_ATTENUATION 	= 4,
							IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_GAIN_RANGE 	= 5,
							IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_LO_FREQUENCIES	= 7,
							IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_USER_FREQUENCY = 8,
							IRT_SLCP_PARAMETER_CONFIGURATION_PICOBUC_USER_FREQUENCY_RANGE = 9,
							IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_REDUNDANCY_ENABLE	= 10,
							IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_REDUNDANCY_MODE	= 11,
							IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_REDUNDANCY_NAME	= 12,
							IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_REDUNDANCY_STAT	= 15,
							IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_REDUNDANCY_SET_ONLINE= 14;

	/* PicoBUC production procedures */

	public static final byte PACKET_ID_PRODUCTION_GENERIC_SET_1_DP_INIT = 1; /* Re-init default values of RDAC */

	public static final byte IRT_SLCP_PARAMETER_PROTOCOL_ADDRESS	= 3;

	//Alarms
	public static final byte NUMBER_OF_ALARMS 		= 1,
							ALARMS_IDs				= 2,
							ALARM_SUMMARY_STATUS	= 3,
							ALARM_CONFIG			= 4,
							ALARM_STATUS			= 5,
							ALARM_DESCRIPTION 		= 6,
							ALARM_NAME				= 7;

	public static final byte PARAMETER_CONVERTER_DAC = 3;

	//************************************************************************************************************
	/**
	 * @param value
	 * @param bytePosition start from 0
	 * @return one byte 
	 */
	public static byte getByte(long value, int bytePosition){
		return (byte)(bytePosition>0 ? (value >> 8*bytePosition) : value);
	}

	public static byte[] concat(byte[] s, byte[] second) {

		byte[] result = null;

		if(s==null)
			result = second;
		else if(second==null)
			result = s;
		else{
			result = Arrays.copyOf(s, s.length + second.length);
			System.arraycopy(second, 0, result, s.length, second.length);
		}

		return result;
	}

	public static byte[] concatAll(byte[] first, byte[]... rest) { 
		  int totalLength = first.length; 

		  for (byte[] array : rest)
			  if(array!=null)
				  totalLength += array.length; 

		  byte[] result = Arrays.copyOf(first, totalLength); 
		  int offset = first.length; 

		  for (byte[] array : rest) { 
			  if(array!=null){
				  System.arraycopy(array, 0, result, offset, array.length); 
				  offset += array.length; 
			  }
		  } 
		  return result; 
		}

	public static long shiftAndAdd(byte[] toAdd) {
		long toShift = 0;

		if (toAdd != null)
			for (byte b : toAdd)
				toShift = shiftAndAdd(toShift, b);

		return toShift;
	}

	public static long shiftAndAdd(long toShift, byte toAdd) {
		long l = toAdd & 0xff;
		return (toShift<<8) ^ l;
	}

	public static <T> byte[] toBytes(T value) {
		byte[] bytes = null;

		if(value!=null){
			if(value instanceof Byte)
				bytes = new byte[]{(Byte) value};
			else if(value instanceof Short)
				bytes = shortToBytes((Short)value);
			else if(value instanceof Integer)
				bytes = intToBytes((Integer)value);
			else if(value instanceof Long)
				bytes = longToBytes((Long)value);
		}

		return bytes;
	}

	private static byte[] longToBytes(long value) {
		byte[] bs = new byte[8];

		bs[7] = (byte)	value;
		bs[6] = (byte) (value>>8);
		bs[5] = (byte) (value>>16);
		bs[4] = (byte) (value>>24);
		bs[3] = (byte) (value>>32);
		bs[2] = (byte) (value>>40);
		bs[1] = (byte) (value>>48);
		bs[0] = (byte) (value>>56);

		return bs;
	}

	private static byte[] intToBytes(int value) {
		byte[] bs = new byte[4];

		bs[3] = (byte)	value;
		bs[2] = (byte) (value>>8);
		bs[1] = (byte) (value>>16);
		bs[0] = (byte) (value>>24);

		return bs;
	}

	public static byte[] shortToBytes(short value) {
		byte[] bs = new byte[2];

		bs[1] = (byte)	value;
		bs[0] = (byte) (value>>8);

		return bs;
	}

	public static byte[] getPacketAsBytes(PacketType packetType, PacketId packetId, byte[] data) {

		byte[] id 	= shortToBytes(packetId.getValue());
		byte[] size = shortToBytes((short) (data==null ? 0 : data.length));
		
		byte[] bs = new byte[]{
				(byte) 254,
				0,
				0,
				0,
				packetType.getValue(),
				id[0],
				id[1],
				packetId.getPacketGroupId().getValue(),
				0,
				0,
				PacketErrors.NO_ERROR.getValue(),
				packetId.getParameterHeaderCode().getValue(),
				size[0],
				size[1]};
		bs = concat(bs, data);
		bs = PacketAbstract.preparePacket(bs);
		return bs;
	}
}