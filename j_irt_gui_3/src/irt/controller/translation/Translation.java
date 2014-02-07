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

	private static final Logger LOGGER = (Logger) LogManager.getLogger();

	private static final Preferences PREFS = GuiController.getPrefs();
	private static Locale locale;
	private static Font font;

	static {
		setLocale(PREFS.get("locale", DEFAULT_LANGUAGE));
	}

	private static Properties translationProperties;
	private static Map<String, String> map;
	private static ResourceBundle messages;

	public static void setLocale(final String localeStr){
		font = null;

		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {

				LOGGER.entry(localeStr);

				locale = new Locale(localeStr);

				messages = ResourceBundle.getBundle("irt.controller.translation.messageBundle", locale);
				map = getMap();

				if(!PREFS.get("locale", DEFAULT_LANGUAGE).equals(localeStr))
					PREFS.put("locale", localeStr);

				getFont(localeStr);
				LOGGER.exit(locale);
			}
		}, "setLocale "+localeStr);
		int priority = t.getPriority();
		if(priority>Thread.MIN_PRIORITY)
			t.setPriority(priority-1);
		t.setDaemon(true);
		t.start();
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
		LOGGER.entry(clazz, key, defaultValue);
		T returnValue = null;

		while(map==null)
			synchronized (LOGGER) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					LOGGER.catching(e);
				}
			}
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
				LOGGER.warn("Have to do implementation for '{}'", clazz);
			}
		}else{
			if(defaultValue!=null)
				LOGGER.warn("Con not find value for key={}, Used Default={}", key, defaultValue);
			returnValue = defaultValue;
		}

		return LOGGER.exit(returnValue);
	}

	private static Font getFont(String selectedLanguage) {
		LOGGER.entry(selectedLanguage);
		try {
			String fontURL = getValue(String.class, "font_path", "fonts/TAHOMA.TTF");

			int fontStyle = Font.BOLD;
			float fontSize = getValue(Float.class, "headPanel.font_size", 18f);

			if (fontURL != null && (font = getSystemFont(fontURL, fontStyle, (int) fontSize)) == null) {
				LOGGER.warn("The Operating System does not have {} font.", fontURL);
				URL resource = IrtGui.class.getResource(fontURL);
				font = Font.createFont(Font.TRUETYPE_FONT, resource.openStream());
				font = font.deriveFont(fontStyle).deriveFont(fontSize);
			}
		} catch (Exception e) {
			LOGGER.catching(e);
		}

		if(font==null)
			font = new Font("Tahoma", Font.PLAIN, 14);

		return LOGGER.exit(font);
	}

	public static Font getSystemFont(String fontURL, int fontStyle, int fontSize) {
		LOGGER.entry( fontURL, fontStyle, fontSize);

		Font font = null;
		String[] split = fontURL.split("/");
		String fontName = split[split.length-1].split("\\.")[0];

		String[] availableFontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		for(String s:availableFontNames)
			if(s.equalsIgnoreCase(fontName)){
				font = new Font(fontName, fontStyle, fontSize);
				break;
			}

		return LOGGER.exit(font);
	}

	public static Font getFont() {
		LOGGER.entry();
		while(font==null)
			synchronized (LOGGER) {
				try {
					LOGGER.trace("Wait for Font");
					Thread.sleep(400);
				} catch (InterruptedException e) {
					LOGGER.catching(e);
				}
			}
		return LOGGER.exit(font);
	}

	public static String getSelectedLanguage() {
		return LOGGER.exit(locale.toString());
	}

	public static void setFont(Font font) {
		LOGGER.trace("setFont({})", font);
		Translation.font = font;
	}

	public static Locale getLocale() {
		return LOGGER.exit(locale);
	}

	private static Properties getTranslationProperties() {
		LOGGER.entry();
		if(translationProperties==null){
			translationProperties = new Properties();
			try {
				translationProperties.load(Translation.class.getResourceAsStream("translation.properties"));
			} catch (Exception e) {
				LOGGER.catching(e);
			}
		}
		return LOGGER.exit(translationProperties);
	}

	public static String getTranslationProperties(String key) {
		LOGGER.entry(key);
		return LOGGER.exit(getTranslationProperties().getProperty(key));
	}
}
