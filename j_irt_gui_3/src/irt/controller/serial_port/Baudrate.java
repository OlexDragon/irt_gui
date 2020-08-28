package irt.controller.serial_port;

import jssc.SerialPort;

public enum Baudrate {
	BAUDRATE_9600	(SerialPort.BAUDRATE_9600),
	BAUDRATE_19200	(SerialPort.BAUDRATE_19200),
	BAUDRATE_38400	(SerialPort.BAUDRATE_38400),
	BAUDRATE_57600	(SerialPort.BAUDRATE_57600),
	BAUDRATE_115200	(SerialPort.BAUDRATE_115200);

	private int baudrate;

	private Baudrate(int baudrate){
		this.baudrate = baudrate;
	}

	public int getBaudrate() {
		return baudrate;
	}

	@Override
	public String toString(){
		return Integer.toString(baudrate);
	}

	public static Baudrate valueOf(int baudrate) {
		Baudrate result = null;

		for(Baudrate b:values())
			if(b.getBaudrate()==baudrate){
				result = b;
				break;
			}

		return result;
	}
}
