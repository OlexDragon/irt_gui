package irt.gui.controllers;

import java.util.HashSet;
import java.util.Set;

import irt.gui.controllers.interfaces.FieldController;


/**
 * @author Alex
 * 	Manages the added controllers.
 */
public class UpdateController {

	private final static Set<FieldController> controllers = new HashSet<>();
	private static String senderId;		public static String getSenderId() { return senderId; }

	public static void addController(FieldController controller) {
		controllers.add(controller);
	}

	/**
	 * @param senderId - 
	 */
	public static void start(String senderId){

		UpdateController.senderId = senderId;

		controllers
		.parallelStream()
		.forEach(c->c.doUpdate(true));
	}

	public static void stop(String senderId){

		UpdateController.senderId = senderId;

		controllers
		.parallelStream()
		.forEach(c->c.doUpdate(false));
	}
}
