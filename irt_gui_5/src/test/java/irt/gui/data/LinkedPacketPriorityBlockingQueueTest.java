
package irt.gui.data;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

import irt.gui.data.packet.observable.InfoPacket;
import irt.gui.errors.PacketParsingException;

public class LinkedPacketPriorityBlockingQueueTest {

	@Test
	public void test() throws PacketParsingException {
		final LinkedPacketPriorityBlockingQueue linkedPacketPriorityBlockingQueue = new LinkedPacketPriorityBlockingQueue();

		final InfoPacket linkedPacket = new InfoPacket();
		final InfoPacket linkedPacket2 = new InfoPacket();
		linkedPacketPriorityBlockingQueue.add(linkedPacket);
		linkedPacketPriorityBlockingQueue.add(linkedPacket2);

		assertEquals(1, linkedPacketPriorityBlockingQueue.size());
		assertThat(System.identityHashCode(linkedPacket), not(System.identityHashCode(linkedPacket2)));
	}

}
