package irt.http.update;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.logging.log4j.LogManager;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

public class HttpUploader implements Closeable{

	private final String PREFIX = "var httpd_message=";
	private static final String TWO_HYPHENS = "--";
	private static final String LINE_END = "\r\n";
	private static final int MAX_BUFFER_SIZE = 1*1024*1024;

	private final HttpURLConnection connection;
	private final String boundary;

	public HttpUploader(String ipAddress) throws IOException {

		boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
		URL url = new URL("http", ipAddress, "/upgrade.cgi");

		connection = (HttpURLConnection) url.openConnection();	

		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setUseCaches(false);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Connection", "Keep-Alive");

		final String value = "IRT HTML Updater; User: " + System.getProperty("user.name") + "; os.name: " + System.getProperty("os.name") + "; os.arch: " + System.getProperty("os.arch") + ";";
		connection.setRequestProperty("User-Agent", value);
		connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
	}

	public void upload(File file) throws FileNotFoundException, IOException{

			LogManager.getLogger().error(file);

			sendFileToUnit(file);
			Message response = readResponse();
			showAlert(response);
	}

	private void showAlert(final Message message) {
		Platform.runLater(
				()->{
					final Alert alert = new Alert(message.getType());
					alert.setContentText(message.getMessage());
					alert.show();
				});
	}

	private void sendFileToUnit(File file) throws IOException, FileNotFoundException {

		try(	InputStream inputStream = new FileInputStream(file);
				OutputStream outputStream = connection.getOutputStream();
				DataOutputStream dataOutputStream = new DataOutputStream(outputStream);) {

			dataOutputStream.writeBytes(TWO_HYPHENS);
			dataOutputStream.writeBytes(boundary);
			dataOutputStream.writeBytes(LINE_END);

			dataOutputStream.writeBytes("Upgrade");
			dataOutputStream.writeBytes(LINE_END);
			dataOutputStream.writeBytes(LINE_END);

			do {

				int bytesAvailable = inputStream.available();
				if(bytesAvailable == 0)
					break;

				int bufferSize = Math.min(bytesAvailable, MAX_BUFFER_SIZE);
				byte[] buffer = new byte[bufferSize];

				inputStream.read(buffer, 0, bufferSize);

				dataOutputStream.write(buffer, 0, bufferSize);

			}while(true);
					
			dataOutputStream.writeBytes(LINE_END);
			dataOutputStream.flush();
		}
	}

	private AlertType alertType;

	private Message readResponse() throws IOException {

		alertType = AlertType.ERROR;
		Message message = null;
		try(	final InputStream in = connection.getInputStream();
				final InputStreamReader streamReader = new InputStreamReader(in);
				final BufferedReader bufferedReader = new BufferedReader(streamReader);) {

//			LogManager.getLogger().error(in.);
			
			StringBuffer buf = new StringBuffer();
			String line;

			final String lineSeparator = System.getProperty("line.separator");

			// When the unit accepts the update it return page with the title 'End of session'
			while ((line = bufferedReader.readLine())!=null) {

				buf.append(line).append(lineSeparator);
				if(line.contains("End of session"))
					alertType = AlertType.INFORMATION;

				message = getMessage(line);
				if(message!=null)
					// Break if message exists
					break;
			}
			LogManager.getLogger().error(buf);
		}

		return message;
	}

	private Message getMessage(String line) {

		if(line.startsWith(PREFIX)) {
			return new Message(alertType, line.substring(PREFIX.length(), line.length()-1));
		}

		return null;
	}

	@Override
	public void close() throws IOException {
		connection.disconnect();
	}

	// ********************** class Message ***************************
	@AllArgsConstructor @Getter @ToString
	public static class Message {
		private final AlertType type;
		private final String message;
	}
}
