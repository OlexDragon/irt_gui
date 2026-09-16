package irt.gui.web;

import java.awt.Desktop;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

@SpringBootApplication
public class Gui4 {
	private final static Logger logger = LogManager.getLogger();
	private static String urlStr;
	private static Object port;

	public static void main(String[] args) {
//		logger.info("Start GUI}");

		System.setProperty("java.awt.headless", "false");

		String name;
		try {
			name = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			logger.catching(e);
			name = "localhost";
		}

		Resource resource = new ClassPathResource("/application.properties");
		Properties props;

		try {
			props = PropertiesLoaderUtils.loadProperties(resource);
			port = props.get("server.port");
		} catch (IOException e) {
			logger.catching(e);
			port = "8085";
		}

		urlStr = "http://" + name + ":" + port;
		stopPreviousInstance(urlStr);

		SpringApplication.run(Gui4.class, args);
	}

	private static void stopPreviousInstance(String urlStr) {

		try {
			URL url = new URL(urlStr + "/exit");

			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			con.setConnectTimeout(50);
			con.setReadTimeout(50);
			con.setRequestMethod("GET");

			con.getResponseCode();
			con.disconnect();

		} catch (Exception e) {
			logger.trace("No previous instance found.");
		}
	}

	@EventListener(ApplicationReadyEvent.class)
	public void applicationReady() {
		try {
			final URI uri = new URI(urlStr);
			Desktop.getDesktop().browse(uri);
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
