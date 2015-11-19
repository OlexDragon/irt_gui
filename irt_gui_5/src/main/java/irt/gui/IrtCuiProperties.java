package irt.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IrtCuiProperties {

	public static final String PANEL_PROPERTIES = "gui.panel.%s.%s";

	private static final Logger logger = LogManager.getLogger();
	private static final Properties properties = new Properties();
	private static final Set<String> defaultPath = new LinkedHashSet<>(Arrays.asList("", System.getProperty("user.home")+"/irt", System.getProperty("user.home")+"/Desktop"));

	static{
		final InputStream resource = IrtCuiProperties.class.getResourceAsStream("/gui5.properties");
		try {

			properties.load(resource);

			Optional.ofNullable(properties.getProperty("gui.properties.file.path"))
				.ifPresent((p)->defaultPath.addAll(Arrays.asList(p.split(","))));

			getPropertiesFromFiles();

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	private static void getPropertiesFromFiles() throws FileNotFoundException, IOException {
		String fileName = properties.getProperty("gui.properties.file.nane");

		for(String path:defaultPath){
			File propertiesFile = new File(path, fileName);
			if(propertiesFile.exists() && !propertiesFile.isDirectory())
				properties.load(new FileInputStream(propertiesFile));
		}
	}

	public static Boolean getBoolean(String propertyName){
		return Optional.ofNullable(properties.getProperty(propertyName)).filter(v->!v.isEmpty()).map(Boolean::parseBoolean).orElse(false);
	}

	public static Long getLong(String propertyName){
		return Optional.ofNullable(properties.getProperty(propertyName)).filter(v->!v.isEmpty()).map(Long::parseLong).orElse(null);
	}

	public static String getProperty(String propertyName){
		return properties.getProperty(propertyName);
	}
}
