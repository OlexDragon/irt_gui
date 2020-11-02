package irt.gui.controllers;

import irt.gui.controllers.enums.Baudrate;
import jssc.SerialPort;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SerialPortParams {

	private Baudrate baudrate = Baudrate.BAUDRATE_115200;
	private int dataBits = SerialPort.DATABITS_8;
	private int stopBits = SerialPort.STOPBITS_1;
	private int parity = SerialPort.PARITY_NONE;
}
