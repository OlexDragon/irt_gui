package irt.gui.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("fragment")
public class FragmentsController {
//	private final static Logger logger = LogManager.getLogger();

	private final String template = "fragments/%s :: %s";

	@GetMapping("{pageName}/{name}")
    String control(@PathVariable String pageName, @PathVariable String name) {
//		logger.error("pageName: {}; name: {};", pageName, name);

		return String.format(template, pageName, name);
	}
}
