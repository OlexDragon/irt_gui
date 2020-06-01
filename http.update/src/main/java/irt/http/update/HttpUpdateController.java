package irt.http.update;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.prefs.Preferences;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.beans.value.ObservableBooleanValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

public class HttpUpdateController{
	private final static Logger logger = LogManager.getLogger();

	private final static String UNIT_ADDRESS = "HttpUpdate.UnitAddress";
	private final static Preferences prefs = Preferences.userNodeForPackage(HttpUpdateApp.class);

	private final DeviceInfoControllerListener listener = new DeviceInfoControllerListener();
	private final Set<DeviceInfoController> controllers = new HashSet<>();

	@FXML private TextField tfHttpAddress;
    @FXML private Button btnUpdate;
    @FXML private VBox vBox;

    @FXML void onGetInfo() {

    	URL httpAddress = null;
		try {

			vBox.getChildren().clear();
    		httpAddress = new URL("http", tfHttpAddress.getText(), "/debug.asp");

    		URLConnection openConnection = httpAddress.openConnection();
    		try(BufferedReader in = new BufferedReader(new InputStreamReader(openConnection.getInputStream()));){

    			List<String> deviceID = new ArrayList<>();
    			List<String> serialNumber = new ArrayList<>();
    			List<String> description = new ArrayList<>();
    			List<String> partNumber = new ArrayList<>();

    			String inputLine;
				while ((inputLine = in.readLine()) != null) {
					Optional.of(inputLine).filter(line->line.contains("Product name:")).ifPresent(line->description.add(line.trim().substring("Product name:".length()).trim()));
					Optional.of(inputLine).filter(line->line.contains("Serial number:")).ifPresent(line->serialNumber.add(line.trim().substring("Serial number:".length()).trim()));
					Optional.of(inputLine).filter(line->line.contains("Part number:")).ifPresent(line->partNumber.add(line.trim().substring("Part number:".length()).trim()));
					Optional.of(inputLine).filter(line->line.contains("Device ID:")).ifPresent(line->deviceID.add(line.trim().substring("Device ID:".length()).replace("{", "").replace("}", "").trim()));
				}

				IntStream.range(0, deviceID.size()).forEach(
						index->{Platform.runLater(
								()->{

									DeviceInfoController deviceInfoController = new DeviceInfoController(deviceID.get(index), serialNumber.get(index), description.get(index), partNumber.get(index));
							    	vBox.getChildren().add(deviceInfoController);
							    	listener.addController(deviceInfoController);
								});
						});
    		}

    	} catch (UnknownHostException e) {
    		final Alert alert = new Alert(AlertType.ERROR);
    		alert.setTitle("HTTP Connection Error");
    		alert.setHeaderText(null);
    		alert.setContentText("The IP Address '" + httpAddress + "' is not Reachable.");
    		
    		alert.showAndWait();
			logger.catching(e);

    	} catch (IOException e) {
			logger.catching(e);
		}
    }

    @FXML void onUpdate() {

    	try {

    		controllers.parallelStream().filter(DeviceInfoController::isSelected).findAny()
    		.map(
    				t -> {
    					try {
    						return t.getPackage();
    					} catch (IOException e) {
    						logger.catching(e);
    					}
    					return null;
    				});

    		PackageBuilder.createPackage();

    	} catch (NoSuchAlgorithmException | IOException e) {
			logger.catching(e);
		}
    	File file = null;//TODO
    	try(final HttpUploader httpUploader = new HttpUploader(tfHttpAddress.getText());){

    		httpUploader.upload(file);

    	} catch (IOException e) {
			logger.catching(e);
		}
    }

    @FXML void onAddressRefresh(KeyEvent e) {

		if(e.getCode()==KeyCode.ESCAPE)
			Optional.ofNullable(prefs.get(UNIT_ADDRESS, null)).ifPresent(tfHttpAddress::setText);
    }

    @FXML public void initialize() {

    	initializeTextField();
    }

	private void initializeTextField() {
		Optional.ofNullable(prefs.get(UNIT_ADDRESS, null)).ifPresent(tfHttpAddress::setText);
		tfHttpAddress.focusedProperty().addListener(
				e->{
					Optional.of(((ObservableBooleanValue)e).getValue())
					.filter(v->v==false)
					.ifPresent(v->Optional.of(tfHttpAddress.getText().trim()).filter(t->!t.isEmpty()).ifPresent(t->prefs.put(UNIT_ADDRESS, t)));
				});
	}

	// ******************** class DeviceInfoControllerListener ***************************

	public class DeviceInfoControllerListener implements Consumer<DeviceInfoController>{

		public void addController(DeviceInfoController deviceInfoController) {
			controllers.add(deviceInfoController);
			deviceInfoController.setListener(this);
		}

		@Override
		public void accept(DeviceInfoController deviceInfoController) {

			btnUpdate.setDisable(!deviceInfoController.isSelected());
			controllers.parallelStream().filter(controller->controller!=deviceInfoController).forEach(DeviceInfoController::deselect);
		}
	}
}
