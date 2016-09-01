package irt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IrtGuiProperties {
	private static final Logger logger = LogManager.getLogger();

	public static final String PREFS_NAME = "IRT Technologies inc.";

	public static final String 	USER_HOME 			= System.getProperty("user.home");
	public static final File 	IRT_HOME			= new File(USER_HOME, "irt") ;
	public static final File 	DESKTOP				= new File(USER_HOME, "Desktop") ;

	public static final String 	PANEL_PROPERTIES 	= "gui.panel.%s.%s";

	private static final Properties properties = new Properties();
	private static final Set<String> defaultPaths = new LinkedHashSet<>(Arrays.asList("", IRT_HOME.getAbsolutePath(), DESKTOP.getAbsolutePath()));

	static{
		try {

			reload();

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	public static String getPropertiesFileName() {
		return properties.getProperty("gui.properties.file.nane");
	}

	private static void getPropertiesFromFiles() {
		String fileName = getPropertiesFileName();

		defaultPaths
		.stream()
		.map(path->path.isEmpty() ? new File(fileName) : new File(path, fileName))
		.filter(f->f.exists() && !f.isDirectory())
		.forEach(f->loadProperties(f));
	}

	private static void loadProperties(File file) { try { properties.load(new FileInputStream(file)); } catch (IOException e) { logger.catching(e); } }

	public static Boolean getBoolean(String propertyName){
		return Optional
				.ofNullable(properties.getProperty(propertyName))
				.filter(v->!v.isEmpty())
				.map(Boolean::parseBoolean)
				.orElse(false);
	}

	public static Long getLong(String propertyName, final Long defaultValue){
		return Optional
				.ofNullable(properties.getProperty(propertyName))
				.filter(v->!v.isEmpty())
				.map(Long::parseLong)
				.orElse(defaultValue);
	}

	public static String getProperty(String propertyName){
		return properties.getProperty(propertyName);
	}

	public static Properties selectFromProperties(String propertyStartsWith) {
		return selectFromProperties(properties, propertyStartsWith);
	}

	public static Properties selectFromProperties(Properties properties, String propertyStartsWith) {

		final Properties result = new Properties();
		result.putAll(
				properties
				.entrySet()
				.parallelStream()
				.filter(set->((String)set.getKey()).startsWith(propertyStartsWith))
				.collect(Collectors.toMap(e->(String)e.getKey(), e->(String)e.getValue())));

		return result;
	}

	public static void updateProperties(Properties propertiesFromFile) {
		propertiesFromFile
		.entrySet()
		.stream()
		.forEach(e->properties.setProperty((String)e.getKey(), (String)e.getValue()));
	}

	public static void reload() throws IOException {
		final InputStream resource = IrtGuiProperties.class.getResourceAsStream("/linearizer.properties");
		properties.load(resource);

		Optional
		.ofNullable(properties.getProperty("gui.properties.file.path"))
		.ifPresent((p)->defaultPaths.addAll(Arrays.asList(p.split(","))));

		getPropertiesFromFiles();
	}
}
