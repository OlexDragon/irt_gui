package irt.tools.fx;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.Timer;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.javafx.tk.TKStage;

import irt.data.DeviceInfo;
import irt.data.network.HttpUploader;
import irt.data.network.NetworkAddress;
import irt.data.profile.Profile;
import irt.data.profile.ProfileValidator.ProfileErrors;
import irt.tools.fx.UpdateMessageFx.PacketFormats;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.text.Font;

public class UpdateButtonJFXPanel extends JFXPanel {
	private static final long serialVersionUID = -5186685111758208307L;

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

	// ******************************* class UpdateButtonFx   ***************************************************
	public class UpdateButtonFx extends Button{

		private UpdateMessageFx updateMessage;
		private boolean timerDone;

		// ******************************* constructor UpdateButtonFx   ***************************************************
		public UpdateButtonFx() {
			setText("Update");

			final Font font = new Font(12);
			setFont(font);

			addMouseListener(new MouseListener() {
				Timer timer = new Timer((int) TimeUnit.SECONDS.toMillis(10), a->timerDone=true);
				
				@Override
				public void mouseReleased(MouseEvent e) {
					timer.stop();
				}
				
				@Override
				public void mousePressed(MouseEvent e) {
					timer.restart();
				}
				
				@Override public void mouseExited(MouseEvent e) { }
				@Override public void mouseEntered(MouseEvent e) { }
				@Override public void mouseClicked(MouseEvent e) { }
			});

			setOnAction(e->{

				// Return if message already showing
				Optional<TKStage> oStage = Optional.ofNullable(updateMessage)

						.map(UpdateMessageFx::getDialogPane)
						.map(DialogPane::getScene)
						.map(Scene::getWindow)
						.map(TKStage.class::cast);

				if(oStage.isPresent()) {
					oStage.get().setVisible(true);
					return;
				}

				final byte[] address = networkAddress.getAddress();
				final String addrStr = Optional.of(
											Optional.ofNullable(address)
											.filter(a->a.length==4)
											.map(a->IntStream.range(0, a.length))
											.orElse(IntStream.empty())
											.map(index->address[index]&0xFF)
											.mapToObj(Integer::toString)
											.collect(Collectors.joining(".")))
								.filter(a->!a.isEmpty())
								.orElseGet(()->deviceInfo.getSerialNumber().orElse(""));

				updateMessage = new UpdateMessageFx(deviceInfo, timerDone);

				timerDone = false;	//Reset the timer setting

				updateMessage.setIpAddress(addrStr);
				updateMessage.showAndWait()
				.ifPresent(message->{

					// Test IP Address
					final HttpUploader uploader = new HttpUploader(message.getAddress());

					// If IP address is not reachable show error message
					if(!uploader.isIpReachable()){

						String ipAddress = uploader.getIpAddress();
						logger.warn("Can not reach the IP addredd {}", ipAddress);
						showAlert("The IP Address '" + ipAddress + "' is not Reachable.");
						return;
					}

					//***********************************************   Upload package   *******************************************************

					if(message.isPackage()){

						Optional
					    .ofNullable(message.getPacksgePath())
					    .map(File::new)
					    .ifPresent(file->{
							try {

								uploader.upload(file);

							} catch (IOException e2) {
								logger.catching(e2);
							}
						});

						return;

					}

					//*************************************************   Create package   *****************************************************

					try(	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
							TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(byteArrayOutputStream);){
								
						StringBuffer setupMD5 = new StringBuffer();
						MessageDigest md5 = MessageDigest.getInstance("MD5");

						//SETUP.INFO file
						{

							final String setupInfo = message.getSetupInfo();
							logger.debug(setupInfo);

							final byte[] setupInfoBytes = setupInfo.getBytes(Profile.charEncoding);

							addToTar(tarArchiveOutputStream, "setup.info", setupInfo.getBytes(Profile.charEncoding));

							setupMD5.append(DatatypeConverter.printHexBinary(md5.digest(setupInfoBytes))).append(" *setup.info").append("\n") ;
						}

						addProfileToTheTar(message.getProfile(), setupMD5, tarArchiveOutputStream);

						// PROGRAM
						message
						.getByteBuffer(PacketFormats.IMAGE)
						.map(bb->{
							byte[] dst = new byte[bb.capacity()];
							bb.get(dst);
							return dst;
						})
						.ifPresent(bytes->{
							message
							.getPath(PacketFormats.IMAGE)
							.map(Path::getFileName)
							.map(Path::toString)
							.ifPresent(fileName->{

								try {

									addToTar(tarArchiveOutputStream, fileName, bytes);
									setupMD5.append(DatatypeConverter.printHexBinary(md5.digest(bytes))).append(" *" + fileName).append("\n");

								} catch (IOException e1) {
									logger.catching(e1);
								}
							});
						});

						addToTar(tarArchiveOutputStream, "setup.md5", setupMD5.toString().getBytes());

						ByteArrayInputStream is = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
						uploader.upload(is);

					} catch (NoSuchAlgorithmException | IOException e1) {
						logger.catching(e1);
					}
				});
			});
		}

		private void addProfileToTheTar(Optional<Profile> oProfile, StringBuffer setuoMD5, TarArchiveOutputStream tarArchiveOutputStream) {

			final ProfileErrors profileErrors = oProfile.map(Profile::validate).orElse(ProfileErrors.DO_NOT_EXSISTS);
			if(profileErrors==ProfileErrors.DO_NOT_EXSISTS)
				return;

			// If profile has error return
			if(profileErrors!=ProfileErrors.NO_ERROR){

				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Profile error: " + profileErrors);
				alert.setHeaderText("The profile contains errors.\nYou must decide to stop or continue the upload.");
				alert.setContentText("Press 'Continue' or 'Stop' buton.");

				ButtonType btContinue = new ButtonType("Continue", ButtonData.OK_DONE);
				ButtonType btStop = new ButtonType("Stop", ButtonData.CANCEL_CLOSE);
				final ObservableList<ButtonType> buttonTypes = alert.getButtonTypes();
				buttonTypes.addAll(btContinue, btStop);
				buttonTypes.remove(ButtonType.OK);

				final Optional<ButtonType> oButtonType = alert.showAndWait();
				if(oButtonType.get()==btStop)
					return;
			}

			// if profile does not have errors prepare a profile for uploading
			final String profileMD5 = oProfile
											.map(p -> {
												try {

													return p.getMD5();

												} catch (Exception e1) {
													logger.catching(e1);
													return null;
												}
											}).orElse(null);

			if(profileMD5==null){
				showAlert("MD5 errorr.");
				return;
			}

			// setup.md5 file content
			final String fileName = oProfile.map(Profile::getFileName).orElse("");
			setuoMD5.append(profileMD5).append(" *").append(fileName);

			// Profile tar entry
			oProfile
			.map(Profile::getProfileCharBuffer)
			.ifPresent(charBuffer->{
				try {

					charBuffer.rewind();
					addToTar(tarArchiveOutputStream, fileName, charBuffer.toString().getBytes());

				} catch (Exception e1) {
					logger.catching(e1);
				}
			});
		}

		private void addToTar(TarArchiveOutputStream tarArchiveOutputStream, String fileName, byte[] content) throws IOException {

			TarArchiveEntry infoEntry = new TarArchiveEntry(fileName);
			infoEntry.setSize(content.length);

			tarArchiveOutputStream.putArchiveEntry(infoEntry);
			tarArchiveOutputStream.write(content);
			tarArchiveOutputStream.closeArchiveEntry();
		}

		private void showAlert(final String errorMessage) {

			final Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText(errorMessage);

			Platform.runLater(()->{
				alert.show();
			});
		}
	}

	public void fire() {
		Platform.runLater(()->root.fire());
	}
}
