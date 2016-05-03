package irt.gui.data.packet.observable.flash;

import irt.gui.controllers.flash.enums.Command;

public class ReadPacket extends AbstractFlashPacket{

	public ReadPacket() {
		super(Command.READ_MEMORY);
	}
}
