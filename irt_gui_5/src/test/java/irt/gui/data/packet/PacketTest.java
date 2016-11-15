
package irt.gui.data.packet;

import static org.junit.Assert.assertEquals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.observable.configuration.AttenuationPacket;
import irt.gui.data.packet.observable.configuration.FrequencyPacket;
import irt.gui.errors.PacketParsingException;

public class PacketTest {
	private final Logger logger = LogManager.getLogger();

	@Test
	public void createNewPacketTest1() throws PacketParsingException {
		logger.traceEntry();

		AttenuationPacket expecteds = new AttenuationPacket();
		LinkedPacket actuals = (LinkedPacket) Packet.createNewPacket(AttenuationPacket.class, (Object[])null);

		logger.trace("\n expecteds:{}\n actuals{}", expecteds, actuals);
		assertEquals(expecteds, actuals);
	}

	@Test
	public void createNewPacketTest2() throws PacketParsingException {
		logger.traceEntry();

		FrequencyPacket expecteds = new FrequencyPacket();
		LinkedPacket actuals = (LinkedPacket) Packet.createNewPacket(FrequencyPacket.class, (Object[])null);

		logger.trace("\n expecteds:{}\n actuals{}", expecteds, actuals);
		assertEquals(expecteds, actuals);
	}

	@Test
	public void createNewPacketTest3() throws PacketParsingException {
		logger.traceEntry();

		FrequencyPacket expecteds = new FrequencyPacket(2000000000L);
		LinkedPacket actuals = (LinkedPacket) Packet.createNewPacket(FrequencyPacket.class, 2000000000L);

		logger.trace("\n expecteds:{}\n actuals{}", expecteds, actuals);
		assertEquals(expecteds, actuals);
	}

	@Test
	public void createNewPacketTest4() throws PacketParsingException {
		logger.traceEntry();

		final byte[] answer = new byte[]{(byte)0x7E, (byte)0xFE, 0x00, 0x00, 0x00, 0x03, 0x00, 0x0B, 0x02, 0x00, 0x00, 0x00, 0x08, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x77, 0x35, (byte)0x94, 0x00, 0x17, 0x5A, (byte)0x7E};
		FrequencyPacket expecteds = new FrequencyPacket(answer, false);
		LinkedPacket actuals = (LinkedPacket) Packet.createNewPacket(FrequencyPacket.class, answer, false);

		logger.trace("\n expecteds:{}\n actuals{}", expecteds, actuals);
		assertEquals(expecteds, actuals);
	}

	@Test
	public void createNewPacketTest5() throws PacketParsingException {
		logger.traceEntry();

		final byte[] answer = new byte[]{(byte)0x7E, (byte)0xFE, 0x00, 0x00, 0x00, 0x03, 0x00, 0x0B, 0x02, 0x00, 0x00, 0x00, 0x08, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x77, 0x35, (byte)0x94, 0x00, 0x17, 0x5A, (byte)0x7E};
		FrequencyPacket expecteds = new FrequencyPacket(answer, false);
		LinkedPacket actuals = (LinkedPacket) Packet.createNewPacket(FrequencyPacket.class, answer, false);

		logger.trace("\n expecteds:{}\n actuals{}", expecteds, actuals);
		assertEquals(expecteds, actuals);
	}
}
