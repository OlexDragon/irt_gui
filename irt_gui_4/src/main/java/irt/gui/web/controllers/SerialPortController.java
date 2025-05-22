package irt.gui.web.controllers;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import irt.gui.web.beans.RequestPacket;
import irt.gui.web.services.IrtSerialPort;
import irt.gui.web.services.SerialPortDistributor;

@RestController
@RequestMapping("serial")
public class SerialPortController {
	private final static Logger logger = LogManager.getLogger();

	@Autowired SerialPortDistributor distributor;

	@Autowired @Qualifier("jSerialComm") IrtSerialPort serialPort;

	@RequestMapping("ports")
    List<String> ports() {
		return serialPort.getSerialPortNames();
	}

	@PostMapping("send")
    RequestPacket send(@RequestBody RequestPacket requestPacket, HttpServletRequest request){
		logger.traceEntry("{}", requestPacket);
		final String headerNames = request.getHeader("User-Agent");
		logger.error(headerNames);

		final FutureTask<RequestPacket> respose = distributor.send(requestPacket);

		try {

			return respose.get(10, TimeUnit.SECONDS);

		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			logger.catching(Level.DEBUG, e);
			throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, e.getLocalizedMessage());
		}catch (CancellationException e) {
			logger.catching(Level.DEBUG, e);
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, e.getLocalizedMessage());
		}
	}

//	@ExceptionHandler(IrtSerialPortException.class)
//	  public void conflict(Exception e) {
//		logger.catching(Level.DEBUG, e);
//	  }
}
