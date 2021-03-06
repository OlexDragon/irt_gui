
package irt.gui.data.packet.observable.configuration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import irt.gui.data.NetworkAddress;
import irt.gui.data.ToHex;
import irt.gui.data.packet.Packet;
import irt.gui.data.packet.enums.PacketId;
import irt.gui.data.packet.enums.PacketType;
import irt.gui.errors.PacketParsingException;

public class AttenuationPacketTest {

	Logger logger = LogManager.getLogger();

	@Test
	public void testRequest() throws PacketParsingException {
		AttenuationPacket packet = new AttenuationPacket();
		byte[] bytes = packet.toBytes();

		logger.trace("\n\t{}", ToHex.bytesToHex(bytes));
		logger.trace(packet);

		assertNotNull(bytes);

		byte[] packetAsBytes = Packet.getPacketAsBytes(PacketType.REQUEST, PacketId.CONFIGURATION_ATTENUATION, null);
		logger.trace("\n\t{}", ToHex.bytesToHex(packetAsBytes));

		assertTrue(Arrays.equals(packetAsBytes, bytes));
	}

	@Test
	public void testCommand() throws PacketParsingException {

		short value = (short) 378;

		AttenuationPacket packet = new AttenuationPacket(value);
		byte[] bytes = packet.toBytes();

		logger.trace("\n\t{}", ToHex.bytesToHex(bytes));
		logger.trace(packet);

		assertNotNull(bytes);

		byte[] packetAsBytes = Packet.getPacketAsBytes(PacketType.COMMAND, PacketId.CONFIGURATION_ATTENUATION, Packet.shortToBytes(value));
		logger.trace("\n\t{}", ToHex.bytesToHex(packetAsBytes));

		assertTrue(Arrays.equals(packetAsBytes, bytes));
	}

	@Test
	public void equalsTest() throws PacketParsingException{
		assertThat(new AttenuationPacket()							, is(new AttenuationPacket()));
		assertThat(new AttenuationRangePacket()					, is(new AttenuationRangePacket()));
		assertThat(new NetworkAddressPacket((NetworkAddress)null)	, is(new NetworkAddressPacket((NetworkAddress)null)));

		assertThat(new AttenuationPacket()							, not(new AttenuationRangePacket()));
		assertThat(new AttenuationPacket()							, not(new NetworkAddressPacket((NetworkAddress)null)));

		assertThat(new AttenuationRangePacket()					, not(new NetworkAddressPacket((NetworkAddress)null)));
	}
}
