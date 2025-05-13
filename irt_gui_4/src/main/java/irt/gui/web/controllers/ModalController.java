package irt.gui.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("modal")
public class ModalController {
//	private final static Logger logger = LogManager.getLogger();


	@GetMapping("exit")
    String home() {
		return "modal/exit :: exit";
	}
}
