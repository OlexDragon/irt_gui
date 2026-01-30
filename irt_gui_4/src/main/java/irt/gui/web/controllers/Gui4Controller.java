package irt.gui.web.controllers;

import java.util.Map;
import java.util.Optional;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import irt.gui.web.beans.Baudrate;

@Controller
public class Gui4Controller {
	private final static Logger logger = LogManager.getLogger();

	@Autowired private Preferences prefs;

	@Value("${info.app.version}")
	private String version;

	@GetMapping
    String home(@CookieValue(required = false) String localeInfo, Model model) {
		logger.traceEntry("localeInfo ='{}'", localeInfo);

		// Set Language
		Optional.ofNullable(localeInfo).filter(s->s.equals("fr") || s.equals("en")).ifPresent(s->model.addAttribute("lang", s));

		checkVersion(model);

		model.addAttribute("version", version);
		model.addAttribute("baudrates", Baudrate.values());
		return "home";
	}

	public void checkVersion(Model model) {
		final String prefVertion = prefs.get("version", "");
		
		if(prefVertion.isEmpty())
			prefs.put("version", version);

		else if(!prefVertion.equals(version)) {
			prefs.put("version", version);
			model.addAttribute("cleareCash", true);
		}
	}

	@GetMapping({"p", "production"})
    String regs(Model model) {
		model.addAttribute("version", version);
		model.addAttribute("baudrates", Baudrate.values());
		return "production";
	}

	@GetMapping({"login"})
    String login(@RequestParam Map<String, Object> map, Model model) {
		final Object message = map.get("error");
		if(message!=null)
			model.addAttribute("message", "Incorrect password. Try again.");
		return "login";
	}

	@GetMapping({"test"})
    String test(Model model) {
		model.addAttribute("version", version);
		model.addAttribute("baudrates", Baudrate.values());
		return "test";
	}
}
