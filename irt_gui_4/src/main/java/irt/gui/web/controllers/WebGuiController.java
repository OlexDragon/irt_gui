package irt.gui.web.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import irt.gui.web.beans.Baudrate;

@Controller
public class WebGuiController {
//	private final static Logger logger = LogManager.getLogger();

	@Value("${info.app.version}")
	private String version;


	@GetMapping
    String home(Model model) {
		model.addAttribute("version", version);
		model.addAttribute("baudrates", Baudrate.values());
		return "home";
	}
}
