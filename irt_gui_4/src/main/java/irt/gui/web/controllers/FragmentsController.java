package irt.gui.web.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("fragment")
public class FragmentsController {
	private final static Logger logger = LogManager.getLogger();

	@GetMapping("control/{name}")
    String control(@PathVariable(value = "name") String name) {
		logger.error("control {}", name);
		return "fragments/control :: " + name;
	}

	@GetMapping("network/{name}")
    String network(@PathVariable(value = "name") String name) {
		logger.error("network {}", name);
		return "fragments/network :: " + name;
	}

	@GetMapping("alarms/{name}")
    String alarms(@PathVariable(value = "name") String name) {
		logger.error("alarms {}", name);
		return "fragments/alarms :: " + name;
	}
}
