
package irt.gui.data.packet.observable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import irt.gui.data.ChecksumLinkedPacket;
import irt.gui.data.ToHex;
import irt.gui.data.packet.Packet;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketId;
import irt.gui.errors.PacketParsingException;

public class PacketAbstractTest {

	Logger logger = LogManager.getLogger();

	@Test
	public void testParameterNull() {
		try {

			new PacketAbstract(PacketId.DEVICE_INFO, null, true){

				@Override
				public PacketId getPacketId() {
					throw new UnsupportedOperationException("Auto-generated method stub");
				}};
			throw new PacketParsingException(null);

		} catch (PacketParsingException e) {
			logger.trace(e.getLocalizedMessage());
			assertEquals("\n\t Constructor parameter can not be null or empty.", e.getLocalizedMessage());
		}
	}

	@Test
	public void testParameterIsNotCorrect() {
		try {

			new PacketAbstract(PacketId.DEVICE_INFO, new byte[]{}, true){

				@Override
				public PacketId getPacketId() {
					throw new UnsupportedOperationException("Auto-generated method stub");
				}};
			throw new PacketParsingException(null);

		} catch (PacketParsingException e) {
			logger.trace(e.getLocalizedMessage());
			assertTrue(e.getLocalizedMessage().contains("\n\t Constructor parameter can not be null or empty."));
		}
	}

	@Test
	public void testParameterIsNotCorrect2(){
		try {

			new PacketAbstract(PacketId.DEVICE_INFO, new byte[]{1}, true){

				@Override
				public PacketId getPacketId() {
					throw new UnsupportedOperationException("Auto-generated method stub");
				}};
			throw new PacketParsingException(null);

		} catch (PacketParsingException e) {
			logger.trace(e.getLocalizedMessage());
		assertTrue(e.getLocalizedMessage().contains("\n\t The Packet structure is not correct:\n\t Das not has Packet.FLAG_SEQUENCE : 126(0x7E)\n\t"));
		}
	}

	@Test
	public void testParameterIsNotCorrect3() {
		try {

			new PacketAbstract(PacketId.DEVICE_INFO, new byte[]{126}, true){

				@Override
				public PacketId getPacketId() {
					throw new UnsupportedOperationException("Auto-generated method stub");
				}};
			throw new PacketParsingException(null);

		} catch (PacketParsingException e) {
			logger.trace(e.getLocalizedMessage());
			assertTrue(e.getLocalizedMessage().contains("\n\t The Packet structure is not correct:\n\t Das not has second Packet.FLAG_SEQUENCE : 126(0x7E)\n\t"));
		}
	}

	@Test
	public void testParameterIsNotCorrect4() {
		try {

			new PacketAbstract(PacketId.DEVICE_INFO, new byte[]{0x7E, (byte)0xFE, 0x00, 0x00, 0x00, (byte)0xFF, 0x00, 0x03, 0x68, 0x69, 0x7E}, true){

				@Override
				public PacketId getPacketId() {
					throw new UnsupportedOperationException("Auto-generated method stub");
				}};
			throw new PacketParsingException(null);

		} catch (PacketParsingException e) {
			logger.error(e.getLocalizedMessage());
			assertTrue(e.getLocalizedMessage().contains("\n\t The Packet structure is not correct:") && e.getLocalizedMessage().contains("NO Packet.FLAG_SEQUENCE : 126(0x7E)"));
		}
	}

	@Test
	public void testParameterIsNotCorrect5() {
		try {

			new PacketAbstract(PacketId.DEVICE_INFO, new byte[]{0x7E, (byte)0xFE, 0x00, 0x00, 0x00, (byte)0xFF, 0x00, 0x03, 0x68, 0x69, 0x7E, 0x7E}, true){

				@Override
				public PacketId getPacketId() {
					throw new UnsupportedOperationException("Auto-generated method stub");
				}};
			throw new PacketParsingException(null);

		} catch (PacketParsingException e) {
			logger.trace(e.getLocalizedMessage());
			assertTrue(e.getLocalizedMessage().contains("\n\t The Packet structure is not correct:") && e.getLocalizedMessage().contains("NO second Packet.FLAG_SEQUENCE : 126(0x7E)"));
		}
	}

	@Test
	public void testParameterIsNotCorrect6() {
		try {

			new PacketAbstract(PacketId.DEVICE_INFO, new byte[]{0x7E, (byte)0xFE, 0x00, 0x00, 0x00, (byte)0xFF, 0x00, 0x03, 0x68, 0x69, 0x7E, 0x7E, 0x7E}, true){

				@Override
				public PacketId getPacketId() {
					throw new UnsupportedOperationException("Auto-generated method stub");
				}};
			throw new PacketParsingException(null);

		} catch (PacketParsingException e) {
			logger.trace(e.getLocalizedMessage());
			assertTrue(e.getLocalizedMessage().contains("\n\t The Packet structure is not correct:"));
		}
	}

	@Test
	public void testChecksummIsNotCorrect() {
		try {

			new PacketAbstract(PacketId.DEVICE_INFO, new byte[]{0x7E, (byte)0xFE, 0x00, 0x00, 0x00, (byte)0xFF, 0x00, 0x14, 0x56, 0x0D, 0x7E, 0x7E, (byte)0xFE, 0x00, 0x00, 0x00, 0x01, 0x00, 0x14, 0x02, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x02, 0x16, 0x7E}, true){

				@Override
				public PacketId getPacketId() {
					throw new UnsupportedOperationException("Auto-generated method stub");
				}};

			throw new IllegalStateException("This function have to throw PacketParsingException") ;

		} catch (PacketParsingException e) {
			logger.trace(e.getLocalizedMessage());
			assertTrue(e.getLocalizedMessage().contains("checksum is not correct"));
		}
	}

	@Test
	public void testPacketAbstract() throws PacketParsingException {

		new PacketAbstract(PacketId.DEVICE_INFO, new byte[]{0x7E, (byte)0xFE, 0x00, 0x00, 0x00, (byte)0xFF, 0x00, 0x0, (byte)0xF3, 0x5B, 0x7E, 0x7E, (byte)0xFE, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x02, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x02, (byte)0x9A, (byte)0xAB, 0x7E}, true){

			@Override
			public PacketId getPacketId() {
				throw new UnsupportedOperationException("Auto-generated method stub");
			}};
	}

	@Test
	public void testPreparePacket() throws PacketParsingException {
		final byte[] readBytes = new byte[]{(byte)0xFE, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x3D, 0x00, 0x00, 0x00, 0x03, 0x00, 0x0C, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x5F};
		final byte[] byteStuffing = PacketAbstract.preparePacket(readBytes);
		logger.trace("\n\t{}\n\t{}", ToHex.bytesToHex(readBytes), ToHex.bytesToHex(byteStuffing));

		final byte[] readBytes2 = PacketAbstract.byteStuffing(byteStuffing);
		final byte[] concatAll = Packet.concatAll(new byte[]{0x7E}, readBytes, new ChecksumLinkedPacket(readBytes).toBytes(), new byte[]{0x7E});
		logger.trace("\n\t original\n{}\n\t prepared packet\n{}\n\tresule\n{}", ToHex.bytesToHex(concatAll), ToHex.bytesToHex(readBytes2));
		assertTrue(Arrays.equals(concatAll, readBytes2));
	}

	@Test
	public void testByteStuffing() throws PacketParsingException {
		final byte[] readBytes = new byte[]{0x7E, (byte)0xFE, 0x00, 0x00, 0x00, (byte)0xFF, 0x00, 0x01, 0x7A, 0x4A, 0x7E, 0x7E, (byte)0xFE, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x3D, 0x00, 0x00, 0x00, 0x03, 0x00, 0x0C, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x5F, (byte)0xCB, 0x7D, 0x5D, 0x7E};
		final byte[] byteStuffing = PacketAbstract.byteStuffing(readBytes);
		logger.trace("\n\t{}\n\t{}", ToHex.bytesToHex(readBytes), ToHex.bytesToHex(byteStuffing));
	}
}
