
package irt.gui.controllers.socket;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import irt.gui.controllers.LinkedPacketSender;
import irt.gui.controllers.LinkedPacketsQueue;
import irt.gui.data.packet.observable.InfoPacket;
import irt.gui.errors.PacketParsingException;
import jssc.SerialPortException;

public class SocketWorkerTest {
	public static final String COM_PORT = "COM13";

	private final Logger logger = LogManager.getLogger();
	private SocketWorker socketWorker;
	private LinkedPacketSender serialPort;
	private LinkedPacketsQueue queue;

	@Before
	public void setup(){
		logger.entry();
		socketWorker = new SocketWorker();
		socketWorker.startServer(COM_PORT);
		serialPort = new LinkedPacketSender(COM_PORT);
		queue = new LinkedPacketsQueue();
		queue.setComPort(serialPort);
		try {
			serialPort.openPort();
		} catch (SerialPortException e) {
			logger.catching(e);
		}
	}

	@Test
	public void testPort() {
		logger.entry();
		final Integer expected = getSocketPort();
		assertEquals(expected, socketWorker.getLocalPort());
	}

	public Integer getSocketPort() {
		final int port = 10000 + Integer.parseInt(COM_PORT.replaceAll("\\D", ""));
		return new Integer(port);
	}

	@Test
	public void testCommunication() throws PacketParsingException, InterruptedException, JsonProcessingException {
		logger.entry();
		final ClientSocket clientSocket = socketWorker.getClientSocket(null, getSocketPort());
		clientSocket.send(new InfoPacket());

		final Integer expected = getSocketPort();
		assertEquals(expected, socketWorker.getLocalPort());

		clientSocket.getExecutorService().awaitTermination(1, TimeUnit.SECONDS);
	}

	@After
	public void end() throws SerialPortException{
		serialPort.closePort();
	}
}
