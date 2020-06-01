package irt.http.update;

import static irt.http.update.HttpUpdateApp.FIRMWARE_FILE_PATH_START_WITH;
import static irt.http.update.HttpUpdateApp.PROFILE_SEARCH_FILE_START_WITH;
import static irt.http.update.HttpUpdateApp.PROPERTIES;
import static irt.http.update.HttpUpdateApp.UNIT_TYPE_START_WITH;

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.prefs.Preferences;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.http.update.HttpUpdateController.DeviceInfoControllerListener;
import irt.http.update.unit_package.PackageFile;
import irt.http.update.unit_package.PackageFile.FileType;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class DeviceInfoController extends VBox{
	private final static Logger logger = LogManager.getLogger();

	private final static Preferences prefs = Preferences.userNodeForPackage(HttpUpdateApp.class);

	private final String UNIT_TYPE_KEY;

	private String deviceID;
	private String serialNumber;
	private String description;
	private String partNumber;

	private Button btnPropertieSet;

	private String unitTtype;
	private String profilePath;
	private String firmwarePath;

	private ChangeListener<? super Boolean> listener = new CheckboxListener();
	private Optional<DeviceInfoControllerListener> oControllerListener = Optional.empty();

	public DeviceInfoController(String deviceID, String serialNumber, String description, String partNumber) {

		UNIT_TYPE_KEY = UNIT_TYPE_START_WITH + deviceID;

		this.deviceID = deviceID;
		this.serialNumber = serialNumber;
		this.description = description;
		this.partNumber = partNumber;

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/DeviceInfo.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {

			fxmlLoader.load();

		} catch (IOException e) {
			logger.catching(e);
		}
	}

    @FXML  private HBox titleHBox;

    @FXML private CheckBox cbProfile;
    @FXML private CheckBox cbFirmware;
	@FXML private Label lblDeviceID;
    @FXML private Label lblSerialNumber;
    @FXML private Label lblDescription;
    @FXML private Label ibiPartNumber;

    @FXML private Label lblProfile;
    @FXML private Label lblFirmware;

    @FXML private MenuItem miOpenProfile;
    @FXML private MenuItem miProfileLocation;
    @FXML private MenuItem miFirnwareLocation;

    @FXML private Button btnSelectProfile;
    @FXML private Button btnSelectFirmware;

    @FXML public void initialize() {

		checkProperties();

		cbProfile.selectedProperty().addListener(listener);
		cbFirmware.selectedProperty().addListener(listener);

		lblDeviceID.setText(deviceID);
		lblSerialNumber.setText(serialNumber);
		lblDescription.setText(description);
		ibiPartNumber.setText(partNumber);
    }

    @FXML void onOpenProfile() {

    	try {

    		Desktop.getDesktop().open(new File(lblProfile.getUserData().toString()));

    	} catch (IOException e1) {
			logger.catching(e1);
		}
    }

    @FXML void onProfileLocation() {

    	try {

    		Runtime.getRuntime().exec("explorer.exe /select," + lblProfile.getUserData());

    	} catch (IOException e1) {
			logger.catching(e1);
		}
    }

    @FXML void onFirnwareLocation() {

    	try {

    		Runtime.getRuntime().exec("explorer.exe /select," + lblFirmware.getUserData());

    	} catch (IOException e1) {
			logger.catching(e1);
		}
    }

    @FXML void onProfileSelect() {

    	onSelect("Profile", lblProfile, cbProfile, miOpenProfile, miProfileLocation);
    }

    @FXML void onFirmwareSelect() {

    	onSelect("Firmware", lblFirmware, cbFirmware, miFirnwareLocation);
    }

    public void deselect() {
    	Platform.runLater(
    			()->{
    				 
    				cbProfile.selectedProperty().removeListener(listener);
    				cbProfile.setSelected(false);
    				cbProfile.selectedProperty().addListener(listener);
    				 
    				cbFirmware.selectedProperty().removeListener(listener);
    				cbFirmware.setSelected(false);
    				cbFirmware.selectedProperty().addListener(listener);
    			});
    }
	private void onSelect(String name, Label label, CheckBox checkBox, MenuItem... menuItems) {

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select " + name + " File.");

		final String key = name + "." + unitTtype;
		Optional.ofNullable(prefs.get(key, null))
		.ifPresent(
				profilePath->{
					final File file = new File(profilePath);
					fileChooser.setInitialDirectory(file.getParentFile());
					fileChooser.setInitialFileName(file.getName());
				});

		Optional.ofNullable(fileChooser.showOpenDialog(getScene().getWindow()))
		.ifPresent(
				file->{
					setLabel(label, file.toPath(), menuItems);

					prefs.put(key, file.getAbsolutePath());

					checkBox.selectedProperty().removeListener(listener);
					checkBox.setSelected(true);
					checkBox.selectedProperty().addListener(listener);
				});
	}

	private void checkProperties() {

		if(checkUnitType()) return;
		if(checkProfileSearchFolder()) return;
		if(checkFirmwarePath()) return;
	}

	private void removeSetupButton() {

		Optional.ofNullable(btnPropertieSet).ifPresent(button->Platform.runLater(()->titleHBox.getChildren().remove(button)));
		btnPropertieSet = null;
	}

	/**
	 * @return true if the settings button has been added
	 */
	private boolean checkUnitType() {

		removeSetupButton();

		unitTtype = PROPERTIES.getProperty(UNIT_TYPE_KEY);
		Optional<String> oType = Optional.ofNullable(unitTtype);

		Optional.of(oType.isPresent()).filter(b->b==false)
		.ifPresent(
				b->Platform.runLater(
						()->{
							btnPropertieSet = new ButtonSetUnitType(deviceID, ()->checkProperties());
							titleHBox.getChildren().add(btnPropertieSet);
						}));

		return !oType.isPresent();
	}

	/**
	 * @return true if the settings button has been added
	 */
	private boolean checkProfileSearchFolder() {

		removeSetupButton();

		final String searcgPathKey = PROFILE_SEARCH_FILE_START_WITH + unitTtype;
		profilePath = PROPERTIES.getProperty(searcgPathKey);
		Optional<String> oPath = Optional.ofNullable(profilePath);

		Optional.of(oPath.isPresent()).filter(b->b==false)
		.ifPresent(
				b->Platform.runLater(
						()->{
							btnPropertieSet = new ButtonSetProfileSearchFolder(unitTtype, ()->checkProperties());
							titleHBox.getChildren().add(btnPropertieSet);
						}));

		return !oPath.isPresent();
	}

	private void setDefault(Label label) {
		Platform.runLater(
				()->{

					if(label.getUserData()==null) {

						label.setText(label==lblProfile ? "Profile" : "Firmware");
						Optional.ofNullable(btnSelectProfile).ifPresent(Button::fire);
					}
				});
	}

	/**
	 * @return true if the settings button has been added
	 */
	private boolean checkFirmwarePath() {

		removeSetupButton();

		final String firmwarePathKey = FIRMWARE_FILE_PATH_START_WITH + unitTtype;
		firmwarePath = PROPERTIES.getProperty(firmwarePathKey);
		Optional<String> oPath = Optional.ofNullable(firmwarePath);

		Optional.of(oPath.isPresent()).filter(b->b==false)
		.ifPresent(
				b->Platform.runLater(
						()->{
							btnPropertieSet = new ButtonSetFirmwarePath(unitTtype, ()->checkProperties());
							titleHBox.getChildren().add(btnPropertieSet);
						}));

		return !oPath.isPresent();
	}

	private void setLabel(Label label, Path path, MenuItem... menuItem) {

		final Optional<Path> oPath = Optional.ofNullable(path);

		Platform.runLater(
				()->{
					label.setText(oPath.map(Path::getFileName).map(Object::toString).orElse("Search in progress ..."));
					label.setUserData(path);
					label.setTooltip(oPath.map(Object::toString).map(str->new Tooltip(str)).orElse(null));
				});

		Arrays.stream(menuItem).forEach(mi->Platform.runLater(()->mi.setDisable(path==null)));
	}

	private void searchForFirmware() {
		setLabel(lblFirmware, null, miFirnwareLocation);
		
		final String key = FIRMWARE_FILE_PATH_START_WITH + unitTtype;
		final String firmwarePath = PROPERTIES.getProperty(key);

		Optional.ofNullable(firmwarePath)
		.ifPresent(
				p->{
					final File file = new File(p);
					if(file.exists())
						setLabel(lblFirmware, file.toPath(), miFirnwareLocation);
					else {
						setDefault(lblFirmware);
						cbFirmware.setSelected(false);
					}
				});
	}

	public void setListener(DeviceInfoControllerListener listener) {
		oControllerListener = Optional.ofNullable(listener);
	}

	public boolean isSelected() {
		return cbProfile.isSelected() || cbFirmware.isSelected();
	}

	public File getPackage() throws IOException {

		try(	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(byteArrayOutputStream);){
			
//			PackageFile packageFile = null;
			if(cbProfile.isSelected())
				extracted(FileType.PROFILE, (Path) lblProfile.getUserData(), tarArchiveOutputStream);

		}
		return null;
	}

	private void extracted(final FileType type, final Path p, TarArchiveOutputStream tarArchiveOutputStream)
			throws IOException {
		PackageFile packageFile;
		packageFile = new PackageFile(type, p.toFile());
		TarArchiveEntry infoEntry = new TarArchiveEntry(packageFile.getFileName());
		byte[] bytes = packageFile.toBytes();
		infoEntry.setSize(bytes.length);

		tarArchiveOutputStream.putArchiveEntry(infoEntry);
		tarArchiveOutputStream.write(bytes);
		tarArchiveOutputStream.closeArchiveEntry();
	}
	// ******************** class CheckboxListener ***************************

	public class CheckboxListener implements ChangeListener<Boolean>{

		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

			Optional.of(newValue).filter(v->v==true)
			.ifPresent(
					v->{

						final CheckBox checkBox = (CheckBox)((BooleanProperty)observable).getBean();

						// Set the properties if missing
						if(btnPropertieSet!=null) {

							btnPropertieSet.fire();

							observable.removeListener(this);
							checkBox.setSelected(false);
							observable.addListener(this);
							return;
						}

						// Search for profile or firmware
						if(checkBox==cbProfile)
							searchForProfile();
						else
							searchForFirmware();
					});

			oControllerListener.ifPresent(l->l.accept(DeviceInfoController.this));
		}

		private void searchForProfile() {

			setLabel(lblProfile, null, miOpenProfile, miProfileLocation);
			ThreadBuilder.startThread(
					()->{
						final String key = PROFILE_SEARCH_FILE_START_WITH + unitTtype;
						final String profileFolder = PROPERTIES.getProperty(key);

						try {

							final Optional<Path> findAny = Files.find(Paths.get(profileFolder), 5, (filePath, fileAttr)->fileAttr.isRegularFile()).filter(p->p.getFileName().toString().equals(serialNumber + ".bin")).findAny();

							if(findAny.isPresent()) {
								if(lblProfile.getUserData()==null) 
									setLabel(lblProfile, findAny.get(), miOpenProfile, miProfileLocation);
							}else {
								// if the profile is not found
								setDefault(lblProfile);
								cbProfile.setSelected(false);
							}

						} catch (IOException e) {
							logger.catching(e);
						}
					});
		}
	}
}
