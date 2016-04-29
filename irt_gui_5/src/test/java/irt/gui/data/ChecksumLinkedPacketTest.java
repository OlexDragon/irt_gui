
package irt.gui.data;

import static org.junit.Assert.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class ChecksumLinkedPacketTest {
	private Logger logger = LogManager.getLogger();

	@Test
	public void test1() {
		final ChecksumLinkedPacket cs = new ChecksumLinkedPacket((byte)0, (byte)0, (byte)0);
		logger.trace(cs);
	}

	@Test
	public void test2() {
		final ChecksumLinkedPacket cs = new ChecksumLinkedPacket((byte)00, (byte)00, (byte)0x40, (byte)0x1B, (byte)0x16, (byte)0x00, (byte)0x02, (byte)0x00);
		logger.trace(cs);
	}

	@Test
	public void test3() {
		final ChecksumLinkedPacket cs = new ChecksumLinkedPacket((byte)0x02, (byte)0x00, (byte)0x55, (byte)0x3D, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x03, (byte)0x00, (byte)0x08, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0A, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x07);
		logger.trace(cs);
		assertArrayEquals(new byte[]{(byte)0xC8, 0x25}, cs.toBytes());
	}
}
//7E 00 00 00 A3 AB 7E 7E 00 00 40 1B 16 00 02 00 71 E5 FF 

//acknowledgement: 00 00 00 A3 AB
//checksum: A3 AB 
//ChecksumLinkedPacket 
//	[checksumOf=00 00 00 , 
//	fcs=50892,
//	getChecksumAsBytes()=CC C6 ]



//Test 3
//7E 02 00 55 3D 00 00 00 03 00 08 00 00 00 0A 00 00 00 07 C8 25 7E