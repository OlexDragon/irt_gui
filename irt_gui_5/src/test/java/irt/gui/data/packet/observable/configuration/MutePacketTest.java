
package irt.gui.data.packet.observable.configuration;

import static org.junit.Assert.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import irt.gui.data.packet.enums.PacketId;
import irt.gui.data.packet.observable.PacketAbstract5;
import irt.gui.errors.PacketParsingException;

public class MutePacketTest {
	Logger logger = LogManager.getLogger();

	@Test
	public void test() throws PacketParsingException {
		final MutePacket packet = new MutePacket();
		logger.trace(packet);

		assertArrayEquals(new byte[]{0x7E, (byte)0xFE, 0x00, 0x00, 0x00, 0x02, 0x00, 0x0F, 0x02, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x42, (byte)0x95, 0x7E}, packet.toBytes());
		assertEquals(PacketId.CONFIGURATION_MUTE, packet.getPacketId());

		packet.setLinkHeaderAddr(PacketAbstract5.CONVERTER_ADDR);
		logger.trace(packet);

		//7E 02 00 10 02 00 00 00 07 00 00 F0 DB 7E
		assertArrayEquals(new byte[]{0x7E, 0x02, 0x00, 0x10, 0x02, 0x00, 0x00, 0x00, 0x07, 0x00, 0x00, (byte) 0xF0, (byte) 0xDB, 0x7E}, packet.toBytes());
		assertEquals(PacketId.CONFIGURATION_FCM_MUTE, packet.getPacketId());
	}

}
