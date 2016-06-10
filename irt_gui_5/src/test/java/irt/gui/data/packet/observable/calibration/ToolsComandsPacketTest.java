
package irt.gui.data.packet.observable.calibration;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import irt.gui.data.packet.interfaces.PacketToSend;
import irt.gui.data.packet.observable.calibration.prologix.PAddrPacket;
import irt.gui.data.packet.observable.flash.ReadPacket;

public class ToolsComandsPacketTest {

	@Test
	public void test() {
		final PAddrPacket pAddrPacket = new PAddrPacket();
		final ToolsFrequencyPacket toolsFrequencyPacket = new ToolsFrequencyPacket();
		final ReadPacket readPacket = new ReadPacket();

		List<PacketToSend> l = new ArrayList<>();
		l.add(pAddrPacket);

		final ToolsComandsPacket toolsComandsPacket = new ToolsComandsPacket(l);

		l.add(toolsFrequencyPacket);
		l.add(readPacket);

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
