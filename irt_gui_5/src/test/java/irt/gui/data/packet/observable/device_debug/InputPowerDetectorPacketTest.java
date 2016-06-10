
package irt.gui.data.packet.observable.device_debug;

import static org.junit.Assert.assertEquals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import irt.gui.IrtGuiProperties;
import irt.gui.data.packet.Payload;
import irt.gui.errors.PacketParsingException;

public class InputPowerDetectorPacketTest {
	Logger logger = LogManager.getLogger();

	@Test
	public void test() throws PacketParsingException {

		final InputPowerDetectorPacket packet = new InputPowerDetectorPacket();
		logger.trace(packet);

		Payload payload = packet.getPayloads().get(0);
		final int index = IrtGuiProperties.getLong("gui.label.register.p_det.1.index").intValue();
		final int addr = IrtGuiProperties.getLong("gui.label.register.p_det.1.addr").intValue();

		assertEquals(index, payload.getInt(0));
		assertEquals(addr, payload.getInt(1));

		packet.setLinkHeaderAddr((byte) -1);
		logger.trace(packet);

		payload = packet.getPayloads().get(0);
		assertEquals(IrtGuiProperties.getLong("gui.label.register.fcm.p_det.input.addr").intValue(), payload.getInt(0));
		assertEquals(IrtGuiProperties.getLong("gui.label.register.fcm.p_det.input.index").intValue(), payload.getInt(1));

		packet.setLinkHeaderAddr((byte) 254);
		logger.trace(packet);

		payload = packet.getPayloads().get(0);
		assertEquals(index, payload.getInt(0));
		assertEquals(addr, payload.getInt(1));

		packet.setLinkHeaderAddr((byte) 255);	// (byte)255 == -1 == converter
		logger.trace(packet);

		payload = packet.getPayloads().get(0);
		assertEquals(IrtGuiProperties.getLong("gui.label.register.fcm.p_det.input.addr").intValue(), payload.getInt(0));
		assertEquals(IrtGuiProperties.getLong("gui.label.register.fcm.p_det.input.index").intValue(), payload.getInt(1));
	}

}
