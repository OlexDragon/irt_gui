
package irt.irt_gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import irt.controller.DumpControllerFull;

public class Test {

	private static final LoggerContext ctx = DumpControllerFull.setSysSerialNumber(null);//need for log file name setting

	private final static Logger logger = LogManager.getLogger();

	public static void main(String[] args) {
		logger.trace(ctx);

		String ipAddress = "op-2123100";
		String urlParams = "exec=calib_ro_info";

		URL url;
		HttpURLConnection connection = null;
		try {

			url = new URL("http", ipAddress, "/update.cgi");
			connection = (HttpURLConnection) url.openConnection();	
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Connection", "keep-alive");

			try(	OutputStream outputStream = connection.getOutputStream();
					OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);){

				outputStreamWriter.write(urlParams);
				outputStreamWriter.flush();

				String line;
				try(	final InputStream inputStream = connection.getInputStream();
						final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
						final BufferedReader reader = new BufferedReader(inputStreamReader);){

while ((line = reader.readLine()) != null) {
    System.out.println(line);
}
					
				}
			}
		} catch (IOException e) {
			logger.catching(e);
		}

		Optional.ofNullable(connection).ifPresent(HttpURLConnection::disconnect);
	}
}
