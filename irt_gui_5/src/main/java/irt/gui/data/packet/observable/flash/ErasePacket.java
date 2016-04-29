package irt.gui.data.packet.observable.flash;

import irt.gui.controllers.flash.PanelFlash.Command;

public class ErasePacket extends AbstractFlashPacket{

	public ErasePacket() {
		super(Command.EXTENDED_ERASE);
	}
}
