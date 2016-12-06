
package irt.serial.port.controllers;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import irt.packet.Packet;

public class PacketSenderTest {
	Logger logger = LogManager.getLogger();

	private byte[] bucPacket = new byte[]{0x7E, (byte) 0xFE, 00, 00, 00, 0x7E, 0x7E, (byte) 0xFE, 00, 00, 00, 02, 00, 00, 0x08, 00, 00, 00, (byte) 0xFF, 00, 00, (byte) 0xEC, (byte) 0xBE, 0x7E};
	private byte[] proligixPacket = new byte[]{ 1, 2, 3, 4, 5, 48, 13, 10};

	@Test
	public void getFlagSequencesTest() {
		final PacketSender ps = new PacketSender("COM1");

		assertTrue(ps.hasFlagSequences(bucPacket, new byte[]{Packet.FLAG_SEQUENCE}));
		assertFalse(ps.hasFlagSequences(Arrays.copyOf(bucPacket, bucPacket.length-1), new byte[]{Packet.FLAG_SEQUENCE}));

		assertTrue(ps.hasFlagSequences(proligixPacket, "\r\n".getBytes()));
		assertFalse(ps.hasFlagSequences(Arrays.copyOf(proligixPacket, proligixPacket.length-1), "\r\n".getBytes()));
	}

}
