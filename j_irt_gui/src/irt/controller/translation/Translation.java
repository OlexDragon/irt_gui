package irt.controller.translation;

import irt.controller.GuiController;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class Translation {

	private static final Preferences PREFS = GuiController.getPrefs();

	private static String locate = PREFS.get("l", "en_US");
	private static ResourceBundle messages = ResourceBundle.getBundle("irt.controller.translation.messageBundle", new Locale(locate.split(",")[0], locate.split(",")[1]));
	private static Map<String, String> map = getMap();

	public static void setLocate(String locate){
		System.out.println(locate);
		String[] splitLocate = locate.split("_");
		messages = ResourceBundle.getBundle("irt.controller.translation.messageBundle", new Locale(splitLocate[0], splitLocate[1]));
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
}
