package irt.gui.web.controllers;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import irt.gui.web.beans.RequestPacket;
import irt.gui.web.services.IrtSerialPort;
import irt.gui.web.services.SerialPortDistributor;
import lombok.NonNull;

@RestController
@RequestMapping("console/rest")
public class ConsoleRestController {
	private final static Logger logger = LogManager.getLogger();

	static final @NonNull
	public Integer PACKET_ID = UpgradeRestController.PACKET_ID + 1;

	@Autowired @Qualifier("jSerialComm") IrtSerialPort serialPort;

	@Autowired SerialPortDistributor distributor;

	@Value("${irt.console.timeout.ms}")
	private int consoleTimeoutMs;

	@RequestMapping("send")
	RequestPacket send(@RequestParam String sp, @RequestParam Integer br, @RequestParam String command) throws InterruptedException, ExecutionException, TimeoutException{
		logger.traceEntry("spName={}, baudrate={}, command={}", sp, br, command);

		if(command==null || command.isEmpty())
			command = "\n";
		else if(command.charAt(command.length()-1)!='\n')
			command+='\n';

		final RequestPacket requestPacket = new RequestPacket(false, PACKET_ID, 1, sp, command.getBytes(), "unused");
		requestPacket.setName("Console");
		requestPacket.setBaudrate(br);
		requestPacket.setTimeout(consoleTimeoutMs);
		distributor.send(requestPacket);
		return distributor.send(requestPacket).get(10, TimeUnit.SECONDS);
	}
}
