package irt.gui.web.beans;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import irt.gui.web.controllers.UpgradeRestController;

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

	@Test
	void packetIdTest(){

		Packet packet = new Packet(new byte[] {126, -1, 15, 0, -9, -125, 126}, true);
		assertEquals(15, packet.getPacketId());
	}

	@Test
	void packetTypeTest() {

		Byte unitAddress = 1;
		final short packetId =  5555;
		final byte parameterId =  5;
		final byte[] value = new byte[] { 6, 7, 8, 9 };
		Packet packet = new Packet(unitAddress, PacketType.COMMAND, packetId, PacketGroupId.DEVICEINFO, parameterId, value);
		System.out.println(packet);
		assertEquals(PacketType.COMMAND, packet.getPacketType());
		final int length1 = packet.toBytes().length;

		packet = new Packet(null, PacketType.COMMAND, packetId, PacketGroupId.DEVICEINFO, parameterId, value);
		System.out.println(packet);
		assertEquals(PacketType.COMMAND, packet.getPacketType());
		final int length2 = packet.toBytes().length;
		assertTrue(length1>length2);
		assertEquals(length1 - Packet.LINK_HEADER_SIZE, length2);
	}

	@Test
	void packetIdTest2() {

        Byte unitAddress = 1;
        final short packetId =  5555;
        final byte parameterId =  5;
        final byte[] value = new byte[] { 6, 7, 8, 9 };

        Packet packet = new Packet(unitAddress, PacketType.COMMAND, packetId, PacketGroupId.DEVICEINFO, parameterId, value);
        System.out.println(packet);
        assertEquals(PacketType.COMMAND, packet.getPacketType());
        assertEquals(packetId, packet.getPacketId());

        packet = new Packet(null, PacketType.COMMAND, packetId, PacketGroupId.DEVICEINFO, parameterId, value);
        System.out.println(packet);
        assertEquals(PacketType.COMMAND, packet.getPacketType());
        assertEquals(packetId, packet.getPacketId());
	}

	@Test
	void packetChecksumTest() {
		Byte unitAddress = 1;
		PacketType.COMMAND.getCode();
		final short packetId = Packet.FLAG_SEQUENCE;
		final byte parameterId = 5;
		final byte[] value = new byte[] { 6, Packet.CONTROL_ESCAPE, 8, 9 };

		Packet packet = new Packet(unitAddress, PacketType.COMMAND, packetId, PacketGroupId.DEVICEINFO, parameterId, value);
		System.out.println(packet);
		final byte[] bytes = packet.toBytes();
		Checksum checksum = new Checksum(Arrays.copyOf(bytes, bytes.length - 2));
		assertEquals(checksum.get(), packet.getChecksumValue());

		packet = new Packet(null, PacketType.COMMAND, packetId, PacketGroupId.DEVICEINFO, parameterId, value);
		System.out.println(packet);
		final byte[] bytes2 = packet.toBytes();
		Checksum checksum2 = new Checksum(Arrays.copyOf(bytes2, bytes2.length - 2));
		assertEquals(checksum2.get(), packet.getChecksumValue());
	}

	@Test
	void packetsTest() {
		Byte unitAddress = 1;
		final short packetId = Packet.FLAG_SEQUENCE;
		final byte parameterId = 5;
		final byte[] value = new byte[] { 6, Packet.CONTROL_ESCAPE, 8, 9 };

		Packet packet1 = new Packet(unitAddress, PacketType.COMMAND, packetId, PacketGroupId.DEVICEINFO, parameterId, value);
		final byte[] send = packet1.toSend();
		final byte[] bytes = packet1.toBytes();
		assertNotEquals(send.length, bytes.length);
		System.out.println("toButes: " + Arrays.toString(bytes) + " toSend: " +  Arrays.toString(send));

		assertArrayEquals(Packet.controlEscape(bytes), Arrays.copyOfRange(send, 1, send.length -1));
		assertArrayEquals(bytes, Packet.byteStuffing(Arrays.copyOfRange(send, 1, send.length -1)));

		Packet packet2 = new Packet(send, false);
		assertEquals(packet1.getPacketType(), packet2.getPacketType());
		assertEquals(packet1.getPacketId(), packet2.getPacketId());
		assertArrayEquals(bytes, packet2.toBytes());

		Packet packet3 = new Packet((byte) 0, PacketType.COMMAND, packetId, PacketGroupId.DEVICEINFO, parameterId, value);
		Packet packet4 = new Packet(packet3.toSend(), true);
		assertEquals(packet3.getPacketType(), packet4.getPacketType());
		assertEquals(packet3.getPacketId(), packet4.getPacketId());
		assertArrayEquals(packet3.toBytes(), packet4.toBytes());
		assertArrayEquals(packet3.toSend(), packet4.toSend());
	}

	@Test
	void packetTest() {
		Packet packet = new Packet((byte) 55, PacketType.COMMAND, (short)1234, PacketGroupId.CONTROL, (byte) UpgradeRestController.PARAMETER_ID_UPGRADE_DATA, new byte[] {1,2,3,4,5});
		System.out.println(packet);
	}
}
