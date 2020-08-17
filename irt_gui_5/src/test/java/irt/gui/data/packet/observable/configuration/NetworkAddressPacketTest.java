
package irt.gui.data.packet.observable.configuration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
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
import irt.gui.controllers.PacketSenderJssc;
import irt.gui.data.NetworkAddress;
import irt.gui.data.ToHex;
import irt.gui.data.packet.Packet;
import irt.gui.data.packet.PacketProperties;
import irt.gui.data.packet.enums.PacketErrors;
import irt.gui.data.packet.enums.PacketId;
import irt.gui.data.packet.enums.PacketType;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.observable.PacketAbstract5;
import irt.gui.data.packet.observable.alarms.ObjectParsingException;
import irt.gui.errors.PacketParsingException;
import jssc.SerialPort;
import jssc.SerialPortException;

public class NetworkAddressPacketTest {

	Logger logger = LogManager.getLogger();
	private PacketSenderJssc port  = new PacketSenderJssc(ComPortTest.COM_PORT);

	@Test
	public void testRequest() throws PacketParsingException {
		NetworkAddressPacket packet = new NetworkAddressPacket((NetworkAddress)null);
		byte[] bytes = packet.toBytes();

		logger.trace("\n\t{}", ToHex.bytesToHex(bytes));
		logger.trace(packet);

		assertNotNull(bytes);

		byte[] packetAsBytes = Packet.getPacketAsBytes(PacketType.REQUEST, PacketId.CONFIGURATION_NETWORK_ADDRESS, null);
		logger.trace("\n\t{}", ToHex.bytesToHex(packetAsBytes));

		assertTrue(Arrays.equals(packetAsBytes, bytes));
	}

	@Test
	public void testCommand() throws PacketParsingException, ObjectParsingException {

		NetworkAddress value = new NetworkAddress();

		NetworkAddressPacket packet = new NetworkAddressPacket(value);
		byte[] bytes = packet.toBytes();

		logger.trace("\n\t{}", ToHex.bytesToHex(bytes));
		logger.trace(packet);

		assertNotNull(bytes);

		byte[] packetAsBytes = Packet.getPacketAsBytes(PacketType.COMMAND, PacketId.CONFIGURATION_NETWORK_ADDRESS, value.toBytes());
		logger.trace("\n\t{}", ToHex.bytesToHex(packetAsBytes));

		assertTrue(Arrays.equals(packetAsBytes, bytes));
	}


	@Test
	public void testObserver() throws PacketParsingException {
		logger.traceEntry();

		NetworkAddressPacket packet = new NetworkAddressPacket((NetworkAddress)null);
		packet.addObserver(new Observer() {

			@Override
			public void update(Observable o, Object arg) {
				logger.debug("\n\t Observable: {}\n", o);

				try {

					PacketAbstract5 packet = new PacketAbstract5(new PacketProperties(NetworkAddressPacket.PACKET_ID).setHasAcknowledgment(true), ((LinkedPacket)o).getAnswer()){

						@Override
						public PacketId getPacketId() {
							throw new UnsupportedOperationException("Auto-generated method stub");
						}};
					logger.debug("\n\t new PacketAbstract5: {}\n", packet);
					assertEquals(packet.getPacketHeader().getPacketError(), PacketErrors.NO_ERROR);

				} catch (PacketParsingException e) {
					logger.catching(e);
					assertTrue("\n\t PacketParsingException", false);
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

	@Test
	public void equalsTest() throws PacketParsingException{
		assertThat(new AttenuationPacket()			, is(new AttenuationPacket()));
		assertThat(new AttenuationRangePacket()	, is(new AttenuationRangePacket()));
		assertThat(new NetworkAddressPacket((NetworkAddress)null)	, is(new NetworkAddressPacket((NetworkAddress)null)));

		assertThat(new AttenuationPacket()			, not(new AttenuationRangePacket()));
		assertThat(new AttenuationPacket()			, not(new NetworkAddressPacket((NetworkAddress)null)));

		assertThat(new AttenuationRangePacket()	, not(new NetworkAddressPacket((NetworkAddress)null)));
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
