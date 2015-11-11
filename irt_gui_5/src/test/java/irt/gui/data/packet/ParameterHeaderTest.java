
package irt.gui.data.packet;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import irt.gui.data.packet.interfaces.LinkedPacket.PacketId;
import irt.gui.errors.PacketParsingException;

public class ParameterHeaderTest {

	@Test
	public void test1() throws PacketParsingException {
		ParameterHeader ph = new ParameterHeader(PacketId.DEVICE_DEBAG_POTENTIOMETER);
		ph.setSize((short) 30014);
		assertEquals(PacketId.DEVICE_DEBAG_POTENTIOMETER.getParameterHeaderCode(), ph.getParameterHeaderCode());
		assertEquals((short) 30014, ph.getPayloadSize().getSize());
	}

	@Test
	public void test2() throws PacketParsingException {
		ParameterHeader ph = new ParameterHeader(PacketId.DEVICE_DEBAG_POTENTIOMETER, new PayloadSize((short) 30014));
		assertEquals(PacketId.DEVICE_DEBAG_POTENTIOMETER.getParameterHeaderCode(), ph.getParameterHeaderCode());
		assertEquals((short) 30014, ph.getPayloadSize().getSize());
	}


	@Test(expected=PacketParsingException.class)
	public void nullTest() throws PacketParsingException {
		new ParameterHeader(null);
	}

	@Test(expected=PacketParsingException.class)
	public void tooShortTest() throws PacketParsingException {
		 new ParameterHeader(PacketId.DEVICE_DEBAG_POTENTIOMETER, null);
	}
}
