package irt.gui.data.packet.observable.flash;

import irt.gui.controllers.LinkedPacketSender;
import irt.gui.controllers.flash.PanelFlash.Command;
import irt.gui.controllers.interfaces.WaitTime;

public class ConnectPacket extends AbstractFlashPacket implements WaitTime{

	public ConnectPacket() {
		super(Command.CONNECT);
	}

	@Override
	public int getWaitTime() {
		return LinkedPacketSender.FLASH_MEMORY_WAIT_TIME;
	}
}
