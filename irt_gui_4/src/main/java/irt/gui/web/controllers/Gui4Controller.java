package irt.gui.web.controllers;

import java.util.prefs.Preferences;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import irt.gui.web.beans.Baudrate;

@Controller
public class Gui4Controller {
//	private final static Logger logger = LogManager.getLogger();

	@Autowired
	private Preferences prefs;

	@Value("${info.app.version}")
	private String version;

	@GetMapping
    String home(Model model) {

		final String prefVertion = prefs.get("version", "");
		
		if(prefVertion.isEmpty())
			prefs.put("version", version);

		else if(!prefVertion.equals(version)) {
			prefs.put("version", version);
			model.addAttribute("cleareCash", true);
		}

		model.addAttribute("version", version);
		model.addAttribute("baudrates", Baudrate.values());
		return "home";
	}

	@GetMapping("regs")
    String regs(Model model) {
		model.addAttribute("version", version);
		model.addAttribute("baudrates", Baudrate.values());
		return "regs";
	}
}
