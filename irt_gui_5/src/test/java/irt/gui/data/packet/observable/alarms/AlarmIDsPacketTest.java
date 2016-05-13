
package irt.gui.data.packet.observable.alarms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
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
import irt.gui.data.packet.enums.PacketErrors;
import irt.gui.data.packet.enums.PacketGroupId;
import irt.gui.data.packet.enums.PacketId;
import irt.gui.data.packet.enums.PacketType;
import irt.gui.data.packet.enums.ParameterHeaderCode;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.observable.PacketAbstract;
import irt.gui.errors.PacketParsingException;
import jssc.SerialPortException;

public class AlarmIDsPacketTest {

	Logger logger = LogManager.getLogger();

	@Test
	public void testRequest() throws PacketParsingException {

		assertEquals(PacketId.ALARMS				, AlarmIDsPacket.PACKET_ID);

		assertEquals(PacketGroupId.ALARM			, AlarmIDsPacket.PACKET_ID.getPacketGroupId());
		assertEquals(Packet.GROUP_ID_ALARM			, PacketGroupId.ALARM.getValue());

		assertEquals(ParameterHeaderCode.ALARM_IDs	, AlarmIDsPacket.PACKET_ID.getParameterHeaderCode());
		assertEquals(Packet.ALARMS_IDs	, ParameterHeaderCode.ALARM_IDs.getValue());

		AlarmIDsPacket packet = new AlarmIDsPacket();
		byte[] bytes = packet.toBytes();

		logger.trace("\n\t{}", ToHex.bytesToHex(bytes));
		logger.trace(packet);

		assertNotNull(bytes);

		byte[] packetAsBytes = Packet.getPacketAsBytes(PacketType.REQUEST, AlarmIDsPacket.PACKET_ID, null);
		logger.trace("\n\t{}", ToHex.bytesToHex(packetAsBytes));

		assertTrue(Arrays.equals(packetAsBytes, bytes));
	}


	@Test
	public void testObserver() throws PacketParsingException {
		logger.entry();

		AlarmIDsPacket packet = new AlarmIDsPacket();
		packet.addObserver(new Observer() {

			@Override
			public void update(Observable o, Object arg) {
				logger.debug("\n\t Observable: {}\n", o);

				try {

					PacketAbstract bp = new PacketAbstract(new PacketProperties(AlarmIDsPacket.PACKET_ID).setHasAcknowledgment(true), ((LinkedPacket)o).getAnswer()){

						@Override
						public PacketId getPacketId() {
							throw new UnsupportedOperationException("Auto-generated method stub");
						}};
					logger.debug("\n\t new PacketAbstract: {}\n", bp);
					assertEquals(PacketErrors.NO_ERROR, bp.getPacketHeader().getPacketError());
					assertEquals(1, bp.getPayloads().size());

					Payload payload = bp.getPayloads().get(0);
					assertTrue(payload.getParameterHeader().getPayloadSize().getSize()>0);

					logger.trace("\n\t alarm IDs:\n\t{}", payload.getArrayOfShort());

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
