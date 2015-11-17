
package irt.gui.data.packet.observable.alarms;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

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
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketErrors;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketGroupId;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketId;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketType;
import irt.gui.data.packet.interfaces.LinkedPacket.ParameterHeaderCode;
import irt.gui.data.packet.observable.PacketAbstract;
import irt.gui.errors.PacketParsingException;
import jssc.SerialPortException;

public class AlarmSummaryStatusTest {

	Logger logger = LogManager.getLogger();

	@Test
	public void testRequest() throws PacketParsingException {

		final PacketId packetId = AlarmSummaryStatusPacket.PACKET_ID;

		assertEquals(PacketId.ALARM_SUMMARY_STATUS	, packetId);

		assertEquals(PacketGroupId.ALARM			, AlarmIDsPacket.PACKET_ID.getPacketGroupId());
		assertEquals(Packet.GROUP_ID_ALARM			, PacketGroupId.ALARM.getValue());

		assertEquals(ParameterHeaderCode.ALARM_SUMMARY_STATUS	, packetId.getParameterHeaderCode());
		assertEquals(Packet.ALARM_SUMMARY_STATUS				, packetId.getParameterHeaderCode().getValue());

		AlarmSummaryStatusPacket packet = new AlarmSummaryStatusPacket();
		byte[] bytes = packet.toBytes();

		logger.trace("\n\t{}", ToHex.bytesToHex(bytes));
		logger.trace(packet);

		assertNotNull(bytes);

		byte[] packetAsBytes = Packet.getPacketAsBytes(PacketType.REQUEST, packetId, null);
		logger.trace("\n\t{}", ToHex.bytesToHex(packetAsBytes));

		assertTrue(Arrays.equals(packetAsBytes, bytes));
	}


	@Test
	public void testObserver() throws PacketParsingException {
		logger.entry();

		AlarmSummaryStatusPacket packet = new AlarmSummaryStatusPacket();
		packet.addObserver(new Observer() {

			@Override
			public void update(Observable o, Object arg) {
				logger.debug("\n\t Observable: {}\n", o);

				try {

					PacketAbstract bp = new PacketAbstract(AlarmIDsPacket.PACKET_ID, ((LinkedPacket)o).getAnswer()){};
					logger.debug("\n\t new PacketAbstract: {}\n", bp);
					assertEquals(PacketErrors.NO_ERROR, bp.getPacketHeader().getPacketErrors());
					assertEquals(1, bp.getPayloads().size());

					Payload payload = bp.getPayloads().get(0);
					assertTrue(payload.getParameterHeader().getPayloadSize().getSize()>0);

					final int status = payload.getInt(0) & 0x07;

					logger.trace("\n\tStatus: {}", status);
					logger.trace("\n\t{}", AlarmSummaryStatusPacket.AlarmSeverities.values()[status]);

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

	@Test
	public void comparPacketsTest() throws PacketParsingException{

		assertThat(new AlarmSummaryStatusPacket()		, is(new AlarmSummaryStatusPacket()));
		assertThat(new AlarmStatusPacket((short) 1)		, is(new AlarmStatusPacket((short) 1)));
		assertThat(new AlarmIDsPacket()					, is(new AlarmIDsPacket()));
		assertThat(new AlarmDescriptionPacket((short) 1), is(new AlarmDescriptionPacket((short) 1)));
		assertThat(new AlarmNamePacket((short) 1)		, is(new AlarmNamePacket((short) 1)));

		assertThat(new AlarmSummaryStatusPacket()		, not(new AlarmStatusPacket((short) 1)));
		assertThat(new AlarmSummaryStatusPacket()		, not(new AlarmIDsPacket()));
		assertThat(new AlarmSummaryStatusPacket()		, not(new AlarmDescriptionPacket((short) 1)));
		assertThat(new AlarmSummaryStatusPacket()		, not(new AlarmNamePacket((short) 1)));

		assertThat(new AlarmStatusPacket((short) 4)		, not(new AlarmStatusPacket((short) 1)));
		assertThat(new AlarmStatusPacket((short) 4)		, not(new AlarmIDsPacket()));
		assertThat(new AlarmStatusPacket((short) 4)		, not(new AlarmDescriptionPacket((short) 4)));
		assertThat(new AlarmStatusPacket((short) 4)		, not(new AlarmNamePacket((short) 4)));

		assertThat(new AlarmIDsPacket()					, not(new AlarmNamePacket((short) 1)));
		assertThat(new AlarmIDsPacket()					, not(new AlarmNamePacket((short) 4)));

		assertThat(new AlarmDescriptionPacket((short) 4), not(new AlarmDescriptionPacket((short) 1)));
		assertThat(new AlarmDescriptionPacket((short) 4), not(new AlarmNamePacket((short) 4)));
	}
}
