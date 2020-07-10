
package irt.gui.data.packet.observable.configuration;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import irt.gui.data.ChecksumLinkedPacket;
import irt.gui.data.packet.enums.PacketId;
import irt.gui.errors.PacketParsingException;

public class MutePacketTest {
	Logger logger = LogManager.getLogger();

	@Test
	public void test() throws PacketParsingException {
		final MutePacket packet = new MutePacket();

		final byte[] bytes = new byte[]{(byte)0xFE, 0x00, 0x00, 0x00, 0x02, 0x00, (byte)PacketId.CONFIGURATION_MUTE.ordinal(), 0x02, 0x00, 0x00, 0x00, PacketId.CONFIGURATION_MUTE.getParameterHeaderCode().getValue(), 0x00, 0x00};
		final ChecksumLinkedPacket checksumLinkedPacket = new ChecksumLinkedPacket(bytes);
		final byte[] checksum = checksumLinkedPacket.toBytes();
		logger.trace("{}\nbytes: {}\nchecksumOf: {}", packet, bytes, checksum);

		assertEquals(PacketId.CONFIGURATION_MUTE, packet.getPacketId());
		final byte[] b = new byte[]{0x7E, bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7], bytes[8], bytes[9], bytes[10], bytes[11], bytes[12], bytes[13], checksum[0], checksum[1], 0x7E};
		assertArrayEquals(b, packet.toBytes());

		packet.setLinkHeaderAddr(MutePacket.CONVERTER_ADDR);
		logger.trace(packet);

		assertEquals(PacketId.CONFIGURATION_FCM_MUTE, packet.getPacketId());

		b[7] = (byte)PacketId.CONFIGURATION_FCM_MUTE.ordinal();
		b[12] = PacketId.CONFIGURATION_FCM_MUTE.getParameterHeaderCode().getValue();

		byte[] converter = new byte[14];
		converter[0] = 0x7E;
		System.arraycopy(b, 5, converter, 1, 13);
		final byte[] checksumConverter = new ChecksumLinkedPacket(Arrays.copyOfRange(converter, 1, 11)).toBytes();
		converter[11] = checksumConverter[0];
		converter[12] = checksumConverter[1];

		final byte[] result = packet.toBytes();
		logger.error("\n{}\n{}\n{}", b, converter, result);
		assertArrayEquals(converter, result);
	}

}
