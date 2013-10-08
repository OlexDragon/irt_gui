package irt.controller.translation;

import irt.controller.GuiController;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import org.junit.Test;

public class TranslationTest {

	@Test
	public void test() {
		Preferences PREFS = GuiController.getPrefs();
		String locate = PREFS.get("locate", "en,US");
		ResourceBundle messages = ResourceBundle.getBundle(
				"irt.controller.translation.messageBundle",
				new Locale(locate.split(",")[0],
						locate.split(",")[1]));
		System.out.println(messages);
	}

}
