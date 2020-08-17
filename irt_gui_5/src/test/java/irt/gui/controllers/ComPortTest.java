
package irt.gui.controllers;

import static org.junit.Assert.assertNotNull;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Test;

import irt.gui.data.packet.observable.InfoPacket;
import irt.gui.errors.PacketParsingException;
import jssc.SerialPortException;

public class ComPortTest implements Observer {

	public static final String COM_PORT = "COM3";
	Logger logger = LogManager.getLogger();
	private FutureTask<Boolean> task;
	private PacketSenderJssc port  = new PacketSenderJssc(ComPortTest.COM_PORT);

	@Test
	public void test() throws PacketParsingException {

		try {
			port.openPort();

			InfoPacket packet = new InfoPacket();
			packet.setLinkHeaderAddr((byte) 254);
			packet.addObserver(this);

			task = new FutureTask<>(()->true);

			logger.info("{}", packet.toBytes());
			port.send(packet);

			task.get(1, TimeUnit.SECONDS);

			logger.trace(packet);
			assertNotNull(packet.getAnswer());

			port.closePort();

		} catch (Exception e) {
			logger.catching(e);
		}finally{
			try {
				port.closePort();
			} catch (SerialPortException e) {
			}
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		task.run();
	}

	@After
	public void end() throws SerialPortException{
		port.closePort();
	}
}
