package irt.gui.web.exceptions;

public class IrtSerialPortRTException extends RuntimeException {
	private static final long serialVersionUID = -5155102827475583947L;

	public IrtSerialPortRTException(String message) {
		super(message);
	}

	public IrtSerialPortRTException(String message, Throwable cause) {
		super(message, cause);
	}
}
