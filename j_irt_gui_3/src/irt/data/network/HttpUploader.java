
package irt.data.network;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;

import irt.irt_gui.IrtGui;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class HttpUploader {

	private static final String twoHyphens = "--";
	private static final String lineEnd = "\r\n";
	private static final int maxBufferSize = 1*1024*1024;

	private final String ipAddress;

	public HttpUploader(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public boolean isIpReachable() {
		return isIpReachable(ipAddress);
	}


	private static boolean isIpReachable(String targetIp) {
		 
        try {
            InetAddress target = InetAddress.getByName(targetIp);
            return target.isReachable(5000);  //timeout 5sec

        } catch (Exception ex) {
            LogManager.getLogger().catching(ex);
            return false;
        }
    }

	public String getIpAddress() {
		return ipAddress;
	}

	public void upload(File file) throws FileNotFoundException, IOException {

		try(InputStream inputStream = new FileInputStream(file);) {
			upload(inputStream);
		}
	}

	public void upload(InputStream inputStream) throws IOException {

		String boundary =  "*****"+Long.toHexString(System.currentTimeMillis())+"*****";

		URL url = new URL("http", ipAddress, "/upgrade.cgi");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();	

		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setUseCaches(false);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Connection", "Keep-Alive");
		final String value = "IRT GUI" + IrtGui.VERTION + "; User: " + System.getProperty("user.name") + "; os.name: " + System.getProperty("os.name") + "; os.arch: " + System.getProperty("os.arch") + ";";
		connection.setRequestProperty("User-Agent", value);
		connection.setRequestProperty("Content-Type", "multipart/form-data; boundary="+boundary);

		try(	OutputStream outputStream = connection.getOutputStream();
				DataOutputStream dataOutputStream = new DataOutputStream(outputStream);) {

			dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
			dataOutputStream.writeBytes("Upgrade" + lineEnd);
			dataOutputStream.writeBytes(lineEnd);

			int bytesAvailable = inputStream.available();
			int bufferSize = Math.min(bytesAvailable, maxBufferSize);
			byte[] buffer = new byte[bufferSize];

			int bytesRead = inputStream.read(buffer, 0, bufferSize);
			while(bytesRead > 0) {
				dataOutputStream.write(buffer, 0, bufferSize);
				bytesAvailable = inputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = inputStream.read(buffer, 0, bufferSize);
			}
					
			dataOutputStream.writeBytes(lineEnd);
			dataOutputStream.flush();

			// Read Response
			try(InputStream in = connection.getInputStream()) {

				BufferedReader r = new BufferedReader(new InputStreamReader(in));
				StringBuffer buf = new StringBuffer();
				String line;

				AlertType t = null;;
				String em = null;
				// When the unit accepts the update it return page with the title 'End of session'
				while ((line = r.readLine())!=null) {
					buf.append(line).append(System.getProperty("line.separator"));
					final int indexOf = buf.indexOf("End of session");
					if(indexOf>0){
						t = AlertType.INFORMATION;
						em = "Firmware upgrade is in progress. It may take up to 30 seconds to complete operation";
						break;
					}
				}

				if(t==null){
					
					String m = "var httpd_message='";
					em = Optional
														.of(buf.indexOf(m))
														.filter(index->index>0)
														.map(index->index+m.length())
														.map(index->buf.substring(index))
														.map(str->str.substring(0, str.indexOf("'")))
														.orElse("Ooops, there was an error!");
					t = AlertType.ERROR;
				}

				final AlertType type = t;
				final String errorMessage = em;
				Platform.runLater(()->{
					
					Alert alert = new Alert(type);
					alert.setTitle(type==AlertType.ERROR ? "Upload Error" : "Package Upload");
					alert.setHeaderText(type==AlertType.ERROR ? "Error Message:" : "Message:");
					alert.setContentText(errorMessage);

					TimerTask task = new TimerTask() {
						
						@Override
						public void run() {
							Platform.runLater(()->alert.close());
						}
					};
					new Timer().schedule(task, 10*1000);
					alert.show();
				});
			}
		}
		connection.disconnect();
	}
}
