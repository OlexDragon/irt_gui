package irt.gui.data.packet.observable.flash;

import irt.gui.controllers.flash.enums.Command;

public class ConnectPacket extends AbstractFlashPacket {

	public ConnectPacket() {
		super(Command.CONNECT);
	}
}
