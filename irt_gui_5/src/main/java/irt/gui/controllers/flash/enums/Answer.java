package irt.gui.controllers.flash.enums;

import java.util.Arrays;
import java.util.Optional;

import irt.gui.data.ToHex;

public enum Answer {
	UNKNOWN	((byte) -1),
	NULL	((byte) 0),
	ACK		((byte) 0x79),
	NACK	((byte) 0x1F);

	private final byte answer;

	private Answer(byte answer) {
		this.answer = answer;
	}

	public byte getAnswer() {
		return answer;
	}

	public static Optional<Answer> valueOf(byte key){
		return Arrays
				.stream(values())
				.filter(a->a.answer==key)
				.findAny();
	}

	@Override
	public String toString() {
		return name()+" (0x"+ToHex.bytesToHex(answer)+ ")";
	}
}
