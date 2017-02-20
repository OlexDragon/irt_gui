
package irt.data.value;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import irt.data.value.enumes.ValueStatus;

public class PacketDoubleValueTest {
	Logger logger = LogManager.getLogger();

	@Test(expected=IllegalArgumentException.class)
	public void nullValueTest() {
		new PacketDoubleValue(null, 10);
	}

	@Test(expected=IllegalArgumentException.class)
	public void valueLengthTest() {
		new PacketDoubleValue(new byte[]{}, 10);
	}

	@Test(expected=IllegalArgumentException.class)
	public void dividerTest() {
		new PacketDoubleValue(new byte[]{1,1,1}, 0);
	}

	@Test
	public void length2BytesTest() {

		final ByteBuffer bb = ByteBuffer.allocate(2);
		bb.putShort((short) -55);
		PacketDoubleValue pv = new PacketDoubleValue(bb.array(), 10);

		assertEquals("-5.5", pv.toString());

		bb.clear();
		bb.putShort((short) -543);
		pv = new PacketDoubleValue(bb.array(), 10);

		assertEquals("-54.3", pv.toString());

		bb.clear();
		bb.putShort((short) 637);
		pv = new PacketDoubleValue(bb.array(), 1);

		assertEquals("637.0", pv.toString());
	}

	@Test
	public void length3BytesTest() {

		final ByteBuffer bb = ByteBuffer.allocate(3);
		bb.putShort(1, (short) -55);
		byte[] array = bb.array();
		logger.trace("{}", array);
		PacketDoubleValue pv = new PacketDoubleValue(array, 10);

		assertEquals("<?>-5.5", pv.toString());

		bb.clear();
		bb.put(0, (byte) ValueStatus.IN_RANGE.ordinal());
		bb.putShort(1, (short) -543);
		array = bb.array();
		logger.trace("{}", array);
		pv = new PacketDoubleValue(array, 10);

		assertEquals("-54.3", pv.toString());

		bb.put(0, (byte) ValueStatus.OVER_RANGE.ordinal());
		array = bb.array();
		logger.trace("{}", array);
		pv = new PacketDoubleValue(array, 10);

		assertEquals(">-54.3", pv.toString());

		bb.put(0, (byte) ValueStatus.UNDER_RANGE.ordinal());
		array = bb.array();
		logger.trace("{}", array);
		pv = new PacketDoubleValue(array, 10);

		assertEquals("<-54.3", pv.toString());
	}
}
