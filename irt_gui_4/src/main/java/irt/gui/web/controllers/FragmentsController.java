package irt.gui.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("fragment")
public class FragmentsController {
//	private final static Logger logger = LogManager.getLogger();

	@GetMapping("control/{name}")
    String control(@PathVariable(value = "name") String name) {
		return "fragments/control :: " + name;
	}

	@GetMapping("network/{name}")
    String network(@PathVariable(value = "name") String name) {
		return "fragments/network :: " + name;
	}

	@GetMapping("redundancy/{name}")
    String alarms(@PathVariable(value = "name") String name) {
		return "fragments/redundancy :: " + name;
	}
}
