package irt.gui.controllers.enums;

import jssc.SerialPort;
import lombok.Getter;

@Getter
public enum Baudrate {

	BAUDRATE_9600	(SerialPort.BAUDRATE_9600),
	BAUDRATE_19200	(SerialPort.BAUDRATE_19200),
	BAUDRATE_38400	(SerialPort.BAUDRATE_38400),
	BAUDRATE_57600	(SerialPort.BAUDRATE_57600),
	BAUDRATE_115200	(SerialPort.BAUDRATE_115200);

	 private int value;

	private Baudrate(int baudrate){
		this.value = baudrate;
	}

	@Override
	public String toString(){
		return Integer.toString(value);
	}

	public static Baudrate valueOf(int baudrate) {
		Baudrate result = BAUDRATE_115200;

		for(Baudrate b:values())
			if(b.value==baudrate){
				result = b;
				break;
			}

		return result;
	}
}
