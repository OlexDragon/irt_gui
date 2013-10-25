package irt.controller.translation;

import irt.controller.GuiController;
import irt.irt_gui.IrtGui;
import irt.tools.panel.head.HeadPanel;
import irt.tools.panel.head.IrtPanel;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class Translation {

	private static final Logger logger = (Logger) LogManager.getLogger(Translation.class);

	public static final String SPLITER = "_";

	private static final Preferences PREFS = GuiController.getPrefs();

	private static volatile String selectedLanguage = PREFS.get("locale", "en_US");
	private static Locale locale = new Locale(selectedLanguage.split(SPLITER)[0], selectedLanguage.split(SPLITER)[1]);
	private static ResourceBundle messages = ResourceBundle.getBundle("irt.controller.translation.messageBundle", new Locale(selectedLanguage.split(SPLITER)[0], selectedLanguage.split(SPLITER)[1]));
	private static Map<String, String> map = getMap();
	private static Font font = getFont(selectedLanguage);

	public static void setLocale(String localeStr){
		logger.trace("setLocale({})", localeStr);
		getFont(localeStr);
		String[] splitLocale = localeStr.split(SPLITER);
		locale = new Locale(splitLocale[0], splitLocale[1]);
		messages = ResourceBundle.getBundle("irt.controller.translation.messageBundle", locale);
		map = getMap();
		PREFS.put("locale", localeStr);
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

	public static String getLocateStr() {
		return selectedLanguage;
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
		Translation.selectedLanguage = selectedLanguage;
		try {
			Properties headPanelProperties = HeadPanel.properties;
			String fontURL = headPanelProperties.getProperty("font_path_" + Translation.getSelectedLanguage());
			logger.trace("fontURL={}", fontURL);

			int fontStyle = IrtPanel.fontStyle.get(headPanelProperties.getProperty("font_style_" + selectedLanguage));
			float fontSize = Float.parseFloat(headPanelProperties.getProperty("font_size_" + selectedLanguage));

			if (fontURL != null && (font = getSystemFont(fontURL, fontStyle, (int) fontSize)) == null) {
				URL resource = IrtGui.class.getResource(fontURL);
				font = Font.createFont(Font.TRUETYPE_FONT, resource.openStream());
				font = font.deriveFont(fontStyle).deriveFont(fontSize);
			}
		} catch (IOException | FontFormatException e) {
			logger.catching(e);
		}
		return font;
	}

	private static Font getSystemFont(String fontURL, int fontStyle, int fontSize) {

		Font font = null;
		String[] split = fontURL.split("/");
		String fontName = split[split.length-1].split("\\.")[0];

		String[] availableFontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		for(String s:availableFontNames)
			if(s.equalsIgnoreCase(fontName)){
				font = new Font(fontName, fontStyle, fontSize);
				break;
			}

		logger.debug(fontURL);
		logger.debug(Arrays.toString(availableFontNames));
		logger.debug(font);

		return font;
	}

	public static Font getFont() {
		return font;
	}

	public static String getSelectedLanguage() {
		return selectedLanguage!=null && !selectedLanguage.isEmpty() ? selectedLanguage : "us_US";
	}

	public static void setFont(Font font) {
		Translation.font = font;
	}

	public static void setSelectedLanguage(String selectedLanguage) {
		Translation.selectedLanguage = selectedLanguage;
	}

	public static Locale getLocale() {
		return locale;
	}

	public static void setLocace(Locale locace) {
		Translation.locale = locace;
	}
}
