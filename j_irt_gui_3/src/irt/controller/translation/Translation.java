package irt.controller.translation;

import irt.controller.GuiController;
import irt.irt_gui.IrtGui;
import irt.tools.panel.head.IrtPanel;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.net.URL;
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

	private static final String DEFAULT_LANGUAGE = "en";

	private static final Logger logger = (Logger) LogManager.getLogger(Translation.class);

	private static final Preferences PREFS = GuiController.getPrefs();
	private static Locale locale = setLocale(PREFS.get("locale", DEFAULT_LANGUAGE));

	private static ResourceBundle messages;
	private static Map<String, String> map;
	private static Font font;

	private static Properties translationProperties;

	public static Locale setLocale(String localeStr){

		logger.trace("setLocale({})", localeStr);

		locale = new Locale(localeStr);

		messages = ResourceBundle.getBundle("irt.controller.translation.messageBundle", locale);
		map = getMap();

		if(!PREFS.get("locale", DEFAULT_LANGUAGE).equals(localeStr))
			PREFS.put("locale", localeStr);

		getFont(localeStr);

		logger.debug("setLocale(), Locale={}", locale);
		return locale;
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

	@SuppressWarnings("unchecked")
	public static <T> T getValue(Class<T> clazz, String key, T defaultValue){
		logger.trace("getValue({}, {}, {})", clazz, key, defaultValue);
		T returnValue = null;

		logger.debug("map=", map);
		String stringValue = map.get(key);

		if(stringValue!=null){
			switch(clazz.getName()){
			case "java.lang.Integer":
				String tmp = stringValue.replaceAll("\\D", "");
				if(tmp.isEmpty())
					returnValue = (T) IrtPanel.parseFontStyle(stringValue);
				else
					returnValue = (T) new Integer(Integer.parseInt(tmp));
				break;
			case "java.lang.Float":
				returnValue = (T) new Float(Float.parseFloat(stringValue));
				break;
			case "java.lang.String":
				returnValue = (T) stringValue;
				break;
			case "java.awt.Rectangle":
				String[] split = stringValue.split(",");
				if(split.length==4)
					returnValue = (T) new Rectangle(Integer.parseInt(split[0]),
													Integer.parseInt(split[1]),
													Integer.parseInt(split[2]),
													Integer.parseInt(split[3]));
				break;
			default:
				logger.warn("Have to do implementation for '{}'", clazz);
			}
		}else{
			if(defaultValue!=null)
				logger.warn("Con not find value for key={}, Used Default={}", key, defaultValue);
			returnValue = defaultValue;
		}

		logger.debug("getValue(key={})={}, stringValue={}", key, returnValue, stringValue);
		return returnValue;
	}

	private static Font getFont(String selectedLanguage) {
		logger.trace("getFont(selectedLanguage={})", selectedLanguage);
		try {
			String fontURL = getValue(String.class, "font_path", "fonts/TAHOMA.TTF");
			logger.trace("fontURL={}", fontURL);

			int fontStyle = Font.BOLD;
			float fontSize = getValue(Float.class, "headPanel.font_size", 18f);

			if (fontURL != null && (font = getSystemFont(fontURL, fontStyle, (int) fontSize)) == null) {
				logger.warn("The Operating System does not have {} font.", fontURL);
				URL resource = IrtGui.class.getResource(fontURL);
				font = Font.createFont(Font.TRUETYPE_FONT, resource.openStream());
				font = font.deriveFont(fontStyle).deriveFont(fontSize);
			}
		} catch (Exception e) {
			logger.catching(e);
		}

		if(font==null)
			font = new Font("Tahoma", Font.PLAIN, 14);

		return font;
	}

	public static Font getSystemFont(String fontURL, int fontStyle, int fontSize) {

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
		logger.debug(font);

		return font;
	}

	public static Font getFont() {
		logger.trace("getFont()={}", font);
		return font;
	}

	public static String getSelectedLanguage() {
		logger.trace("getSelectedLanguage(), locale={}", locale);

		return locale.toString();
	}

	public static void setFont(Font font) {
		logger.trace("setFont({})", font);
		Translation.font = font;
	}

	public static Locale getLocale() {
		logger.trace("getLocale()", font);
		return locale;
	}

	private static Properties getTranslationProperties() {
		if(translationProperties==null){
			translationProperties = new Properties();
			try {
				translationProperties.load(Translation.class.getResourceAsStream("translation.properties"));
			} catch (Exception e) {
				logger.catching(e);
			}
		}
		return translationProperties;
	}

	public static String getTranslationProperties(String key) {
		return getTranslationProperties().getProperty(key);
	}
}
