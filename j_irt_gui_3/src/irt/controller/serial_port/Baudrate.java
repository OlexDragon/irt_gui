package irt.controller.serial_port;

import jssc.SerialPort;

public enum Baudrate {
	BAUDRATE_9600	(SerialPort.BAUDRATE_9600),
	BAUDRATE_19200	(SerialPort.BAUDRATE_19200),
	BAUDRATE_38400	(SerialPort.BAUDRATE_38400),
	BAUDRATE_57600	(SerialPort.BAUDRATE_57600),
	BAUDRATE_115200	(SerialPort.BAUDRATE_115200);

	private static Baudrate DEFAULT_BAUDRATE = BAUDRATE_115200;
	private int value;

	private Baudrate(int baudrate){
		value = baudrate;
	}

	public int getValue() {
		return value;
	}

	@Override
	public String toString(){
		return Integer.toString(value);
	}

	public static Baudrate valueOf(int baudrate) {
		Baudrate result = null;

		for(Baudrate b:values())
			if(b.getValue()==baudrate){
				result = b;
				break;
			}

		return result;
	}

	public static Baudrate getDefaultBaudrate() {
		return DEFAULT_BAUDRATE;
	}

	public static void setDefaultBaudrate(Baudrate baudrate) {
		DEFAULT_BAUDRATE = baudrate;
	}

	
}
