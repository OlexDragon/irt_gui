package irt.tools.fx.update.profile.table;

import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

public class TableEntry {

	private final Number key;
	private final Number value;

	public TableEntry(String key, String value) {
		this(Optional.ofNullable(key).map(str->str.replaceAll("\\D", "")).map(parse()).orElse(null), Optional.ofNullable(value).map(str->str.replaceAll("\\D", "")).map(parse()).orElse(null));
	}

	public TableEntry(Number key, Number value) {
		this.key = key;
		this.value = value;
	}

	public Number getKey() {
		return key;
	}

	public Number getValue() {
		return value;
	}

	public boolean isValid() {
		return key!=null && value!=null;
	}

	private static Function<String, Number> parse() {
		return str->{
			return Pattern.matches("-*([0-9]*)\\.([0-9]*)", str) ? Double.parseDouble(str) : Long.parseLong(str);
		};
	}
}
