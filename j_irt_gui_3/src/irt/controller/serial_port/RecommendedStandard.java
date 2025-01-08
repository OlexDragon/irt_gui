package irt.controller.serial_port;

import java.util.Arrays;
import java.util.Optional;

public enum RecommendedStandard {
	RS232(1),
	RS422(2),
	RS485(3);

	private final int value;

	private RecommendedStandard(int value) {
		this.value = value;
		
	}

	public int getValue() {
		return value;
	}

	public static Optional<RecommendedStandard> valueOf(int value) {
		return Arrays.stream(RecommendedStandard.values()).parallel().filter(v->v.value==value).findAny();
	}
}