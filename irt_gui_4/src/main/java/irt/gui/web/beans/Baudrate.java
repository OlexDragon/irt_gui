package irt.gui.web.beans;

import java.util.Arrays;
import java.util.Optional;

public enum Baudrate {
	BAUDRATE_9600	(9600),
	BAUDRATE_19200	(19200),
	BAUDRATE_38400	(38400),
	BAUDRATE_57600	(57600),
	BAUDRATE_115200	(115200);

	private static Baudrate DEFAULT_BAUDRATE = BAUDRATE_115200;
	private long value;

	private Baudrate(long baudrate){
		value = baudrate;
	}

	public long getValue() {
		return value;
	}

	@Override
	public String toString(){
		return Long.toString(value);
	}

	public static Optional<Baudrate> valueOf(long baudrate) {
		return Arrays.stream(values()).parallel().filter(v->v.getValue()==baudrate).findAny();
	}

	public static Baudrate getDefaultBaudrate() {
		return DEFAULT_BAUDRATE;
	}

	public static void setDefaultBaudrate(Baudrate baudrate) {
		DEFAULT_BAUDRATE = baudrate;
	}

	
}
