package irt.gui.web;

import java.awt.Desktop;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Properties;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

@SpringBootApplication
public class Gui4 {
	private final static Logger logger = LogManager.getLogger();


	public static void main(String[] args) {
		logger.info("Start GUI}");

		System.setProperty("java.awt.headless", "false");
		String name = null;

		try {
			name = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			logger.catching(e);
			name = "localhost";
		}

		try {

			SpringApplication.run(Gui4.class, args);

		}catch(Exception e) {
			logger.catching(Level.TRACE, e);
		}

		try {

			Resource resource = new ClassPathResource("/application.properties");
			Properties props = PropertiesLoaderUtils.loadProperties(resource);
			final Object port = props.get("server.port");
			Desktop.getDesktop().browse(new URI("http://" + name + ":" + port));

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	public static void exit() {
		logger.info("Stop GUI");
		System.exit(0);
	}

//    @PreDestroy
//    public void destroy() {
//        System.out.println(
//          "Callback triggered - @PreDestroy.");
//    }
}
