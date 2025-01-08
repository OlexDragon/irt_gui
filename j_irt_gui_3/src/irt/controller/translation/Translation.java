package irt.controller.translation;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiController;
import irt.data.ThreadWorker;
import irt.irt_gui.IrtGui;
import irt.tools.panel.head.IrtPanel;

public class Translation {

	private static final Logger logger = LogManager.getLogger();

	public static final String DEFAULT_LANGUAGE = "en";

	private static final Preferences PREFS = GuiController.getPrefs();
	private static Locale locale;
	private static Font font;

	static {
		final String localeStr = Optional.ofNullable(PREFS.get("locale", null)).orElseGet(()->getLanguage());
		setLocale(localeStr);
	}

	private static Properties translationProperties;
	private static Map<String, String> map;
	private static ResourceBundle messages;

	public static void setLocale(final String localeStr){
		font = null;

		new ThreadWorker(
				()->{

					try{

						locale = new Locale(localeStr);

						messages = ResourceBundle.getBundle("irt.controller.translation.messageBundle", locale);
						map = getMap();

						if(!PREFS.get("locale", DEFAULT_LANGUAGE).equals(localeStr))
							PREFS.put("locale", localeStr);

						getFont(localeStr);

					}catch (Exception e) {
						logger.catching(e);
					}
				}, "Translation.setLocale()");
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

	public static String getValue(String key, String defaultValue){
		return getValueWithSuplier(String.class, key, ()->defaultValue);
	}

	public static <T> T getValue(Class<T> clazz, String key, T defaultValue){
		return getValueWithSuplier(clazz, key, ()->defaultValue);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getValueWithSuplier(Class<T> clazz, String key, Supplier<T> defaultValue){
		T returnValue = null;

		int times = 0;
		while(map==null)
			synchronized (logger) {
				try {
					Thread.sleep(10);
					if(times>10)
						return defaultValue.get();
				} catch (Exception e) {
					logger.catching(e);
					return null;
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
				logger.warn("Have to do implementation for '{}'", clazz);
			}
		}else{
			try{
				returnValue = defaultValue.get();
			}catch (NullPointerException e) {
				logger.error("Con not find value for key={}, Used Default={}", key, defaultValue);
				returnValue = null;
			}
		}

		return returnValue;
	}

	private static Font getFont(String selectedLanguage) {
		try {
			String fontURL = getValue(String.class, "font_path", "fonts/TAHOMA.TTF");

			int fontStyle = Font.BOLD;
			float fontSize = getValue(Float.class, "headPanel.font_size", 18f);

			if (fontURL != null && (font = getSystemFont(fontURL, fontStyle, (int) fontSize)) == null) {
				logger.warn("The Operating System does not have {} font.", fontURL);
				URL resource = IrtGui.class.getResource(fontURL);
				try (InputStream openStream = resource.openStream();) {
					font = Font.createFont(Font.TRUETYPE_FONT, openStream);
					font = font.deriveFont(fontStyle).deriveFont(fontSize);
				}
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
				font = new Font(s, fontStyle, fontSize);
				break;
			}

		return font;
	}

	public static Font getFont() {
		while(font==null)
			synchronized (logger) {
				try {
					logger.info("Wait for Font");
					Thread.sleep(400);
				} catch (InterruptedException e) {
					logger.catching(e);
				}
			}
		return font;
	}

	public static String getLanguage() {
		return locale!=null ?  locale.getLanguage() : Locale.getDefault().getLanguage();
	}

	public static void setFont(Font font) {
		Translation.font = font;
	}

	public static Locale getLocale() {
		return locale;
	}

	private static Properties getTranslationProperties() {
		if(translationProperties==null){
			translationProperties = new Properties();
			try(InputStream resourceAsStream = Translation.class.getResourceAsStream("translation.properties");) {
				
				translationProperties.load(resourceAsStream);
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
