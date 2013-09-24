package resources;

import irt.controller.GuiController;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class Tanslation {

	private static final Preferences PREFS = GuiController.getPrefs();

	private static String locate = PREFS.get("locate", "en,US");
	public static ResourceBundle messages = ResourceBundle.getBundle("resources.translation.messageBundle", new Locale(locate.split(",")[0], locate.split(",")[1]));

	public static void setLocate(String locate){
		String[] splitLocate = locate.split(",");
		messages = ResourceBundle.getBundle("resources.translation.messageBundle", new Locale(splitLocate[0], splitLocate[1]));
		PREFS.put("locate", locate);
	}

	public static String getLocate() {
		return locate;
	}
}
