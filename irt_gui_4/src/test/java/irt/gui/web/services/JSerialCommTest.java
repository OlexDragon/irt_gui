package irt.gui.web.services;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import com.fazecast.jSerialComm.SerialPort;

import irt.gui.web.exceptions.IrtSerialPortIOException;

class JSerialCommTest {
	private final static Logger logger = LogManager.getLogger();

	private final JSerialComm port = new JSerialComm();

	@Test
	void openPortTest() throws IrtSerialPortIOException {

		final List<String> serialPortNames = port.getSerialPortNames();
		if(serialPortNames.size()!=1) {
			logger.error("Test completed. It is not clear which serial port to use.");
			return;
		}

		final String spName = serialPortNames.get(0);
		final SerialPort sp = port.open(spName, 115200);
		final long startTime = System.currentTimeMillis();

		while(sp.isOpen()) {
			final long currentTime = System.currentTimeMillis();
			if(currentTime-startTime>1200)
				fail("The serial port did not close after 10 seconds.");
		}

		final long currentTime = System.currentTimeMillis();
		final long time = currentTime-startTime;
		if(time<1000)
			fail("The serial port closed prematurely. - " + time);
	}

	@Test
	void sendTest() throws IrtSerialPortIOException {

		final List<String> serialPortNames = port.getSerialPortNames();
		if(serialPortNames.size()!=1) {
			logger.error("Test completed. It is not clear which serial port to use.");
			return;
		}

		byte[] bytes = new byte[] {126,(byte) 254,0,0,0,2,122,121,8,0,0,0,(byte) 255,0,0,104,(byte) 144,126};
		final String spName = serialPortNames.get(0);
		final byte[] answer = port.send(spName, 5000, bytes, null);
		assertNotNull(answer);
		assertTrue(answer.length>0);
		logger.error("{} : {}", answer.length, answer);
	}
}
