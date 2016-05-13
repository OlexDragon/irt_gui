
package irt.gui.data.packet.observable.calibration;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import irt.gui.data.packet.observable.calibration.prologix.PAddrPacket;
import irt.gui.data.packet.observable.flash.ReadPacket;

public class ToolsComandsPacketTest {

	@Test
	public void test() {
		final PAddrPacket pAddrPacket = new PAddrPacket();
		final ToolsFrequencyPacket toolsFrequencyPacket = new ToolsFrequencyPacket();
		final ReadPacket readPacket = new ReadPacket();

		final ToolsComandsPacket toolsComandsPacket = new ToolsComandsPacket(pAddrPacket, toolsFrequencyPacket, readPacket);

		final byte[] bytes1 = pAddrPacket.toBytes();
		final byte[] bytes2 = toolsFrequencyPacket.toBytes();
		final byte[] bytes3 = readPacket.toBytes();
		final int length = bytes1.length +bytes2.length +bytes3.length;

		final byte[] bytes = toolsComandsPacket.toBytes();
		
		assertEquals(length, bytes.length);
		assertArrayEquals(bytes1, Arrays.copyOf(bytes, bytes1.length));
		assertArrayEquals(bytes2, Arrays.copyOfRange(bytes, bytes1.length, bytes1.length+bytes2.length));
		assertArrayEquals(bytes3, Arrays.copyOfRange(bytes, bytes1.length+bytes2.length, bytes1.length+bytes2.length+bytes3.length));
	}

}
