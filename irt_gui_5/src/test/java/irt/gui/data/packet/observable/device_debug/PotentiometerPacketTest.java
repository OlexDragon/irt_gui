
package irt.gui.data.packet.observable.device_debug;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Test;

import irt.gui.controllers.ComPortTest;
import irt.gui.controllers.PacketSenderJssc;
import irt.gui.data.RegisterValue;
import irt.gui.data.ToHex;
import irt.gui.data.packet.Packet;
import irt.gui.data.packet.PacketProperties;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.enums.PacketId;
import irt.gui.data.packet.enums.PacketType;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.observable.PacketAbstract5;
import irt.gui.errors.PacketParsingException;
import jssc.SerialPort;
import jssc.SerialPortException;

public class PotentiometerPacketTest {

	Logger logger = LogManager.getLogger();
	private PacketSenderJssc port  = new PacketSenderJssc(ComPortTest.COM_PORT);

	@Test
	public void testRequest() throws PacketParsingException {
		logger.traceEntry();

		RegisterValue registerValue = new RegisterValue(1, 0);
		RegisterPacket packet = new RegisterPacket("testRequest()", registerValue);
		logger.trace(packet);

		byte[] bytes = packet.toBytes();
		logger.trace("\n\t{}", ToHex.bytesToHex(bytes));

		logger.trace("\n\tLinkHeader: {}", ToHex.bytesToHex(packet.getLinkHeader().toBytes()));
		logger.trace("\n\tPacketHeader: {}", ToHex.bytesToHex(packet.getPacketHeader().toBytes()));
		List<Payload> payloads = packet.getPayloads();
		for(Payload p:payloads)
		logger.trace("\n\tPayload: {}", ToHex.bytesToHex(p.toBytes()));

		assertNotNull(bytes);

		byte[] packetAsBytes = Packet.getPacketAsBytes(PacketType.REQUEST, RegisterPacket.PACKET_ID, registerValue.toBytes());
		logger.trace("\n\t{}", ToHex.bytesToHex(packetAsBytes));

		assertTrue(Arrays.equals(packetAsBytes, bytes));

		logger.traceExit();
	}

	@Test
	public void testCommand() throws PacketParsingException {
		logger.traceEntry();
		RegisterValue registerValue = new RegisterValue(1, 0, 777);
		RegisterPacket packet = new RegisterPacket("testCommand()", registerValue);
		logger.trace(packet);
		byte[] bytes = packet.toBytes();
		logger.trace("\n\t{}", ToHex.bytesToHex(bytes));

		logger.trace("\n\tLinkHeader: {}", ToHex.bytesToHex(packet.getLinkHeader().toBytes()));
		logger.trace("\n\tPacketHeader: {}", ToHex.bytesToHex(packet.getPacketHeader().toBytes()));
		List<Payload> payloads = packet.getPayloads();
		for(Payload p:payloads)
		logger.trace("\n\tPayload: {}", ToHex.bytesToHex(p.toBytes()));

		assertNotNull(bytes);

		byte[] packetAsBytes = Packet.getPacketAsBytes(PacketType.COMMAND, RegisterPacket.PACKET_ID, registerValue.toBytes());
		logger.trace("\n\t{}", ToHex.bytesToHex(packetAsBytes));

		assertTrue(Arrays.equals(packetAsBytes, bytes));

		logger.traceExit();
	}

	@Test
	public void testObserver() throws PacketParsingException {
		logger.traceEntry();

		RegisterPacket packet = new RegisterPacket("testObserver()", new RegisterValue(1, 0));
		packet.addObserver(new Observer() {

			@Override
			public void update(Observable o, Object arg) {
				logger.debug("\n\t Observable: {}\n", o);

				try {

					PacketAbstract5 bp = new PacketAbstract5(new PacketProperties(RegisterPacket.PACKET_ID).setHasAcknowledgment(true), ((LinkedPacket)o).getAnswer()){

						@Override
						public PacketId getPacketId() {
							throw new UnsupportedOperationException("Auto-generated method stub");
						}};
					logger.debug("\n\t new PacketAbstract5: {}\n", bp);

				} catch (PacketParsingException e) {
					logger.catching(e);
				}
			}
		});

		try {

			port.openPort();

			port.send(packet);
			logger.trace("\n\t packet as byte:\n\t\t{}", ToHex.bytesToHex(packet.toBytes()));
			assertNotNull(packet.getAnswer());

			port.closePort();

		} catch (SerialPortException e) {
			logger.catching(e);
		}

		logger.traceExit();
	}

	@Test
	public void equalsTest() throws PacketParsingException{
		assertThat(new RegisterPacket("equalsTest()", new RegisterValue(1, 5)), is(new RegisterPacket("equalsTest()", new RegisterValue(1, 5))));

		assertThat(new RegisterPacket("equalsTest()", new RegisterValue(1, 5)), is(new RegisterPacket("equalsTest()", new RegisterValue(1, 5, 0))));
		assertThat(new RegisterPacket("equalsTest()", new RegisterValue(1, 5)), not(new RegisterPacket("equalsTest()", new RegisterValue(1, 7))));
		assertThat(new RegisterPacket("equalsTest()", new RegisterValue(1, 5)), not(new RegisterPacket("equalsTest()", new RegisterValue(2, 5))));
	}

	@After
	public void exit() {
		Optional.ofNullable(port).filter(SerialPort::isOpened).ifPresent(p -> {
			try {
				p.closePort();
			} catch (SerialPortException e) {
				logger.catching(e);
			}
		});
	}
}
