
package irt.packet.observable.measurement;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

import org.junit.Test;

import irt.packet.PacketParsingException;
import irt.packet.observable.PacketAbstract;

public class InputPowerPacketTest {

	@Test
	public void test() throws PacketParsingException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

		final InputPowerPacket packet = new InputPowerPacket();

		final byte[] acknowledgement = packet.getAcknowledgement();
		final byte[] bytes = packet.toBytes();

		ByteBuffer buffer = ByteBuffer.allocate(acknowledgement.length + bytes.length);
		buffer.put(acknowledgement);
		buffer.put(bytes);
		packet.setAnswer(buffer.array());

		final PacketAbstract p = packet.getAnswerPacket();

		assertEquals(packet, p);
	}

}
