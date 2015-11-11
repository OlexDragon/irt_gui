
package irt.gui.controllers;

import static org.junit.Assert.assertNotNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import irt.gui.data.packet.observable.InfoPacket;
import irt.gui.errors.PacketParsingException;
import jssc.SerialPortException;

public class ComPortTest {

	public static final String COM_PORT = "COM2";
	Logger logger = LogManager.getLogger();

	@Test
	public void test() throws PacketParsingException {
		LinkedPacketSender comPort = new LinkedPacketSender(COM_PORT);
		try {

			comPort.openPort();

			InfoPacket packet = new InfoPacket();
			comPort.send(packet);
			assertNotNull(packet.getAnswer());

			comPort.closePort();

		} catch (SerialPortException e) {
			logger.catching(e);
		}
	}

}
