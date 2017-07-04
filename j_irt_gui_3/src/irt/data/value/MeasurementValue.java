
package irt.data.value;

import irt.data.packet.ParameterHeader;
import irt.data.packet.Payload;

public class MeasurementValue {

	private final int value;
	private final MeasurementStatus flags;

	public  MeasurementValue(Payload payload){

		ParameterHeader parameterHeader = payload.getParameterHeader();

		switch(parameterHeader.getSize()){
		case 2:
			flags = MeasurementStatus.UNKNOWN;
			value = payload.getShort(0);
			break;
		case 3:
			flags = MeasurementStatus.values()[(byte) (payload.getByte()&0x03)];
			value =  payload.getShort((byte)1);
			break;
		case 4:
			flags = MeasurementStatus.UNKNOWN;
			value = payload.getInt(0);
			break;
		default:
			flags = MeasurementStatus.UNKNOWN;
			value = Integer.MIN_VALUE;
		}
	}

	public int getValue() {
		return value;
	}

	public MeasurementStatus getFlags() {
		return flags;
	}

	@Override
	public String toString() {
		return "MeasurementValue [value=" + value + ", flags=" + flags + "]";
	}

	public enum MeasurementStatus{
		UNKNOWN,
		REAL_VALUE,
		LESS_THAN,
		MORE_THAN;
	}
}
