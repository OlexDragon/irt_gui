package irt.gui.web.beans;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

class PacketTest {
	private final static Logger logger = LogManager.getLogger();

	@Test
	void test() {
		Packet packet = new Packet(new byte[] {126, -2, 0, 0, 0, -1, 0, 0, -13, 91, 126, 126, -2, 0, 0, 0, 1, 0, 0, 8, 0, 0, 7, 106, -124, 126}, false);
		assertEquals(packet.getPacketType(), PacketType.ACKNOWLEDGEMENT);
		System.out.println(packet);
		assertArrayEquals(new byte[] {126, -2, 0, 0, 0, -1, 0, 0, -13, 91, 126}, packet.getAcknowledgement());

		packet = new Packet(new byte[] {126, -2, 0, 0, 0, 1, 0, 0, 8, 0, 0, 7, 106, -124, 126}, false);
		System.out.println(packet);
		assertArrayEquals(new byte[] {126, -2, 0, 0, 0, -1, 0, 0, -13, 91, 126}, packet.getAcknowledgement());
	}

	@Test
	void acknowledgementTest(){
		Packet packet = new Packet(new byte[] {126, -1, 15, 0, -9, -125, 126}, true);
		assertEquals(packet.getPacketType(), PacketType.ACKNOWLEDGEMENT);
		System.out.println(packet);
		byte[] acknowledgement = packet.getAcknowledgement();
		logger.error("acknowledgement: {} : {}" , acknowledgement.length, acknowledgement);
		assertArrayEquals(new byte[] {126, -1, 15, 0, -9, -125, 126}, acknowledgement);

		packet = new Packet(new byte[] {126, 1, 15, 0, 1, 0, 0, 0, 3, 0, 4, 32, 0, 69, -86, -103, -113, 126}, true);
		System.out.println(packet);
		byte[] acknowledgement2 = packet.getAcknowledgement();
		logger.error("acknowledgement2: {} : {}" , acknowledgement2.length, acknowledgement2);
		assertArrayEquals(new byte[] {126, -1, 15, 0, -9, -125, 126}, acknowledgement2);
	}
}
