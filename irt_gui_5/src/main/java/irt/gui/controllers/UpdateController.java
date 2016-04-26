
package irt.gui.controllers;

import java.util.ArrayList;
import java.util.List;

import irt.gui.controllers.interfaces.FieldController;

public class UpdateController {

	private final static List<FieldController> controllers = new ArrayList<>();

	public static void addController(FieldController infoController) {
		controllers.add(infoController);
	}

	public static void start(){
		controllers
		.parallelStream()
		.forEach(c->c.doUpdate(true));
	}

	public static void stop(){
		controllers
		.parallelStream()
		.forEach(c->c.doUpdate(false));
	}
}
