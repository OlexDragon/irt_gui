package irt.controller.serial_port;

import java.util.Arrays;
import java.util.Optional;

public enum RecommendedStandard {
	UNKNOWN(null),
//	RS232(1),
	RS422(2),
	RS485(3);

	private final Integer value;

	private RecommendedStandard(Integer value) {
		this.value = value;
		
	}

	public Integer getValue() {
		return value;
	}

	public static Optional<RecommendedStandard> valueOf(int value) {
		return Arrays.stream(RecommendedStandard.values()).parallel().filter(v->v.value!=null).filter(v->v.value==value).findAny();
	}
}