
package irt.services;

import java.util.InputMismatchException;

public class ObjectToBoolean extends ObjectToAbstract<Boolean> {

	@Override
	public Boolean setValue(Object value) {

		//Value is null
		if(value==null)
			return super.setValue(null);

		//Do not need cast
		if(value instanceof Boolean)
			return super.setValue(value);

		//number to boolean
		if(value instanceof Number)
			return super.setValue(((Number)value).intValue() > 0);

		//String to boolean
		if(value instanceof String){
			final String v = ((String)value).replaceAll("\\D", "");

			if(v.length()>0)
				return super.setValue(Integer.parseInt(v) > 0);

			else
				return super.setValue(Boolean.valueOf(((String)value).toLowerCase()));

		}else if(value instanceof Character)
			return super.setValue(((Character)value - '0') > 0);

		//Prologix answer(byte[]) to boolean
		if(value instanceof byte[]){
			return super.setValue(((byte[])value)[0] - '0' > 0);
		}

		throw new InputMismatchException("The input type can be Boolean, String, Character, byte[] or Number, but it was " + value.getClass().getName());
	}

	@Override
	public String toPrologixCode() {

		final Boolean v = getValue();
		return v==null ? "" : (" " + (v ? '1' : '0'));
	}

}
