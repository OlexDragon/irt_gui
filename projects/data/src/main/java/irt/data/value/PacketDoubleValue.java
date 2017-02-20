
package irt.data.value;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Optional;

import irt.data.value.enumes.ValueStatus;
import irt.data.value.interfaces.PacketValue;

public class PacketDoubleValue implements PacketValue {

	private static final String NOT_AVAILABLE = "N/A";
	private final DecimalFormat format = new DecimalFormat("0.0");

	//	private final Boolean status; // true - over then value, false - under value, null - value is correct
	private final byte[] value;
	private final int divider;

	public PacketDoubleValue(byte[] value, int divider) {

		Optional.ofNullable(value).map(v->v.length).filter(l->l<=3).filter(l->l>=2).orElseThrow(()->new IllegalArgumentException("Array length is not correct: " + Arrays.toString(value)));
		if(divider<1) throw new IllegalArgumentException(String.format("Divider(%d) can not be less then 1", divider));

		this.value = value;
		this.divider = divider;
	}

	@Override
	public String toString() {
		return Optional.ofNullable(value).map(v->valueToString()).orElse(PacketDoubleValue.NOT_AVAILABLE);
	}

	private String valueToString() {
		String str = "";
		byte[] array = value;

		switch(value.length){

		case 3:

			ValueStatus status = ValueStatus.values()[value[0]&3];
			str = status.toString();

			array = Arrays.copyOfRange(value, 1, 3);

		case 2:

			ByteBuffer bb = ByteBuffer.wrap(array);
			final short v = bb.getShort();
			final double dv = (double)v/divider;
			str += format.format(dv);
			break;

		default:

			str = NOT_AVAILABLE;
		}

		return str;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = prime + divider;
		result = prime * result + Arrays.hashCode(value);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PacketDoubleValue other = (PacketDoubleValue) obj;
		if (divider != other.divider)
			return false;
		if (!Arrays.equals(value, other.value))
			return false;
		return true;
	}
}
