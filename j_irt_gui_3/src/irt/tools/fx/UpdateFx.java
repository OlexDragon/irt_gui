package irt.tools.fx;

import static irt.tools.fx.update.UpdateButtonJFXPanel.showAlert;
import static irt.tools.fx.update.UpdateButtonJFXPanel.addToTar;
import static irt.tools.fx.update.UpdateButtonJFXPanel.toPackage;
import static irt.tools.fx.update.UpdateMessageFx.getFileChooser;

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiControllerAbstract;
import irt.data.DeviceInfo;
import irt.data.ThreadWorker;
import irt.data.listener.PacketListener;
import irt.data.network.HttpUploader;
import irt.data.packet.interfaces.Packet;
import irt.tools.fx.update.data.SetupInfo;
import irt.tools.fx.update.profile.Profile;
import irt.tools.panel.head.IrtPanel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.util.Pair;

public class UpdateFx extends BorderPane implements PacketListener{
	private final static Logger logger = LogManager.getLogger();

	private static String serialNumber;

    @FXML private Button btnUpdate;
    @FXML private Button btnOpen;
    @FXML private Button btnToPackage;
    @FXML private Button btnLocation;
    @FXML private Button btnSelect;
	@FXML private TextField tfIpAddress;
    @FXML private Label lblSerial;

    public UpdateFx() {

		try {

			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Update.fxml"));
			fxmlLoader.setRoot(this);
			fxmlLoader.setController(this);

			fxmlLoader.load();
 
		} catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @FXML protected void initialize() {
    	sceneProperty().addListener(
    			(os,o,n)->  // scene is set for the first time.
    			Optional.ofNullable(n).filter(s->o==null).ifPresent(s->s.windowProperty().addListener(
    					(ov,ow,nw)->// stage is set.
    					Optional.ofNullable(nw).filter(w->ow==null)
    					.ifPresent(
    							w->{;
    						    	w.setOnShowing(
    						    			e->{
    						    				GuiControllerAbstract.getComPortThreadQueue().addPacketListener(this);
    						    				setIp();
    						    			});
       						    	w.setOnHiding(e->GuiControllerAbstract.getComPortThreadQueue().removePacketListener(this));
    							}))));
	}
	@FXML
    void onGo() {

		Optional.of(tfIpAddress.getText()).filter(t->!t.isEmpty()).map(String::toUpperCase)
		.ifPresent(
				sn->{
					serialNumber = sn;
					tfIpAddress.setText(sn);
					scan();
				});
    }

    @FXML
    void onOpen() {
		final File file = new File(lblSerial.getTooltip().getText());
    	try {
			Desktop.getDesktop().edit(file);
		} catch (IOException e) {
			logger.catching(e);
		}
    }

    @FXML
    void onLocation() {
    	final String path = lblSerial.getTooltip().getText();
		try {
			Runtime.getRuntime().exec("explorer.exe /select," + path);
		} catch (IOException e1) {
			logger.catching(e1);
		}
    }

    @FXML
    void onSelect() {
    	FileChooser fileChooser = getFileChooser(lblSerial, "*.bin");
    	Optional.ofNullable(fileChooser.showOpenDialog(getScene().getWindow()))
    	.ifPresent(
    			f->{
    				lblSerial.setText(f.getName());
    				lblSerial.setTooltip(new Tooltip(f.getAbsolutePath()));
					btnOpen.setDisable(false);
					btnLocation.setDisable(false);

					if(!tfIpAddress.getText().isEmpty()) {
						btnUpdate.setDisable(false);
						btnToPackage.setDisable(false);
					}
    			});
    }

    @FXML
    void onToPackage() {

		byte[] bytes = preparePackage();

		if(bytes==null)
			return;

		try {

			toPackage(serialNumber + ".pkg", bytes);

		} catch (IOException e) {
			logger.catching(e);
		}
    }

    @FXML
    void onUpdate() {

		// Test IP Address
		final HttpUploader uploader = new HttpUploader(tfIpAddress.getText());

		// If IP address is not reachable show error message
		if(!uploader.isIpReachable()){

			String ipAddress = uploader.getIpAddress();
			logger.warn("Can not reach the IP addredd {}", ipAddress);
			showAlert("The IP Address '" + ipAddress + "' is not Reachable.");
			return;
		}

		byte[] bytes = preparePackage();

		if(bytes==null)
			return;

		try {

			uploader.upload(bytes);

		} catch (IOException e) {
			logger.catching(e);
		}
   }

	private byte[] preparePackage() {

		try(	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(byteArrayOutputStream);){

			//SETUP.INFO file
			final Path path = Paths.get(lblSerial.getTooltip().getText());
			final Profile profile = new Profile(path);

			final SetupInfo setupInfo = new SetupInfo(serialNumber, profile.getFileName());
			logger.debug(setupInfo);

			StringBuffer setupMD5 = new StringBuffer();
			setupMD5.append(setupInfo.getMd5()).append(" *setup.info").append("\n") ;
			addToTar(tarArchiveOutputStream, "setup.info", setupInfo.toBytes());


			final Pair<String, CharBuffer> pair = profile.asCharBufferWithMD5();

			// setup.md5 file content
			final String profileMD5 = pair.getKey();
			final String fileName = profile.getFileName();
			setupMD5.append(profileMD5).append(" *").append(fileName);

			// Profile tar entry
			CharBuffer charBuffer = pair.getValue();
			addToTar(tarArchiveOutputStream, fileName, charBuffer.toString().getBytes(Profile.charEncoding));

			// setup.md5 tar entry
			addToTar(tarArchiveOutputStream, "setup.md5", setupMD5.toString().getBytes());

			return byteArrayOutputStream.toByteArray();


		} catch (IOException | NoSuchAlgorithmException e) {
			logger.catching(e);
		}

		return null;
	}

	@Override
	public void onPacketReceived(Packet packet) {
		ThreadWorker.runThread(()->DeviceInfo.parsePacket(packet).ifPresent(di->setIp()), "UpdateFx Packet Listener");
	}

	private void setIp() {
		Platform.runLater(
				()->{
					if(serialNumber!=null && tfIpAddress.getText().isEmpty()) {
						tfIpAddress.setText(serialNumber);
						scan();
					}
				});
	}

	private void scan() {

		btnOpen.setDisable(true);
		btnLocation.setDisable(true);
		btnToPackage.setDisable(true);
		btnUpdate.setDisable(true);
		try {

			scan(serialNumber, ifFound());

		} catch (IOException e) {
			logger.catching(e);
		}

	}

	public static void scan(String serialNumber, Consumer<? super Path> ifFound) throws IOException {

		final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:*" + serialNumber + ".bin");
		final AtomicReference<Path> arPath = new AtomicReference<>();
		final FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

				if(attrs.isRegularFile()) {

					Path name = file.getFileName();
					if (matcher.matches(name)) {
						arPath.set(file);
						return FileVisitResult.TERMINATE;
					}
				}
				return FileVisitResult.CONTINUE;
			}
		};

		final Path path =  Paths.get(IrtPanel.PROPERTIES.getProperty("path_to_profiles"));
		Files.walkFileTree(path, visitor);
		Optional.ofNullable(arPath.get()).ifPresent(ifFound);
	}

	private Consumer<? super Path> ifFound() {
		return p->
		Platform.runLater(
				()->{
					lblSerial.setText(p.getFileName().toString());
					lblSerial.setTooltip(new Tooltip(p.toString()));
					btnOpen.setDisable(false);
					btnUpdate.setDisable(false);
					btnToPackage.setDisable(false);
					btnLocation.setDisable(false);
				});
	}

	public static void setSerialNumber(String serialNumber) {
		UpdateFx.serialNumber = serialNumber;
	}

}
