
package irt.gui.data.packet.observable.configuration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import irt.gui.controllers.ComPortTest;
import irt.gui.controllers.LinkedPacketSender;
import irt.gui.data.NetworkAddress;
import irt.gui.data.ToHex;
import irt.gui.data.packet.Packet;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketErrors;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketId;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketType;
import irt.gui.data.packet.observable.PacketAbstract;
import irt.gui.data.packet.observable.alarms.ObjectParsingException;
import irt.gui.errors.PacketParsingException;
import jssc.SerialPortException;

public class NetworkAddressPacketTest {

	Logger logger = LogManager.getLogger();

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
		logger.entry();

		NetworkAddressPacket packet = new NetworkAddressPacket((NetworkAddress)null);
		packet.addObserver(new Observer() {

			@Override
			public void update(Observable o, Object arg) {
				logger.debug("\n\t Observable: {}\n", o);

				try {

					PacketAbstract packet = new PacketAbstract(NetworkAddressPacket.PACKET_ID, ((LinkedPacket)o).getAnswer()){

						@Override
						public PacketId getPacketId() {
							throw new UnsupportedOperationException("Auto-generated method stub");
						}};
					logger.debug("\n\t new PacketAbstract: {}\n", packet);
					assertEquals(packet.getPacketHeader().getPacketErrors(), PacketErrors.NO_ERROR);

				} catch (PacketParsingException e) {
					logger.catching(e);
					assertTrue("\n\t PacketParsingException", false);
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
	public void equalsTest() throws PacketParsingException{
		assertThat(new AttenuationPacket()			, is(new AttenuationPacket()));
		assertThat(new AttenuationRangePacket()	, is(new AttenuationRangePacket()));
		assertThat(new NetworkAddressPacket((NetworkAddress)null)	, is(new NetworkAddressPacket((NetworkAddress)null)));

		assertThat(new AttenuationPacket()			, not(new AttenuationRangePacket()));
		assertThat(new AttenuationPacket()			, not(new NetworkAddressPacket((NetworkAddress)null)));

		assertThat(new AttenuationRangePacket()	, not(new NetworkAddressPacket((NetworkAddress)null)));
	}
}
