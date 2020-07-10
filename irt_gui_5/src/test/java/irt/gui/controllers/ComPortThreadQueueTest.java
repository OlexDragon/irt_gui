
package irt.gui.controllers;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Observable;
import java.util.Observer;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Test;

import irt.gui.data.packet.observable.InfoPacket;
import irt.gui.errors.PacketParsingException;
import jssc.SerialPort;
import jssc.SerialPortException;

public class ComPortThreadQueueTest {

	Logger logger = LogManager.getLogger();
	private LinkedPacketSender port  = new LinkedPacketSender(ComPortTest.COM_PORT);

	@Test
	public void test() throws InterruptedException, PacketParsingException {
		LinkedPacketsQueue queue = new LinkedPacketsQueue();

		try {

			port.openPort();

			queue.setComPort(port);
			InfoPacket packet = new InfoPacket();
			packet.addObserver(new Observer() {
				
				@Override
				public void update(Observable o, Object arg) {
					logger.trace(o);
					assertTrue(o instanceof InfoPacket);
				}
			});

			queue.add(packet, true);
			Thread.sleep(100);

			port.closePort();

			logger.trace(packet);
			assertNotNull(packet.getAnswer());

		} catch (SerialPortException e) {
			logger.catching(e);
		}
	}

	@After
	public void exit() {
		Optional.ofNullable(port).filter(SerialPort::isOpened).ifPresent(p -> {
			try {
				p.closePort();
			} catch (SerialPortException e) {
				logger.catching(e);
			}
		});
	}
}
