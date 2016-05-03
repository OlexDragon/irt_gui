package irt.gui.data.packet.observable.flash;

import irt.gui.controllers.flash.enums.Command;

public class EmptyPacket extends AbstractFlashPacket{

	public EmptyPacket() {
		super(Command.EMPTY);
	}
}
