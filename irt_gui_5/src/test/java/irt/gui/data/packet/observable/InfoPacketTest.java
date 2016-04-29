
package irt.gui.data.packet.observable;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import irt.gui.controllers.ComPortTest;
import irt.gui.controllers.LinkedPacketSender;
import irt.gui.data.ToHex;
import irt.gui.data.packet.Packet;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.enums.PacketId;
import irt.gui.data.packet.enums.PacketType;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.errors.PacketParsingException;
import jssc.SerialPortException;

public class InfoPacketTest {

	Logger logger = LogManager.getLogger();

	@Test
	public void testToBytes() throws PacketParsingException {

		LinkedPacket packet = new InfoPacket();
		logger.trace("packet.toBytes()={} {}", ToHex.bytesToHex(packet.toBytes()), packet);
		byte[] bytes = packet.toBytes();
		logger.trace("\n\t{}", bytes);

		logger.trace("\n\tLinkHeader: {}", packet.getLinkHeader().toBytes());
		logger.trace("\n\tPacketHeader: {}", packet.getPacketHeader().toBytes());
		List<Payload> payloads = packet.getPayloads();
		for(Payload p:payloads)
		logger.trace("\n\tPayload: {}", p.toBytes());

		assertNotNull(bytes);

		byte[] packetAsBytes = Packet.getPacketAsBytes(PacketType.REQUEST, InfoPacket.PACKET_ID, null);
		logger.trace("\n\t{}", ToHex.bytesToHex(packetAsBytes));

		assertTrue(Arrays.equals(packetAsBytes, bytes));
	}

	@Test
	public void testObserver() throws PacketParsingException {
		InfoPacket packet = new InfoPacket();
		packet.addObserver(( o,  arg)-> {
				logger.trace(o);

				try {

					PacketAbstract bp = new PacketAbstract(InfoPacket.PACKET_ID, ((LinkedPacket)o).getAnswer(), true){

						@Override
						public PacketId getPacketId() {
							throw new UnsupportedOperationException("Auto-generated method stub");
						}};
					logger.debug("\n\t new PacketAbstract: {}\n", bp);
					assertNotNull(packet.getAnswer());

					LinkedPacket p = new InfoPacket(packet.getAnswer(), true);
					logger.trace(p);

				} catch (PacketParsingException e) {
					logger.catching(e);
					assertTrue("Packet Parsing error", false);
				}
		});

		LinkedPacketSender port = new LinkedPacketSender(ComPortTest.COM_PORT);
		try {

			port.openPort();

			port.send(packet);
			assertNotNull(packet.getAnswer());

			port.closePort();

		} catch (SerialPortException e) {
			logger.catching(e);
		}
	}
}
