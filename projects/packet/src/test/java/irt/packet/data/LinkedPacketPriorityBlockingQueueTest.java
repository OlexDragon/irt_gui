
package irt.packet.data;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import irt.packet.observable.TestPacket;

public class LinkedPacketPriorityBlockingQueueTest {

	private final LinkedPacketPriorityBlockingQueue queue = new LinkedPacketPriorityBlockingQueue();
	private final TestPacket packet = new TestPacket((byte)254);

	@Before
	public void setup(){
		queue.add(packet);
		packet.addObserver((o,arg)->{});
	}

	@Test
	public void test() throws Exception {

		final TestPacket p2 = new TestPacket((byte)4);
		p2.addObserver((o,arg)->{});
		queue.add(p2);

		assertEquals(2, queue.size());
		assertEquals(1, p2.getObservers().length);

		final TestPacket p = new TestPacket((byte)254);
		p.addObserver((o,arg)->{});
		queue.add(p);

		assertEquals(2, queue.size());
		assertEquals(2, p.getObservers().length);
	}

}
