package irt.gui.data.packet.observable.flash;

import irt.gui.flash.PanelFlash.Command;

public class WritePacket extends AbstractFlashPacket{

	public WritePacket() {
		super(Command.WRITE_MEMORY);
	}
}
