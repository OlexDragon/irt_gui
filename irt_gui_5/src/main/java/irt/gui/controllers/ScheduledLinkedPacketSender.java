
package irt.gui.controllers;

import irt.gui.controllers.leftside.setup.SerialPortController;
import irt.gui.data.packet.interfaces.LinkedPacket;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;

public class ScheduledLinkedPacketSender extends ScheduledService<Void> {

	private final LinkedPacket linkedPacket;

	public ScheduledLinkedPacketSender(LinkedPacket linkedPacket, Duration period) {

		this.linkedPacket = linkedPacket;
		setPeriod(period);
		setRestartOnFailure(true);
	}

	@Override
	protected Task<Void> createTask() {
		return new Task<Void>() {

			@Override
			protected Void call() throws Exception {

				SerialPortController.QUEUE.add(linkedPacket);
				return null;
			}
		};
	}

}
