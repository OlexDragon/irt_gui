package irt.gui.web.beans;

import java.util.Arrays;
import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor @Getter @ToString
public enum PacketError {
	CORRUPTED_PACKAGE		(-2, "Packet ERROR: Corrupted packet"),
	UNKNOWN					(-1, "Unknown Packet ERROR"),
	NO_ERROR				(0, "No Error (0)"),
	INTERNAL_ERROR			(1, "Packet ERROR (1): Internal System Error"),
	WRITE_ERROR				(2, "Packet ERROR (2): Write Error"),
	FUNCTION_NOT_IMPLEMENTED(3, "Packet ERROR (3): Function not implemented"),
	NOT_IN_RANGE			(4, "Packet ERROR (4): Value outside of valid range"),
	CAN_NOT_GENERATE		(5, "Packet ERROR (5): Requested information can’t be generated"),
	CAN_NOT_EXECUTE			(6, "Packet ERROR (6): Command can’t be executed"),
	INVALID_FORMAT			(7, "Packet ERROR (7): Invalid data format"),
	INVALID_VALUE			(8, "Packet ERROR (8): Invalid value"),
	NO_MEMORY				(9, "Packet ERROR (9): Not enough memory"),
	NOT_FOUNDR				(10, "Packet ERROR (10): Requested element not foundr"),
	TIMED_OUT				(11, "Packet ERROR (11): Timed out"),
	NO_COMMUNICATION		(20, "Packet ERROR (20): Communication problem");

	private final int code;
	private final String description;

	public static PacketError  valueByCode(Byte code) {

		return Optional.ofNullable(code)
				.map(
						c->{
							final int intVal = c&0xff;
							return Arrays.stream(values()).filter(pe->pe.code==intVal).findAny().orElse(UNKNOWN);
						}).orElse(CORRUPTED_PACKAGE);
	}
}
