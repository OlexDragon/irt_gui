package irt.gui.data.packet.observable.flash;

import irt.gui.controllers.LinkedPacketSender;
import irt.gui.controllers.flash.enums.Command;
import irt.gui.controllers.interfaces.WaitTime;

public class ConnectPacket extends AbstractFlashPacket {

	public ConnectPacket() {
		super(Command.CONNECT);
	}
}
