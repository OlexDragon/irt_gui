package irt.packet;

public class PacketParsingException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public PacketParsingException(String message) {
		super(message);
	}
}
