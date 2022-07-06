
package irt.gui.controllers.socket;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import irt.gui.controllers.ComPortTest;
import irt.gui.controllers.LinkedPacketsQueue;
import irt.gui.controllers.serial_port.PacketSenderJssc;
import irt.gui.data.packet.observable.InfoPacket;
import irt.gui.errors.PacketParsingException;
import jssc.SerialPortException;

public class SocketWorkerTest {

	private final Logger logger = LogManager.getLogger();
	private SocketWorker socketWorker;
	private PacketSenderJssc port  = new PacketSenderJssc(ComPortTest.COM_PORT);
	private LinkedPacketsQueue queue;

	@Before
	public void setup(){
		logger.traceEntry();
		socketWorker = new SocketWorker();
		socketWorker.startServer(ComPortTest.COM_PORT);
		queue = new LinkedPacketsQueue();
		queue.setComPort(port);
		try {
			port.openPort();
		} catch (SerialPortException e) {
			logger.catching(e);
		}
	}

	@Test
	public void testPort() {
		logger.traceEntry();
		final Integer expected = getSocketPort();
		assertEquals(expected, socketWorker.getLocalPort());
	}

	public Integer getSocketPort() {
		final int port = 10000 + Integer.parseInt(ComPortTest.COM_PORT.replaceAll("\\D", ""));
		return new Integer(port);
	}

	@Test
	public void testCommunication() throws PacketParsingException, InterruptedException, JsonProcessingException {
		logger.traceEntry();
		final ClientSocket clientSocket = socketWorker.getClientSocket(null, getSocketPort());
		clientSocket.send(new InfoPacket());

		final Integer expected = getSocketPort();
		assertEquals(expected, socketWorker.getLocalPort());

		clientSocket.getExecutorService().awaitTermination(1, TimeUnit.SECONDS);
	}

	@After
	public void end() throws SerialPortException{
		port.closePort();
	}
}
