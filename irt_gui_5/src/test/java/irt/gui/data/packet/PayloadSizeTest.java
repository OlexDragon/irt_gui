
package irt.gui.data.packet;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import irt.gui.data.ToHex;

public class PayloadSizeTest {

	Logger logger = LogManager.getLogger();

	@Test
	public void test() {
		PayloadSize ps = new PayloadSize((short) 26026);

		byte[] bytes = new byte[]{0x65, (byte) 0xAA};
		logger.trace("\n\t{}\n\t{}\n\t{}", ToHex.bytesToHex(bytes), ToHex.bytesToHex(ps.toBytes()), ps);

		assertTrue(Arrays.equals(bytes, ps.toBytes()));
	}

}
