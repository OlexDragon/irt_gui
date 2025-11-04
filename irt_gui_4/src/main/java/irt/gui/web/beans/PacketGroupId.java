package irt.gui.web.beans;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @Getter
public enum PacketGroupId {
	ALARM		 ((byte) 1),
	CONFIGURATION((byte) 2),
	FILETRANSFER ((byte) 3),
	MEASUREMENT	 ((byte) 4),
	RESET		 ((byte) 5),
	DEVICEINFO	 ((byte) 8),
	CONTROL		 ((byte) 9),
	PROTOCOL	 ((byte) 10),
	NETWORK		 ((byte) 11),
	REDUNDANCY	 ((byte) 12),
	DEVICEDEBUG	 ((byte) 61),
	PRODUCTION	 ((byte) 100),
	DEVELOPER	 ((byte) 120);

	private final byte id;
}
