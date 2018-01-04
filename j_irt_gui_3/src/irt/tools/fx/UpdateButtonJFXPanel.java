
package irt.tools.fx;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.DeviceInfo;
import irt.data.network.NetworkAddress;
import irt.irt_gui.IrtGui;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;

public class UpdateButtonJFXPanel extends JFXPanel {
	private static final long serialVersionUID = -5186685111758208307L;
	private static final String twoHyphens = "--";
	private static final String lineEnd = "\r\n";
	private static final int maxBufferSize = 1*1024*1024;

	private final Logger logger = LogManager.getLogger();

	private final DeviceInfo deviceInfo;
	private final NetworkAddress networkAddress;

	private final UpdateButtonFx root;
	
	public UpdateButtonJFXPanel(DeviceInfo deviceInfo, NetworkAddress networkAddress) {

		this.deviceInfo = deviceInfo;
		this.networkAddress = networkAddress;
		root = new UpdateButtonFx();

		Platform.runLater(()->{
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("fx.css").toExternalForm());
			setScene(scene);
		});
	}

	// ******************************* constructor UpdateButtonFx   ***************************************************
	public class UpdateButtonFx extends Button{

		public UpdateButtonFx() {
			setText("Update");
			setOnAction(e->{

				final byte[] address = networkAddress.getAddress();
				final String addrStr = Optional.ofNullable(address)
												.filter(a->a.length==4)
												.map(a->IntStream.range(0, a.length))
												.orElse(IntStream.empty())
												.map(index->address[index]&0xFF)
												.mapToObj(Integer::toString)
												.collect(Collectors.joining("."));

				UpdateMessagePkgFx d = new UpdateMessagePkgFx(deviceInfo);
				d.setIpAddress(addrStr);
				
				d.showAndWait()
				.ifPresent(message->{

					//IP Address
					final String ipAddress = message.getAddress();
					if(!isIpReachable(ipAddress)){
						logger.warn("Can not reach the IP addredd {}", ipAddress);
						showAlert("The IP Address '" + ipAddress + "' is not Reachable.");
						return;
					}


					String boundary =  "*****"+Long.toHexString(System.currentTimeMillis())+"*****";

					try {

						URL url = new URL("http://" + ipAddress + "/upgrade.cgi");
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

						    Optional
						    .ofNullable(message.getPacksgePath())
						    .map(File::new)
						    .ifPresent(file->{
						    	
								// Upload POST Data
								try(FileInputStream fileInputStream = new FileInputStream(file);) {

									int bytesAvailable = fileInputStream.available();
									int bufferSize = Math.min(bytesAvailable, maxBufferSize);
									byte[] buffer = new byte[bufferSize];

									int bytesRead = fileInputStream.read(buffer, 0, bufferSize);
									while(bytesRead > 0) {
										dataOutputStream.write(buffer, 0, bufferSize);
										bytesAvailable = fileInputStream.available();
										bufferSize = Math.min(bytesAvailable, maxBufferSize);
										bytesRead = fileInputStream.read(buffer, 0, bufferSize);
									}
									
									dataOutputStream.writeBytes(lineEnd);
									dataOutputStream.flush();

								} catch (IOException e1) {
									logger.catching(e1);
								}

								// Read Response
								try(InputStream in = connection.getInputStream()) {

									BufferedReader r = new BufferedReader(new InputStreamReader(in));
									StringBuffer buf = new StringBuffer();
									String line;
									while ((line = r.readLine())!=null) {
										buf.append(line).append(System.getProperty("line.separator"));
										final int indexOf = buf.indexOf("End of session");
										if(indexOf>0)
											return;
									}

									String m = "var httpd_message='";
									final String errorMessage = Optional
																	.of(buf.indexOf(m))
																	.filter(index->index>0)
																	.map(index->index+m.length())
																	.map(index->buf.substring(index))
																	.map(str->str.substring(0, str.indexOf("'")))
																	.orElse("Ooops, there was an error!");
																		
									Alert alert = new Alert(AlertType.ERROR);
									alert.setTitle("Upload Error");
									alert.setHeaderText("Error Message:");
									alert.setContentText(errorMessage);

									alert.showAndWait();

								} catch (IOException e1) { logger.catching(e1); }
						    });
						}


					} catch (IOException e1) {
						logger.catching(e1);
					}

//					final ErrorMessage errorMessage = Optional.ofNullable(packagePath).orElse(null);

//					try {
//						final MessageDigest md5 = MessageDigest.getInstance("MD5");
//
//						if(errorMessage!=null)
//							if(errorMessage.getError()!=ProfileErrors.NO_ERROR){
//
//								showAlert("The profile '" + packagePath + "' has errors.");
//								return;
//
//							} else {
//								Optional
//								.of(Paths.get(packagePath).toFile())
//								.filter(File::exists)
//								.filter(File::isFile)
//								.ifPresent(f->{
//
//									String charEncoding = System.getProperty("file.encoding");
//
//									try(	RandomAccessFile 	raf = new RandomAccessFile(f, "r");
//											FileChannel 		fileChannel 	= raf.getChannel()){
//
//										MappedByteBuffer mbb = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
//										CharBuffer charBuffer = Charset.forName(charEncoding).decode(mbb);
//
//
//										final CharBuffer allocate = CharBuffer.allocate(charBuffer.length() + 10);
//
//										int r = allocate.read(charBuffer);
//										allocate.put("1234567890");
//										logger.error(allocate.position(0).toString());
//										logger.error(charBuffer.position(0).toString());
//										logger.error("sizes: {}:{}", charBuffer.length(), allocate.length());
//
//										logger.error(DatatypeConverter.printHexBinary(md5.digest(charBuffer.toString().getBytes(charEncoding))));
//										logger.error(DatatypeConverter.printHexBinary(md5.digest(allocate.toString().getBytes(charEncoding))));
//
//									} catch (Exception e1) {
//										logger.catching(e1);
//									}
//								});
//
//								try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(Paths.get(packagePath), EnumSet.of(StandardOpenOption.READ))) {
//
//									Optional
//									.ofNullable(fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size()))
//									.ifPresent(mbb->{
//
//										logger.error("position={}", mbb.position());
//										String charEncoding = System.getProperty("file.encoding");
//										CharBuffer charBuffer = Charset.forName(charEncoding).decode(mbb);
//
//										logger.error("position={}", charBuffer.position());
//										logger.error(charBuffer.toString());
//										logger.error("position={}", charBuffer.position());
//
//										try {
//
//											byte[] hash = md5.digest(charBuffer.toString().getBytes(charEncoding));
//											logger.error(DatatypeConverter.printHexBinary(hash));
//
//										} catch (UnsupportedEncodingException e1) {
//											logger.catching(e1);
//										}
//									});
//
//								}
//
//									byte[] b = Files.readAllBytes(Paths.get(packagePath));
//									byte[] hash = md5.digest(b);
//									logger.error(DatatypeConverter.printHexBinary(hash));
//							}
//
//						//If programPath != null
////						Optional.ofNullable(message.getProgramPath()).ifPresent(pp->{
////							//TODO Add action
////							logger.error("programPath - {}", pp);
////						});
//					} catch (Exception e1) {
//						logger.catching(e1);
//					}

				});
			});
		}

		private void showAlert(final String errorMessage) {

			final Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText(errorMessage);

			Platform.runLater(()->{
				alert.showAndWait();
			});
		}
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
//
//	//***************** class ErrorMessage ********************
//	public class ErrorMessage{
//
//		private ProfileErrors error;
//
//		public ErrorMessage(ProfileErrors error) {
//			this.error = error;
//		}
//
//		public ProfileErrors getError() {
//			return error;
//		}
//
//		@Override
//		public String toString() {
//			return "ErrorMessage [error=" + error + "]";
//		}
//	}
}
