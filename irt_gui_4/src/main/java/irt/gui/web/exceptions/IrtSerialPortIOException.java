package irt.gui.web.exceptions;

import java.io.IOException;

public class IrtSerialPortIOException extends IOException {
	private static final long serialVersionUID = 9181671547534203001L;

	public IrtSerialPortIOException(String message) {
		super(message);
	}

	public IrtSerialPortIOException(String message, Throwable cause) {
		super(message, cause);
	}
}
