
package irt.gui.controllers;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Observable;
import java.util.Observer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import irt.gui.data.packet.observable.InfoPacket;
import irt.gui.errors.PacketParsingException;
import jssc.SerialPortException;

public class ComPortThreadQueueTest {

	Logger logger = LogManager.getLogger();

	@Test
	public void test() throws InterruptedException, PacketParsingException {
		LinkedPacketsQueue queue = new LinkedPacketsQueue();
		LinkedPacketSender serialPort = new LinkedPacketSender(ComPortTest.COM_PORT);
		try {

			serialPort.openPort();

			queue.setComPort(serialPort);
			InfoPacket packet = new InfoPacket();
			packet.addObserver(new Observer() {
				
				@Override
				public void update(Observable o, Object arg) {
					logger.trace(o);
					assertTrue(o instanceof InfoPacket);
				}
			});

			queue.add(packet);
			Thread.sleep(100);

			serialPort.closePort();

			logger.trace(packet);
			assertNotNull(packet.getAnswer());

		} catch (SerialPortException e) {
			logger.catching(e);
		}
	}

}
