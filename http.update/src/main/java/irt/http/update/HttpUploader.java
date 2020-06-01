package irt.http.update;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUploader implements Closeable{

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
		connection.setRequestProperty("User-Agent", "IRT HTTP Uploader");

		connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

	}

	public void upload(File file) throws IOException {
		
		try(	InputStream inputStream = new FileInputStream(file);
				OutputStream outputStream = connection.getOutputStream();
				DataOutputStream dataOutputStream = new DataOutputStream(outputStream);) {

			dataOutputStream.writeBytes(TWO_HYPHENS + boundary + LINE_END);
			dataOutputStream.writeBytes("Upgrade" + LINE_END);
			dataOutputStream.writeBytes(LINE_END);


			do {

				int bytesAvailable = inputStream.available();
				int bufferSize = Math.min(bytesAvailable, MAX_BUFFER_SIZE);
				byte[] buffer = new byte[bufferSize];

				int bytesRead = inputStream.read(buffer, 0, bufferSize);
				if(bytesRead <= 0)
					break;

				dataOutputStream.write(buffer, 0, bufferSize);

			}while(true);
					
			dataOutputStream.writeBytes(LINE_END);
			dataOutputStream.flush();
		}
	}

	@Override
	public void close() throws IOException {
		connection.disconnect();
	}
}
