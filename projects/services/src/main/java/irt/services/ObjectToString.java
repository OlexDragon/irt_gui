
package irt.services;

import java.util.Optional;

public class ObjectToString extends ObjectToAbstract<String> {

	@Override
	public String setValue(Object value) {

		//Value is null
		if(value==null)
			return super.setValue(null);

		//number to byte[]
		if(value instanceof byte[])
			return super.setValue(new String((byte[])value));

		if(value instanceof String && ((String)value).length()==0)
			return super.setValue(null);

		return super.setValue(value.toString());
	}

	@Override
	public String toPrologixCode() {

		final String value = getValue();
		return value==null ? "" : value;
	}

	@Override
	public String getValue(byte... bs) {
		return Optional
				.ofNullable(bs)
				.map(String::new)
				.map(String::trim)
				.orElse(null);
	}

}
