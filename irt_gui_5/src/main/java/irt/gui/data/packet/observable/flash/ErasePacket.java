package irt.gui.data.packet.observable.flash;

import irt.gui.flash.PanelFlash.Command;

public class ErasePacket extends AbstractFlashPacket{

	public ErasePacket() {
		super(Command.EXTENDED_ERASE);
	}
}
