
package irt.gui.data.packet.observable.configuration;

import static org.junit.Assert.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import irt.gui.data.packet.enums.PacketId;
import irt.gui.errors.PacketParsingException;

public class MutePacketTest {
	Logger logger = LogManager.getLogger();

	@Test
	public void test() throws PacketParsingException {
		final MutePacket packet = new MutePacket();
		logger.trace(packet);

		assertArrayEquals(new byte[]{0x7E, (byte)0xFE, 0x00, 0x00, 0x00, 0x02, 0x00, 0x0F, 0x02, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x42, (byte)0x95, 0x7E}, packet.toBytes());
		assertEquals(PacketId.CONFIGURATION_MUTE, packet.getPacketId());

		packet.setLinkHeaderAddr((byte) -1);
		logger.trace(packet);

		assertArrayEquals(new byte[]{0x7E, 0x02, 0x00, 0x0F, 0x02, 0x00, 0x00, 0x00, 0x07, 0x00, 0x00, 0x3A, 0x31, 0x7E}, packet.toBytes());
		assertEquals(PacketId.CONFIGURATION_FCM_MUTE, packet.getPacketId());
	}

}
