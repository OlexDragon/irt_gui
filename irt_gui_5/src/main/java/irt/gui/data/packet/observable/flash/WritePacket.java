package irt.gui.data.packet.observable.flash;

import irt.gui.controllers.flash.PanelFlash.Command;

public class WritePacket extends AbstractFlashPacket{

	public WritePacket() {
		super(Command.WRITE_MEMORY);
	}
}
