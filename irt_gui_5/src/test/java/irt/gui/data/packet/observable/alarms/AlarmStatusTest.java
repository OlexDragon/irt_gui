
package irt.gui.data.packet.observable.alarms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Test;

import irt.gui.controllers.ComPortTest;
import irt.gui.controllers.serial_port.PacketSenderJssc;
import irt.gui.data.ToHex;
import irt.gui.data.packet.Packet;
import irt.gui.data.packet.PacketProperties;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.enums.PacketErrors;
import irt.gui.data.packet.enums.PacketGroupId;
import irt.gui.data.packet.enums.PacketId;
import irt.gui.data.packet.enums.PacketType;
import irt.gui.data.packet.enums.ParameterHeaderCode;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.observable.PacketAbstract5;
import irt.gui.data.packet.observable.alarms.AlarmStatusPacket.AlarmSeverities;
import irt.gui.errors.PacketParsingException;
import jssc.SerialPort;
import jssc.SerialPortException;

public class AlarmStatusTest {

	Logger logger = LogManager.getLogger();
	private PacketSenderJssc port  = new PacketSenderJssc(ComPortTest.COM_PORT);

	@Test
	public void test() throws PacketParsingException {

		final PacketId packetId = AlarmStatusPacket.PACKET_ID;

		assertEquals(PacketId.ALARM_STATUS	, packetId);

		assertEquals(PacketGroupId.ALARM			, AlarmIDsPacket.PACKET_ID.getPacketGroupId());
		assertEquals(Packet.GROUP_ID_ALARM			, PacketGroupId.ALARM.getValue());

		assertEquals(ParameterHeaderCode.ALARM_STATUS	, packetId.getParameterHeaderCode());
		assertEquals(Packet.ALARM_STATUS				, packetId.getParameterHeaderCode().getValue());

		final short value = (short) 1;
		AlarmStatusPacket packet = new AlarmStatusPacket(value);
		byte[] bytes = packet.toBytes();

		logger.trace("\n\t{}", ToHex.bytesToHex(bytes));
		logger.trace(packet);

		assertNotNull(bytes);

		byte[] packetAsBytes = Packet.getPacketAsBytes(PacketType.REQUEST, packetId, Packet.shortToBytes(value));
		logger.trace("\n\t{}", ToHex.bytesToHex(packetAsBytes));

		assertTrue(Arrays.equals(packetAsBytes, bytes));
	}

	@Test
	public void testObserver() throws PacketParsingException {
		logger.traceEntry();

		AlarmSummaryStatusPacket packet = new AlarmSummaryStatusPacket();
		packet.addObserver(new Observer() {

			@Override
			public void update(Observable o, Object arg) {
				logger.debug("\n\t Observable: {}\n", o);

				try {

					PacketAbstract5 bp = new PacketAbstract5(new PacketProperties(AlarmIDsPacket.PACKET_ID).setHasAcknowledgment(true), ((LinkedPacket)o).getAnswer()){

						@Override
						public PacketId getPacketId() {
							throw new UnsupportedOperationException("Auto-generated method stub");
						}};
					logger.debug("\n\t new PacketAbstract5: {}\n", bp);
					assertEquals(PacketErrors.NO_ERROR, bp.getPacketHeader().getPacketError());
					assertEquals(1, bp.getPayloads().size());

					Payload payload = bp.getPayloads().get(0);
					assertTrue(payload.getParameterHeader().getPayloadSize().getSize()>0);

					final int status = payload.getInt(0) & 0x07;

					logger.trace("\n\tStatus: {}", status);
					logger.trace("\n\t{}", AlarmSeverities.values()[status]);

				} catch (PacketParsingException e) {
					logger.catching(e);
				}
			}
		});

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
