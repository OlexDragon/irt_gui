package irt.gui.data.packet.observable.flash;

import irt.gui.flash.PanelFlash.Command;

public class ConnectPacket extends AbstractFlashPacket{

	public ConnectPacket() {
		super(Command.CONNECT);
	}
}
