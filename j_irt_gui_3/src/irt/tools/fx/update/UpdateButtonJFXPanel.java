package irt.tools.fx.update;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.CharBuffer;
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
import irt.data.ThreadWorker;
import irt.data.network.HttpUploader;
import irt.data.network.NetworkAddress;
import irt.tools.fx.MonitorPanelFx;
import irt.tools.fx.update.UpdateMessageFx.PacketFormats;
import irt.tools.fx.update.profile.EditTablesMessageFx.Action;
import irt.tools.fx.update.profile.Profile;
import irt.tools.fx.update.profile.ProfileValidator;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.text.Font;
import javafx.util.Pair;

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
			scene.getStylesheets().add(MonitorPanelFx.class.getResource("fx.css").toExternalForm());
			setScene(scene);
		});
	}

	// ******************************* class UpdateButtonFx   ***************************************************
	public class UpdateButtonFx extends Button{

		private UpdateMessageFx updateMessage;
		private boolean showProductionMessage;

		// ******************************* constructor UpdateButtonFx   ***************************************************
		public UpdateButtonFx() {
			setText("Update");

			final Font font = new Font(12);
			setFont(font);

			addMouseListener(new MouseListener() {
				Timer timer = new Timer(

						(int) TimeUnit.SECONDS.toMillis(10),
						a->{
							showProductionMessage=true;
							getStyleClass().add("RED_BORDER");
						});
				
				@Override
				public void mouseReleased(MouseEvent e) {
					timer.stop();
					getStyleClass().remove("RED_BORDER");
				}
				
				@Override
				public void mousePressed(MouseEvent e) {

					showProductionMessage = false;	//Reset the timer setting
					timer.restart();
				}
				
				@Override public void mouseExited(MouseEvent e) { }
				@Override public void mouseEntered(MouseEvent e) { }
				@Override public void mouseClicked(MouseEvent e) { }
			});

			setOnAction(event->{

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

				updateMessage = new UpdateMessageFx(deviceInfo, showProductionMessage);

				updateMessage.setIpAddress(addrStr);
				updateMessage.showAndWait()
				.ifPresent(message->{

					ThreadWorker.runThread(
							()->{

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

								try {

									byte[] bytes;

									try(	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
											TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(byteArrayOutputStream);){
											
										StringBuffer setupMD5 = new StringBuffer();
										MessageDigest md5 = MessageDigest.getInstance("MD5");

										{	//SETUP.INFO file

											final Pair<String, String> setupInfo = message.getSetupInfo();
											logger.debug(setupInfo);

											addToTar(tarArchiveOutputStream, "setup.info", setupInfo.getKey().getBytes(Profile.charEncoding));

											setupMD5.append(setupInfo.getValue()).append(" *setup.info").append("\n") ;
										}

										// PROFILE
										if(addProfileToTheTar(message.getProfile(), setupMD5, tarArchiveOutputStream)==Action.CANCEL)
											return;

										// PROGRAM
										message
										.getByteBuffer(PacketFormats.IMAGE)
										.map(bb->{
											byte[] dst = new byte[bb.capacity()];
											bb.get(dst);
											return dst;
										})
										.ifPresent(b->{
											message
											.getPath(PacketFormats.IMAGE)
											.map(Path::getFileName)
											.map(Path::toString)
											.ifPresent(fileName->{

												try {

													addToTar(tarArchiveOutputStream, fileName, b);
													setupMD5.append(DatatypeConverter.printHexBinary(md5.digest(b))).append(" *" + fileName).append("\n");

												} catch (IOException e) {
													logger.catching(e);
												}
											});
										});

										bytes = byteArrayOutputStream.toByteArray();

										addToTar(tarArchiveOutputStream, "setup.md5", setupMD5.toString().getBytes());

									}

									uploader.upload(bytes);	//TODO: uncomment to do update (commented For test only)

								} catch (NoSuchAlgorithmException | IOException e) {
									logger.catching(e);
								}

							}, "UpdateMessageFx action");
				});
			});
		}

		private Action addProfileToTheTar(Optional<Profile> oProfile, StringBuffer setuoMD5, TarArchiveOutputStream tarArchiveOutputStream) throws IOException, NoSuchAlgorithmException {

			final Action actiom = oProfile.map(

					profile -> {
						try {

							return new ProfileValidator(profile);

						} catch (NoSuchAlgorithmException | IOException e1) {
							logger.catching(e1);
							return null;
						}
					})
					.map(ProfileValidator::getAction)
					.orElse(Action.CANCEL);

			if(actiom==Action.CANCEL)
				return Action.CANCEL;

			// if profile does not have errors prepare a profile for uploading
			if(oProfile.isPresent()) {
				Profile profile = oProfile.get();

					final Pair<String, CharBuffer> pair = profile.asCharBufferWithMD5();

					// setup.md5 file content
					final String profileMD5 = pair.getKey();
					final String fileName = profile.getFileName();
					setuoMD5.append(profileMD5).append(" *").append(fileName);

					// Profile tar entry
					CharBuffer charBuffer = pair.getValue();
					addToTar(tarArchiveOutputStream, fileName, charBuffer.toString().getBytes(Profile.charEncoding));

					return Action.CONTINUE;
			}

			return Action.CANCEL;
		}

		private void addToTar(TarArchiveOutputStream tarArchiveOutputStream, String fileName, byte[] content) throws IOException {

			TarArchiveEntry infoEntry = new TarArchiveEntry(fileName);
			infoEntry.setSize(content.length);

			tarArchiveOutputStream.putArchiveEntry(infoEntry);
			tarArchiveOutputStream.write(content);
			tarArchiveOutputStream.closeArchiveEntry();
		}

		private void showAlert(final String errorMessage) {
			logger.error(errorMessage);

			Platform.runLater(
					()->{
						final Alert alert = new Alert(AlertType.ERROR);
						alert.setContentText(errorMessage);
						alert.show();
					});
		}
	}

	public void fire() {
		Platform.runLater(()->root.fire());
	}
}