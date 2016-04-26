package irt.gui.data.packet.observable.flash;

import irt.gui.flash.PanelFlash.Command;

public class ReadPacket extends AbstractFlashPacket{

	public ReadPacket() {
		super(Command.READ_MEMORY);
	}
}
