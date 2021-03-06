package resources;

import irt.controller.GuiController;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class Translation {

	private static final Preferences PREFS = GuiController.getPrefs();

	private static String locate = PREFS.get("locate", "en,US");
	private static ResourceBundle messages = ResourceBundle.getBundle("resources.translation.messageBundle", new Locale(locate.split(",")[0], locate.split(",")[1]));
	private static Map<String, String> map = getMap();

	public static void setLocate(String locate){
		String[] splitLocate = locate.split(",");
		messages = ResourceBundle.getBundle("resources.translation.messageBundle", new Locale(splitLocate[0], splitLocate[1]));
		map = getMap();
		PREFS.put("locate", locate);
	}

	private static Map<String, String> getMap() {

		Map<String, String> map = new HashMap<String, String>();
		Enumeration<String> keys = messages.getKeys();

		while(keys.hasMoreElements()){
			String nextElement = keys.nextElement();
			map.put(nextElement, messages.getString(nextElement));
		}
		return map;
	}

	public static String getLocate() {
		return locate;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getValue(Class<T> clazz, String key, T defaultValue){
		T returnValue = null;

		String stringValue = map.get(key);

		if(stringValue!=null){
			switch(clazz.getName()){
			case "java.lang.Integer":
				stringValue = stringValue.replaceAll("\\D", "");
				if(!stringValue.isEmpty())
					returnValue = (T) new Integer(Integer.parseInt(stringValue));
				break;
			case "java.lang.Float":
				returnValue = (T) new Float(Float.parseFloat(stringValue));
				break;
			case "java.lang.String":
				returnValue = (T) stringValue;
			}
		}else
			returnValue = defaultValue;

		return returnValue;
	}

	public static Font replaceFont(String fontKey, String fontSizeKey, Font defaultFont, float defaultFontSize) {
		Font font = null;
		try {

			String fontURL = Translation.getValue(String.class, fontKey, null);
			font = fontURL==null ? defaultFont : Font.createFont(Font.TRUETYPE_FONT, Translation.class.getClassLoader().getResource(fontURL).openStream());
			if(!font.equals(defaultFont))
				font = font.deriveFont(Translation.getValue(Float.class, fontSizeKey, defaultFontSize));

		} catch (FontFormatException | IOException e) {
			font = defaultFont;
		}
		return font;
	}
}
