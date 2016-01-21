
package irt.gui.data.packet.observable.configuration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import irt.gui.data.ToHex;
import irt.gui.data.packet.Packet;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketId;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketType;
import irt.gui.data.packet.observable.configuration.AttenuationRangePacket;
import irt.gui.errors.PacketParsingException;

public class AttenuationRangePackegeTest {

	Logger logger = LogManager.getLogger();

	@Test
	public void test() throws PacketParsingException {
		AttenuationRangePacket packege = new AttenuationRangePacket();
		byte[] bytes = packege.toBytes();
		logger.trace("\n\t{}", ToHex.bytesToHex(bytes));

		assertNotNull(bytes);

		byte[] packetAsBytes = Packet.getPacketAsBytes(PacketType.REQUEST, PacketId.CONFIGURATION_ATTENUATION_RANGE, null);
		logger.trace("\n\t{}", ToHex.bytesToHex(packetAsBytes));

		assertTrue(Arrays.equals(packetAsBytes, bytes));
	}

}
