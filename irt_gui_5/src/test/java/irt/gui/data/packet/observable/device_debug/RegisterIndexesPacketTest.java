
package irt.gui.data.packet.observable.device_debug;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import irt.gui.data.ToHex;
import irt.gui.errors.PacketParsingException;

public class RegisterIndexesPacketTest {

	final Logger logger = LogManager.getLogger();

	@Test
	public void test() throws PacketParsingException {
		final RegisterIndexesPacket packet = new RegisterIndexesPacket();
		logger.trace("{}", ToHex.bytesToHex(packet.toBytes()));
	}

}
