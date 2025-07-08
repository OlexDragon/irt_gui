package irt.gui.web.beans;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import irt.gui.web.services.JSerialComm;

class RequestPacketTest {
	private final static Logger logger = LogManager.getLogger();

	@Test
	void test() throws JsonProcessingException {

		final JSerialComm port = new JSerialComm();
		final List<String> serialPortNames = port.getSerialPortNames();
		if(serialPortNames.size()!=1) {
			logger.error("Test completed. It is not clear which serial port to use.");
			return;
		}

		final String spName = serialPortNames.get(0);
		byte[] bytes = new byte[] {126,(byte) 254,0,0,0,2,122,121,8,0,0,0,(byte) 255,0,0,104,(byte) 144,126};

		final RequestPacket requestPacket = new RequestPacket(false, 1, 0, spName, bytes, spName);
		String json = new ObjectMapper().writeValueAsString(requestPacket);
		logger.error(json);
	}

	@Test
	void jsonTest() throws JsonProcessingException {

		byte[] bytes = new byte[] {126,(byte) 254,0,0,0,2,122,121,8,0,0,0,(byte) 255,0,0,104,(byte) 144,126};

		final RequestPacket requestPacket = new RequestPacket(false, 1, 0, "COM Port", bytes, "");
		String json = new ObjectMapper().writeValueAsString(requestPacket);
		logger.error(json);

		assertEquals("{\"port\":\"COM Port\",\"bytes\":[126,254,0,0,0,2,122,121,8,0,0,0,255,0,0,104,144,126],\"baudrate\":null,\"timeout\":null,\"answer\":null,\"function\":null}", json);
	}

}
