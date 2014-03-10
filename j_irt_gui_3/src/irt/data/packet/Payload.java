package irt.data.packet;

import irt.data.DacValue;
import irt.data.DeviceId;
import irt.data.RegisterValue;
import irt.data.StringData;
import irt.data.value.Value;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;


public class Payload {

	private static final Logger logger = (Logger) LogManager.getLogger();

	public static final byte 	DI_DEVICE_TYPE				= 1,
								DI_FIRMWARE_VERSION			= 2,
								DI_FIRMWARE_BUILD_DATE		= 3,
								DI_UNIT_UPTIME_COUNTER	= 4,
								DI_DEVICE_SN 				= 5,
								DI_UNIT_NAME				= 6,
								DI_UNIT_PART_NUMBER			= 7;

	public static final int PLL1 = 1;
	public static final int PLL2 = 2;
	public static final int MUTE = 1;

	public static final byte DEVICE_MESUREMENT_SUMMARY_ALARM_BITS= 1,	//Flags;
							DEVICE_MESUREMENT_STATUS_BITS		= 2,
							DEVICE_MESUREMENT_UNIT_TEMPERATURE	= 3,
							DEVICE_MESUREMENT_INPUT_POVER		= 4,
							DEVICE_MESUREMENT_OUTPUT_POVER		= 5,
							DEVICE_MESUREMENT_MONITOR_5V5		= 6,
							DEVICE_MESUREMENT_MONITOR_13V2_POS	= 7,
							DEVICE_MESUREMENT_MONITOR_13V2_NEG	= 8,
							DEVICE_MESUREMENT_MONITOR__CURRENT	= 9,
							DEVICE_MESUREMENT_SPU_TEMPERATURE	= 10;

	private ParameterHeader parameterHeader;
	private byte[] buffer;

	public Payload(ParameterHeader parameterHeader, byte[] buffer) {
		this.parameterHeader = parameterHeader;
		this.buffer = buffer;
	}

	public Payload() {
	}

	public byte[] setPayload(byte[] data){

		if(data!=null && data.length>=ParameterHeader.SIZE){
			parameterHeader = new ParameterHeader(data);
			int size = parameterHeader.getSize()+ParameterHeader.SIZE;
			if(size>ParameterHeader.SIZE && size<=data.length)
				buffer = Arrays.copyOfRange(data, ParameterHeader.SIZE, size);
		}
		int next = buffer!=null ? buffer.length+ParameterHeader.SIZE : 0;

		return next!=0 && next<data.length ? Arrays.copyOfRange(data, next, data.length) : null;
	}

	public StringData 		getStringData()		{ return new StringData(buffer);}
	public ParameterHeader	getParameterHeader(){ return parameterHeader;		}
	public byte[]			getBuffer()			{ return buffer;				}
	public byte[] 			getPayloadAsBytes()	{ return Packet.concat(parameterHeader.getParameterHeader(), buffer);}

	public byte	getByte() { return getByte(0);	}
	public byte getByte(int b) { return buffer!=null && buffer.length>b ? buffer[b] : 0;}
	public long	getLong() { return Packet.shiftAndAdd(buffer);	}
	public DeviceId getDeviceId() { return buffer!=null  ? new DeviceId(buffer) : null;	}

	public void setParameterHeader	(ParameterHeader parameterHeader)	{ this.parameterHeader= parameterHeader;			}

	public void setBuffer(byte[] buffer	){
		logger.trace("buffer={}", buffer);
		this.buffer = buffer;
		parameterHeader.setSize((short)(buffer!=null ?  buffer.length : 0));
	}
	public void setBuffer(byte value	)					{ setBuffer(new byte[]{value});			}
	public void setBuffer(short value	)					{ setBuffer(new byte[]{(byte) (value>>8),	(byte) value});		}
	public void setBuffer(int value		)					{ setBuffer(Packet.toBytes(value));		}
	public void setBuffer(long value	)					{ setBuffer(Packet.toBytes(value));		}
	public void setBuffer(byte dacNumber, short dacValue)				{ byte[] buffer = new byte[3];
																			buffer[0] = dacNumber;
																			byte[] b = Packet.toBytes(dacValue);
																			System.arraycopy(b, 0, buffer, 1, b.length);
																			setBuffer(buffer);}
	public void setBuffer(int index, int addr)	 						{	byte[] b = Arrays.copyOf(Packet.toBytes(index), 8);
																			System.arraycopy(Packet.toBytes(addr), 0, b, 4, 4);
																			setBuffer(b);}
	public void setBuffer(int index, int addr, int value) 				{	byte[] b = Arrays.copyOf(Packet.toBytes(index), 12);
																			System.arraycopy(Packet.toBytes(addr), 0, b, 4, 4);
																			System.arraycopy(Packet.toBytes(value), 0, b, 8, 4);
																			setBuffer(b);}
	public void setBuffer(Object value) 								{
		if(value!=null)
			switch(value.getClass().getSimpleName()){
			case "RegisterValue":
				RegisterValue r = (RegisterValue)value;
				Value v = r.getValue();
				if(v!=null)
					setBuffer(r.getIndex(), r.getAddr(), (int)v.getValue());
				else
					setBuffer(r.getIndex(), r.getAddr());
				break;
			case "DacValue":
				DacValue dv = (DacValue)value;
				setBuffer(dv.getDacNumber(),dv.getDacValue());
				break;
			case "Long":
				setBuffer((long)value);
				break;
			case "Integer":
				setBuffer((int)value);
				break;
			case "Short":
				setBuffer((short)value);
				break;
			case "Byte":
				setBuffer((byte)value);
				break;
			default:
				System.out.println("*TODO Payload setBuffer T value class neme - "+value.getClass().getSimpleName()+"="+value);
			}
		else
			setBuffer((byte[])null);
	}

	public boolean isFlag(int pll)	{ return (buffer[buffer.length-1] & pll)!=0;}
	public boolean isSet() 			{ return parameterHeader!=null;				}

	@Override
	public String toString() {
		return "Payload [" + parameterHeader + ", buffer="
				+ Arrays.toString(buffer) + "]";
	}

	public void add(int value) {
		byte[] v = Packet.toBytes(value);
		buffer = Packet.concat(buffer, v);
		parameterHeader.setSize((short)buffer.length);
	}

	public long getLong(byte firstByteOfLong) {
		long l = 0;

		int end = firstByteOfLong+8;
		if(end<=buffer.length)
			l = Packet.shiftAndAdd(Arrays.copyOfRange(buffer, firstByteOfLong, end));

		return l;
	}

	public long getLong(int index) {
		long l = 0;
		index *= 8;
		int end = index+8;

		if(end<=buffer.length)
			l = Packet.shiftAndAdd(Arrays.copyOfRange(buffer, index, end));

		return l;
	}

	/**
	 * @param startFrom = start from byte index = 'startFrom'*4
	 * @return	value from 4 bytes (started from 'startFrom'*4) */
	public int getInt(int startFrom) {
		return (short) toLong(startFrom*4, 4);
	}

	/**
	 * @param startFrom = start from byte with index = startFrom
	 * @return	value from 4 bytes (started from 'startFrom') */
	public int getInt(byte startFrom) {
		return (int) toLong(startFrom, 4);
	}

	/**
	 * @param startFrom = start from byte index = 'startFrom'*2
	 * @return	value from 2 bytes (started from 'startFrom'*2) */
	public short getShort(int startFrom) {
		return (short) toLong(startFrom*2, 2);
	}

	/**
	 * @param startFrom = start from byte with index = startFrom
	 * @return	value from 2 bytes (started from 'startFrom') */
	public short getShort(byte startFrom) {
		return (short) toLong(startFrom, 2);
	}

	private long toLong(int startFrom, int length){

		long result = 0;
		int end = startFrom+length;

		if(end<=buffer.length)
			result = Packet.shiftAndAdd(Arrays.copyOfRange(buffer, startFrom, end));

		return result;
	}

	public DacValue getDacValue() {
		DacValue dacValue = null;

		return dacValue;
	}

	public RegisterValue getRegisterValue() {
		return new RegisterValue( getInt(0), getInt(1), new Value(getInt(2)&Long.MAX_VALUE, 0 , Long.MAX_VALUE, 0));
	}

	public long[] getArrayLong() {
		long[] longs = null;

		if(buffer!=null && (buffer.length%8)==0){
			longs = new long[buffer.length/8];
			for(int i=0; i<longs.length; i++)
				longs[i] = getLong(i);
		}

		return longs;
	}

	public short[] getArrayShort() {
		short[] shorts = null;

		if(buffer!=null && (buffer.length%2)==0){
			shorts = new short[buffer.length/2];
			for(int i=0; i<shorts.length; i++)
				shorts[i] = getShort(i);
		}

		return shorts;
	}
}
