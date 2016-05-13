
package irt.gui.data.packet.observable.calibration;

import static org.junit.Assert.assertEquals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import irt.gui.controllers.calibration.tools.data.ToolsFrequency;

public class FrequencyPacketTest {
	private Logger logger = LogManager.getLogger();

	@Test
	public void getTest() {
		final byte[] packet = new ToolsFrequencyPacket().toBytes();
		final String actual = new String(packet);
		logger.trace(actual);
		assertEquals("FREQ:CW?\n", actual);
	}

	@Test
	public void setTest() {
		final ToolsFrequencyPacket packet = new ToolsFrequencyPacket();
		packet.getCommand().setValue(new ToolsFrequency("1 GHz"));
		final String actual = new String(packet.toBytes());
		logger.trace(actual);
		assertEquals("FREQ:CW 1000 MHZ\n", actual);
	}
}
