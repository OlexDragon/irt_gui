
package irt.gui.data.packet.observable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Observable;
import java.util.Observer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Test;

import irt.gui.controllers.ComPortTest;
import irt.gui.controllers.LinkedPacketSender;
import irt.gui.data.ToHex;
import irt.gui.data.packet.Packet;
import irt.gui.data.packet.PacketProperties;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.enums.PacketErrors;
import irt.gui.data.packet.enums.PacketId;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.observable.alarms.AlarmIDsPacket;
import irt.gui.data.packet.observable.alarms.AlarmNamePacket;
import irt.gui.errors.PacketParsingException;
import jssc.SerialPortException;

public class TestPacketTest {

	Logger logger = LogManager.getLogger();
	private LinkedPacketSender port  = new LinkedPacketSender(ComPortTest.COM_PORT);

	@Test
	public void AlarmIDsPacketTest() throws PacketParsingException, InterruptedException {

		AlarmIDsPacket packet = new AlarmIDsPacket();
		doTest(packet);
	}

	@Test
	public void AlarmIDsPacketTest2() throws PacketParsingException, InterruptedException {
		final byte[] bytes = Packet.toBytes(PacketId.ALARMS.getValue());
		LinkedPacket packet = new TestPacket(new byte[]{(byte)0xFE, 0x00, 0x00, 0x00, 0x02, /*(byte)0x9A, 0x78, replaced by PacketId.ALARMS*/ bytes[0], bytes[1], 0x01, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00});
		logger.trace("\n\t packet.getAcknowledgement(): {}", ToHex.bytesToHex(packet.getAcknowledgement()));
		doTest(packet);
	}

	@Test
	public void AlarmSummaryTest() throws PacketParsingException, InterruptedException {
		final byte[] bytes = Packet.toBytes(PacketId.ALARM_NAME.getValue());
		LinkedPacket packet = new TestPacket(new byte[]{(byte)0xFE, 0x00, 0x00, 0x00, 0x02, /*(byte)0x9D, 0x78,, replaced by PacketId.ALARM_NAME*/ bytes[0], bytes[1],0x01, 0x00, 0x00, 0x00, 0x04, 0x00, 0x02, 0x00, 0x04, 0x05, 0x00, 0x02, 0x00, 0x04, 0x06, 0x00, 0x02, 0x00, 0x04, 0x07, 0x00, 0x02, 0x00, 0x04});
		logger.trace("\n\t packet.getAcknowledgement(): {}", ToHex.bytesToHex(packet.getAcknowledgement()));
		doTest(packet);
	}

	@Test
	public void AlarmNameTest() throws PacketParsingException, InterruptedException {
		LinkedPacket packet = new AlarmNamePacket((short) 4);
		logger.trace("\n\t packet.getAcknowledgement(): {}", ToHex.bytesToHex(packet.getAcknowledgement()));
		doTest(packet);
	}

	@Test
	public void AlarmNameTest2() throws PacketParsingException, InterruptedException {
		final byte[] bytes = Packet.toBytes(PacketId.ALARM_NAME.getValue());
		LinkedPacket packet = new TestPacket(new byte[]{(byte)0xFE, 0x00, 0x00, 0x00, 0x02, /*(byte)0x9D, 0x78,, replaced by PacketId.ALARM_NAME*/ bytes[0], bytes[1],0x01, 0x00, 0x00, 0x00, 0x07, 0x00, 0x02, 0x00, 0x04});
		logger.trace("\n\t packet.getAcknowledgement(): {}", ToHex.bytesToHex(packet.getAcknowledgement()));
		doTest(packet);
	}

	private void doTest(LinkedPacket packet) throws InterruptedException {
		logger.trace("\n\t {}{}", ToHex.bytesToHex(packet.toBytes()), packet);

		Thread.sleep(500);
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
					assertTrue(bp.getPayloads().size()>0);

					Payload payload = bp.getPayloads().get(0);
					assertTrue(payload.getParameterHeader().getPayloadSize().getSize()>0);

					logger.trace("\n\t alarm IDs:\n\t{}", payload.getArrayOfShort());

				} catch (PacketParsingException e) {
					logger.catching(e);
					assertTrue("Packet Parsing error", false);
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
	}

	@After
	public void exit() throws SerialPortException {
		port.closePort();
	}
}
