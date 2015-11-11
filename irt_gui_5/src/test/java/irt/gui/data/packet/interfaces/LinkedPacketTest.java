
package irt.gui.data.packet.interfaces;

import static org.junit.Assert.*;

import org.junit.Test;

import irt.gui.data.packet.interfaces.LinkedPacket.PacketId;
import irt.gui.data.packet.interfaces.LinkedPacket.ParameterHeaderCode;
import irt.gui.errors.PacketParsingException;

public class LinkedPacketTest {

	@Test
	public void testPacketId() throws PacketParsingException {

		PacketId pId = PacketId.DEVICE_INFO;
		assertEquals(ParameterHeaderCode.DI_ALL, pId.valueOf(ParameterHeaderCode.DI_ALL.getValue()));

		pId = PacketId.DEVICE_DEBAG_CALIBRATION_MODE;
		assertEquals(ParameterHeaderCode.DD_READ_WRITE, pId.valueOf(ParameterHeaderCode.DD_READ_WRITE.getValue()));
	}
}
