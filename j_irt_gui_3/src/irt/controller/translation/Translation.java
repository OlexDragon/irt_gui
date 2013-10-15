package irt.controller.translation;

import irt.controller.GuiController;
import irt.irt_gui.IrtGui;
import irt.tools.panel.head.HeadPanel;
import irt.tools.panel.head.IrtPanel;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class Translation {

	public static final String SPLITER = "_";

	private static final Preferences PREFS = GuiController.getPrefs();

	private static volatile String locate = PREFS.get("locate", "en_US");
	private static ResourceBundle messages = ResourceBundle.getBundle("irt.controller.translation.messageBundle", new Locale(locate.split(SPLITER)[0], locate.split(SPLITER)[1]));
	private static Map<String, String> map = getMap();

	private static Font font = getFont(locate);

	public static void setLocate(String locate){
		String[] splitLocate = locate.split(SPLITER);
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

	public static Font getFont(String selectedLanguage) {
		locate = selectedLanguage;
		try {
			Properties headPanelProperties = HeadPanel.properties;
			String fontURL = headPanelProperties.getProperty("font_path_"+Translation.getSelectedLanguage());
			if(fontURL!=null){
				URL resource = IrtGui.class.getResource(fontURL);
				font = Font.createFont(Font.TRUETYPE_FONT, resource.openStream());
				int fontStyle = IrtPanel.fontStyle.get(headPanelProperties.getProperty("font_style_"+selectedLanguage));
				float fontSize = Float.parseFloat(headPanelProperties.getProperty("font_size_"+selectedLanguage));
				font = font.deriveFont(fontStyle).deriveFont(fontSize);
			}
		} catch (IOException | FontFormatException e) {
			e.printStackTrace();
		}
		return font;
	}

	public static Font getFont() {
		return font;
	}

	public static String getSelectedLanguage() {
		return locate;
	}

	public static void setFont(Font font) {
		Translation.font = font;
	}

	public static void setSelectedLanguage(String selectedLanguage) {
		locate = selectedLanguage;
	}
}
