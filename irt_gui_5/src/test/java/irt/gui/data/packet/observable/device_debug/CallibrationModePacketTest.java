package irt.gui.data.packet.observable.device_debug;

import static org.junit.Assert.assertEquals;
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
import irt.gui.data.packet.PacketProperties;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.enums.PacketId;
import irt.gui.data.packet.enums.PacketType;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.observable.PacketAbstract;
import irt.gui.data.packet.observable.device_debug.CallibrationModePacket.CalibrationMode;
import irt.gui.errors.PacketParsingException;
import jssc.SerialPortException;

public class CallibrationModePacketTest {

	Logger logger = LogManager.getLogger();

	@Test
	public void testRequest() throws PacketParsingException {
		logger.traceEntry();

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

		logger.traceExit();
	
	}

	@Test
	public void testCommandCallibrationModeOn() throws PacketParsingException {
		logger.traceEntry();

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

		logger.traceExit();
	}

	@Test
	public void testCommandCallibrationModeOff() throws PacketParsingException {
		logger.traceEntry();

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

		logger.traceExit();
	}

	@Test
	public void testObserver() throws PacketParsingException {
		logger.traceEntry();

		CallibrationModePacket packet = new CallibrationModePacket((CalibrationMode)null);
		packet.addObserver(new Observer() {

			@Override
			public void update(Observable o, Object arg) {
				logger.debug("\n\t Observable: {}\n", o);

				try {

					PacketAbstract bp = new PacketAbstract(new PacketProperties(CallibrationModePacket.PACKET_ID).setHasAcknowledgment(true), ((LinkedPacket)o).getAnswer()){

						@Override
						public PacketId getPacketId() {
							throw new UnsupportedOperationException("Auto-generated method stub");
						}};
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

		logger.traceExit();
	}

	CalibrationMode calibrationMode = CalibrationMode.ON;
	final Observer observer = new Observer() {

		@Override
		public void update(Observable o, Object arg) {
			logger.debug("\n\t Observable: {}\n", o);

			try {

				CallibrationModePacket bp = new CallibrationModePacket(((LinkedPacket)o).getAnswer(), true);
				logger.debug("\n\t new PacketAbstract: {}\n", bp);

				final CalibrationMode cm = bp.getCallibrationMode();
				logger.trace(cm);

				assertEquals(calibrationMode, cm);

				calibrationMode = CalibrationMode.OFF;

			} catch (PacketParsingException e) {
				logger.catching(e);
			}
		}
	};
	@Test
	public void testObserverSetOnOff() throws PacketParsingException, InterruptedException {
		logger.traceEntry();

		LinkedPacketSender port = new LinkedPacketSender(ComPortTest.COM_PORT);


		try {

			port.openPort();

			CallibrationModePacket packet = new CallibrationModePacket(calibrationMode);
			packet.addObserver(observer);
			port.send(packet);
			assertNotNull(packet.getAnswer());

			packet = new CallibrationModePacket(calibrationMode);
			packet.addObserver(observer);
			port.send(packet);
			assertNotNull(packet.getAnswer());

			port.closePort();

		} catch (SerialPortException e) {
			logger.catching(e);
		}

		logger.traceExit();
	}
}
