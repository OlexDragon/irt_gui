
package irt.gui.controllers;

import static org.junit.Assert.assertNotNull;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import irt.gui.data.packet.observable.InfoPacket;
import irt.gui.errors.PacketParsingException;

public class ComPortTest implements Observer {

	public static final String COM_PORT = "COM13";
	Logger logger = LogManager.getLogger();
	private FutureTask<Boolean> task;

	@Test
	public void test() throws PacketParsingException {
		LinkedPacketSender comPort = new LinkedPacketSender(COM_PORT);
		try {

			comPort.openPort();

			InfoPacket packet = new InfoPacket();
			packet.addObserver(this);

			task = new FutureTask<>(()->true);

			comPort.send(packet);

			task.get(1, TimeUnit.SECONDS);

			logger.trace(packet);
			assertNotNull(packet.getAnswer());

			comPort.closePort();

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		task.run();
	}

}
