
package irt.gui.data.packet.observable.developer;

import static org.junit.Assert.assertEquals;

import org.apache.logging.log4j.LogManager;
import org.junit.Test;

import irt.gui.data.ToHex;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.ParameterHeader;
import irt.gui.data.packet.PayloadSize;
import irt.gui.data.packet.enums.PacketGroupId;
import irt.gui.data.packet.enums.PacketId;
import irt.gui.data.packet.enums.PacketType;
import irt.gui.data.packet.enums.ParameterHeaderCode;
import irt.gui.data.packet.observable.production.ConnectFCMPacket;
import irt.gui.errors.PacketParsingException;

public class ConnectFCMPacketTest {

	@Test
	public void test() throws PacketParsingException {

		final ConnectFCMPacket packet = new ConnectFCMPacket();
		LogManager.getLogger().trace(packet);
		LogManager.getLogger().trace(ToHex.bytesToHex(packet.toBytes()));
		assertEquals(PacketId.PRODUCTION_UPDATE_FCM, packet.getPacketId());

		final PacketHeader packetHeader = packet.getPacketHeader();
		assertEquals(PacketGroupId.PRODUCTION, packetHeader.getPacketGroupId());
		assertEquals(PacketType.COMMAND, packetHeader.getPacketType());

		final ParameterHeader parameterHeader = packet.getPayloads().get(0).getParameterHeader();
		assertEquals(ParameterHeaderCode.PRODUCTION_CONNECT_FCM, parameterHeader.getParameterHeaderCode());
		assertEquals(new PayloadSize((short) 0), parameterHeader.getPayloadSize());
	}

}
