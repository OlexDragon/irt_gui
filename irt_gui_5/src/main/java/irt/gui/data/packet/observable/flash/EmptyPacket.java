package irt.gui.data.packet.observable.flash;

import irt.gui.flash.PanelFlash.Command;

public class EmptyPacket extends AbstractFlashPacket{

	public EmptyPacket() {
		super(Command.EMPTY);
	}
}
