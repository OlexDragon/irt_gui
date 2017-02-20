
package irt.services;

import java.util.InputMismatchException;

import irt.data.prologix.Eos;
import irt.data.prologix.PrologixDeviceType;
import irt.services.ObjectToAbstract;

public class ObjectToEos extends ObjectToAbstract<Eos> {

	@Override
	public Eos setValue(Object value) {

		if(value == null)
			return super.setValue(value);

		if(value instanceof Eos)
			return super.setValue(value);

		int ordinal = -1;

		if(value instanceof Number)
			ordinal = ((Number)value).intValue();

		else if(value instanceof String){
			final String v = ((String)value).replaceAll("\\D", "");

			if(v.length()>0)
				ordinal = Integer.parseInt(v);

			else
				return super.setValue(Eos.valueOf(((String)value).trim().toUpperCase()));

		}else if(value instanceof Character)
			return super.setValue(PrologixDeviceType.values()[(Character)value - '0']);

		if(ordinal>=0)
			return super.setValue(Eos.values()[ordinal]);
	
		throw new InputMismatchException("The input type can be PrologixDeviceType, String, Character or Number, but it was: " + value.getClass().getName());
	}

	@Override
	public String toPrologixCode() {

		final Eos v = getValue();
		return v == null ? "" : " " + v.ordinal();
	}

	@Override
	public Eos getValue(byte... bs) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

}
