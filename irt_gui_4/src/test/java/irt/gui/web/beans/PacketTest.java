package irt.gui.web.beans;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PacketTest {

	@Test
	void test() {
		Packet packet = new Packet(new byte[] {126, -2, 0, 0, 0, -1, 0, 0, -13, 91, 126, 126, -2, 0, 0, 0, 1, 0, 0, 8, 0, 0, 7, 106, -124, 126});
		assertEquals(packet.getPacketType(), PacketType.ACKNOWLEDGEMENT);
		System.out.println(packet);
		assertArrayEquals(new byte[] {126, -2, 0, 0, 0, -1, 0, 0, -13, 91, 126}, packet.getAcknowledgement());

		packet = new Packet(new byte[] {126, -2, 0, 0, 0, 1, 0, 0, 8, 0, 0, 7, 106, -124, 126});
		System.out.println(packet);
		assertArrayEquals(new byte[] {126, -2, 0, 0, 0, -1, 0, 0, -13, 91, 126}, packet.getAcknowledgement());
	}

}
