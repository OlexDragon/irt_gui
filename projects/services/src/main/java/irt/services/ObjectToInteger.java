
package irt.services;

import java.util.InputMismatchException;
import java.util.Optional;

public class ObjectToInteger extends ObjectToAbstract<Integer> {

	private final int min;
	private final int max;

	public ObjectToInteger(int min, int max) {
		this.min = min;
		this.max = max;
	}

	public ObjectToInteger() {
		this(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Override
	public Integer setValue(Object value) {
		if(value == null)
			return super.setValue(value);

		if(value instanceof Number) {
			final int intValue = ((Number)value).intValue();

			if(intValue<min || intValue>max)
				throw new InputMismatchException("Value '" + value + "' is out of ranger (" + min + "-" + max + ")"); 

			return super.setValue(intValue);
		}

		if(value instanceof String){
			final String v = ((String)value).replaceAll("\\D", "");

			if(v.length()>0) {
				final int parseInt = Integer.parseInt(v);

				if(parseInt<min || parseInt>max)
					throw new InputMismatchException("Value '" + value + "' is out of ranger"); 

				return super.setValue(parseInt);

			}else
				return super.setValue(null);
		}

		throw new InputMismatchException("The input does not contain numbers: " + value);
	}

	@Override
	public String toPrologixCode() {

		final Integer v = getValue();
		return v ==null ? "" : (" " + v);
	}

	@Override
	public String toString() {
		return "ObjectToInteger [min=" + min + ", max=" + max + ", getValue()=" + getValue() + "]";
	}

	@Override
	public Integer getValue(byte... bs) {
		return Optional
				.ofNullable(bs)
				.map(String::new)
				.map(str->str.replaceAll("\\D", ""))
				.filter(str->!str.isEmpty())
				.map(Integer::parseInt)
				.orElseThrow(()->new IllegalArgumentException(new String(bs)));
	}

}
