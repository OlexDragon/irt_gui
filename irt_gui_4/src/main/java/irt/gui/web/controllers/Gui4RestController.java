package irt.gui.web.controllers;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import irt.gui.web.Gui4;
import irt.gui.web.services.IrtSerialPort;
import irt.gui.web.services.SerialPortDistributor;
import irt.gui.web.services.ThreadWorker;
@RestController
public class Gui4RestController {
	private final static Logger logger = LogManager.getLogger();

	@Autowired @Qualifier("jSerialComm") IrtSerialPort serialPort;
	@Autowired SerialPortDistributor distributor;

	@RequestMapping("ping")
    Boolean ping() {
		return true;
	}

	@RequestMapping("exit")
    Boolean exit() {

		serialPort.shutdown();
		distributor.shutdown();

		ThreadWorker.runThread(()->{

			try {
				TimeUnit.SECONDS.sleep(1);

			} catch (InterruptedException e) {
				logger.catching(Level.DEBUG, e);
			}

			Gui4.exit();
		});
		return true;
	}

}
