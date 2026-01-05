package irt.gui.web.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import irt.gui.web.beans.Baudrate;

@Controller
@RequestMapping({"console", "c"})
public class ConsoleController {
//	private final static Logger logger = LogManager.getLogger();

	@Value("${info.app.version}")
	private String version;

	@GetMapping
    String console(Model model) {
		model.addAttribute("version", version);
		model.addAttribute("baudrates", Baudrate.values());
		return "console";
	}
}
