package irt.gui.data.packet.observable.device_debug;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import irt.gui.controllers.ComPortTest;
import irt.gui.controllers.LinkedPacketSender;
import irt.gui.data.ToHex;
import irt.gui.data.packet.Packet;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketType;
import irt.gui.data.packet.observable.PacketAbstract;
import irt.gui.data.packet.observable.device_debug.CallibrationModePacket;
import irt.gui.data.packet.observable.device_debug.CallibrationModePacket.CalibrationMode;
import irt.gui.errors.PacketParsingException;
import jssc.SerialPortException;

public class CallibrationModePacketTest {

	Logger logger = LogManager.getLogger();

	@Test
	public void testRequest() throws PacketParsingException {
		logger.entry();

		CallibrationModePacket packet = new CallibrationModePacket((CalibrationMode)null);
		logger.trace(packet);
		byte[] bytes = packet.toBytes();
		logger.trace("\n\t{}", ToHex.bytesToHex(bytes));

		logger.trace("\n\tLinkHeader: {}", ToHex.bytesToHex(packet.getLinkHeader().toBytes()));
		logger.trace("\n\tPacketHeader: {}", ToHex.bytesToHex(packet.getPacketHeader().toBytes()));
		List<Payload> payloads = packet.getPayloads();
		for(Payload p:payloads)
		logger.trace("\n\tPayload: {}", ToHex.bytesToHex(p.toBytes()));

		assertNotNull(bytes);

		byte[] packetAsBytes = Packet.getPacketAsBytes(PacketType.REQUEST, CallibrationModePacket.PACKET_ID, null);
		logger.trace("\n\t{}", ToHex.bytesToHex(packetAsBytes));

		assertTrue(Arrays.equals(packetAsBytes, bytes));

		logger.exit();
	
	}

	@Test
	public void testCommandCallibrationModeOn() throws PacketParsingException {
		logger.entry();

		CalibrationMode mode = CalibrationMode.ON;
		CallibrationModePacket packet = new CallibrationModePacket(mode);
		logger.trace(packet);
		byte[] bytes = packet.toBytes();
		logger.trace("\n\t{}", ToHex.bytesToHex(bytes));

		logger.trace("\n\tLinkHeader: {}", ToHex.bytesToHex(packet.getLinkHeader().toBytes()));
		logger.trace("\n\tPacketHeader: {}", ToHex.bytesToHex(packet.getPacketHeader().toBytes()));
		List<Payload> payloads = packet.getPayloads();
		for(Payload p:payloads)
		logger.trace("\n\tPayload: {}", ToHex.bytesToHex(p.toBytes()));

		assertNotNull(bytes);

		byte[] packetAsBytes = Packet.getPacketAsBytes(PacketType.COMMAND, CallibrationModePacket.PACKET_ID, Packet.toBytes(mode.ordinal()));
		logger.trace("\n\t{}", ToHex.bytesToHex(packetAsBytes));

		assertTrue(Arrays.equals(packetAsBytes, bytes));

		logger.exit();
	}

	@Test
	public void testCommandCallibrationModeOff() throws PacketParsingException {
		logger.entry();

		CalibrationMode mode = CalibrationMode.OFF;
		CallibrationModePacket packet = new CallibrationModePacket(mode);
		logger.trace(packet);
		byte[] bytes = packet.toBytes();
		logger.trace("\n\t{}", ToHex.bytesToHex(bytes));

		logger.trace("\n\tLinkHeader: {}", ToHex.bytesToHex(packet.getLinkHeader().toBytes()));
		logger.trace("\n\tPacketHeader: {}", ToHex.bytesToHex(packet.getPacketHeader().toBytes()));
		List<Payload> payloads = packet.getPayloads();
		for(Payload p:payloads)
		logger.trace("\n\tPayload: {}", ToHex.bytesToHex(p.toBytes()));

		assertNotNull(bytes);

		byte[] packetAsBytes = Packet.getPacketAsBytes(PacketType.COMMAND, CallibrationModePacket.PACKET_ID, Packet.toBytes(mode.ordinal()));
		logger.trace("\n\t{}", ToHex.bytesToHex(packetAsBytes));

		assertTrue(Arrays.equals(packetAsBytes, bytes));

		logger.exit();
	}

	@Test
	public void testObserver() throws PacketParsingException {
		logger.entry();

		CallibrationModePacket packet = new CallibrationModePacket((CalibrationMode)null);
		packet.addObserver(new Observer() {

			@Override
			public void update(Observable o, Object arg) {
				logger.debug("\n\t Observable: {}\n", o);

				try {

					PacketAbstract bp = new PacketAbstract(CallibrationModePacket.PACKET_ID, ((LinkedPacket)o).getAnswer()){};
					logger.debug("\n\t new PacketAbstract: {}\n", bp);

				} catch (PacketParsingException e) {
					logger.catching(e);
				}
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

		logger.exit();
	}
}