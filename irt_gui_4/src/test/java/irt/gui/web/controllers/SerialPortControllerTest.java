package irt.gui.web.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import irt.gui.web.beans.RequestPacket;
import irt.gui.web.services.JSerialComm;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class SerialPortControllerTest {
	private final static Logger logger = LogManager.getLogger();

	@Autowired private MockMvc mockMvc;

	@Test
	void test() throws Exception {

		final String jSon = getJSonObject();
		if(jSon.isEmpty()) {
			logger.error("Test completed. It is not clear which serial port to use.");
			return;
		}

		mockMvc.perform(
				post("/serial/send")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jSon))
				.andExpect(status().isOk());
	}

	// ***********************************************************************************
	public String getJSonObject() throws JsonProcessingException {

		final JSerialComm port = new JSerialComm();
		final List<String> serialPortNames = port.getSerialPortNames();
		if(serialPortNames.size()!=1)
			return "";

		final String spName = serialPortNames.get(0);
		byte[] bytes = new byte[] {126,(byte) 254,0,0,0,2,122,121,8,0,0,0,(byte) 255,0,0,104,(byte) 144,126};

		final RequestPacket requestPacket = new RequestPacket(false, 1, 0, spName, bytes, spName);
		return new ObjectMapper().writeValueAsString(requestPacket);
	}

}
