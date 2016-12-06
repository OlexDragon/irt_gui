package irt.services;

import javafx.util.converter.IntegerStringConverter;

public class AddressIntegerStringConverter extends IntegerStringConverter {

	public static final String CONVERTER = "Converter";

	@Override
	public String toString(Integer value) {
		if(value!=null && value<0)
			return CONVERTER;

		return super.toString(value);
	}

	@Override
	public Integer fromString(String value) {
		if(value!=null && value.equals(CONVERTER))
			return -1;

		return super.fromString(value);
	}

}
