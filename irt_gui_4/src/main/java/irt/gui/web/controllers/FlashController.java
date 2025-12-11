package irt.gui.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import irt.gui.web.beans.Baudrate;
import irt.gui.web.services.IrtSerialPort;

@Controller
@RequestMapping({"f", "flash"})
public class FlashController {
//	private final static Logger logger = LogManager.getLogger();

	@Autowired @Qualifier("jSerialComm") IrtSerialPort serialPort;

	@Value("${info.app.version}")
	private String version;

	@GetMapping
    String flash(Model model) {

		model.addAttribute("version", version);
		model.addAttribute("baudrates", Baudrate.values());
		model.addAttribute("serialPorts", serialPort.getSerialPortNames());

		return "flash";
	}
}
