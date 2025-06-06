package irt.gui.web.exceptions;

public class IrtSerialPortTOException extends RuntimeException {
	private static final long serialVersionUID = -5155102827475583947L;

	public IrtSerialPortTOException(String message) {
		super(message);
	}

	public IrtSerialPortTOException(String message, Throwable cause) {
		super(message, cause);
	}
}
