package irt.gui.controllers.serial_port;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.observable.InfoPacket;

public class PacketSenderJSerialCommTest {
	private final Logger logger = LogManager.getLogger();

	private final IrtSerialPort irtSerialPort = new PacketSenderJSerialComm("COM3");
	@Test
	public void test() throws Exception {
		irtSerialPort.openSerialPort();

		final LinkedPacket infoPacket = new InfoPacket();
		infoPacket.setLinkHeaderAddr((byte) 0);
		logger.error(infoPacket);

		irtSerialPort.send(infoPacket);
		TimeUnit.SECONDS.sleep(5);
		final int availableBytes = irtSerialPort.getAvailableBytes();

		assertTrue(availableBytes>0);

		irtSerialPort.closeSerialPort();
	}

}
