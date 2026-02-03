package irt.gui.web.controllers;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import irt.gui.web.beans.Baudrate;

@Controller
@RequestMapping({"console", "c"})
public class ConsoleController {
	private final static Logger logger = LogManager.getLogger();

	@Value("${info.app.version}")
	private String version;

	@GetMapping
    String console(@CookieValue(required = false) String localeInfo, Model model) {
		logger.traceEntry("localeInfo ='{}'", localeInfo);

		// Set Language
		Optional.ofNullable(localeInfo).filter(s->s.equals("fr") || s.equals("en")).ifPresent(s->model.addAttribute("lang", s));

		model.addAttribute("version", version);
		model.addAttribute("baudrates", Baudrate.values());
		return "console";
	}
}
