
package irt.gui.data.packet.observable.device_debug;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import irt.gui.IrtGuiProperties;
import irt.gui.data.RegisterValue;
import irt.gui.data.packet.enums.PacketType;
import irt.gui.data.packet.enums.ParameterHeaderCode;
import irt.gui.data.packet.observable.PacketAbstract5;
import irt.gui.data.packet.observable.device_debug.DACPacket.DACs;
import irt.gui.data.packet.observable.device_debug.DACPacket.UnitType;
import irt.gui.errors.PacketParsingException;

public class DAC1PacketTest {
	private final Logger logger = LogManager.getLogger();

	@Test
	public void test1() throws PacketParsingException {
		final DACPacket packet = new DACPacket(DACs.DAC1, null);
		packet.setLinkHeaderAddr(PacketAbstract5.CONVERTER_ADDR);//Converter

		logger.trace(packet);

		assertEquals(IrtGuiProperties.getLong("gui.converter.DAC.1.FCM.index").intValue(), DACs.DAC1.getIndex(UnitType.FCM));

		assertEquals(22, packet.toBytes().length);
		assertEquals(PacketType.REQUEST, packet.getPacketHeader().getPacketType());
		assertEquals(ParameterHeaderCode.DD_READ_WRITE, packet.getPayloads().get(0).getParameterHeader().getParameterHeaderCode());

		assertArrayEquals(new RegisterValue(DACs.DAC1.getIndex(UnitType.FCM), DACs.DAC1.getAddress(UnitType.FCM)).toBytes(), packet.getPayloads().get(0).getBuffer());
	}

	@Test
	public void test2() throws PacketParsingException {
		final DACPacket packet = new DACPacket(DACs.DAC1, null);

		logger.trace(packet);

		assertEquals(IrtGuiProperties.getLong("gui.converter.DAC.1.index").intValue(), DACs.DAC1.getIndex(UnitType.BUC));

		assertEquals(26, packet.toBytes().length);
		assertEquals(PacketType.REQUEST, packet.getPacketHeader().getPacketType());
		assertEquals(ParameterHeaderCode.DD_READ_WRITE, packet.getPayloads().get(0).getParameterHeader().getParameterHeaderCode());

		assertArrayEquals(new RegisterValue(DACs.DAC1.getIndex(UnitType.BUC), DACs.DAC1.getAddress(UnitType.BUC)).toBytes(), packet.getPayloads().get(0).getBuffer());
	}

}
