package irt.serial.port.fx;

import java.io.IOException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.IrtGuiProperties;
import irt.serial.port.controllers.PacketSender;
import irt.serial.port.enums.SerialPortStatus;
import irt.services.GlobalPacketsQueues;
import irt.services.SceneHideListener;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.stage.WindowEvent;
import javafx.util.Pair;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class ComboBoxSerialPortFx extends ComboBox<String> {
	private final Logger logger = LogManager.getLogger();

	private static final String NETWORK_SELECTION = "Network";
	private static final Preferences prefs = Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);
	private String prefsName;


	private PacketSender	serialPort;				public PacketSender 	getSerialPort() { return serialPort; }

	private SerialPortStatus portStatus;
	public SerialPortStatus getSerialPortStatus() { return portStatus; }
	public void setSerialPortStatus(SerialPortStatus portStatus) {
		logger.entry(this.portStatus, portStatus);

		if(this.portStatus==portStatus)
			return;

		this.portStatus = portStatus;
		observable.notifyObservers(this);
	}
//
//	@FXML private ComboBox<String> comboBoxSerialPort;

	public ComboBoxSerialPortFx() {

    	final URL resource = getClass().getResource("/fxml/ComboBoxSerialPort.fxml");
		FXMLLoader fxmlLoader = new FXMLLoader(resource, IrtGuiProperties.BUNDLE);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

//        valueProperty().addListener(e->logger.error(e));
	}

	public void initialize(String prefsName) {
		logger.entry(prefsName);

		if(prefsName!=null && prefsName.equals(this.prefsName))
			return;

		setPrefsName(prefsName);

		try{

			setItems();

			String serialPortName = prefs.get(prefsName, null);
			final int indexOf = getItems().indexOf(serialPortName);
			if(serialPortName!=null && indexOf>=0){
				getSelectionModel().select(indexOf);
				onSelectSerialPort();
			}else
				setSerialPortStatus(SerialPortStatus.NOT_SELECTED);

		}catch(Exception ex){
			logger.catching(ex);
		}

		sceneProperty().addListener((obs, oldScene, newScene) -> {

			Optional
			.ofNullable(newScene)

			//combo box is added to the scene
			.ifPresent(scene->scene
					.windowProperty()

					//scene is added to the window
					.addListener((o, oldWind, newWind)->
													Optional
													.ofNullable(newWind).ifPresent(window->window

															//on window hiding close serial port
															.addEventHandler(WindowEvent.WINDOW_HIDING, e->
																										Optional
																										.ofNullable(serialPort)
																										.ifPresent(serialPort->{ try {

																											serialPort.closePort(); } catch (SerialPortException e1) { logger.catching(e1); }})))));});
	}

	public String getPrefsName() {
		return prefsName;
	}

	private void setPrefsName(String prefsName) {
		logger.entry(prefsName);
		this.prefsName = prefsName;
	}

	private void setItems() {
		ObservableList<String> items = getItems();
		items.clear();
		final String[] portNames = SerialPortList.getPortNames();
		logger.trace("{}", (Object[]) portNames);
		items.addAll(portNames);

		//add Network 
		items.add(NETWORK_SELECTION);
	}

	@FXML private void onSelectSerialPort(){
		String serialPortName = getSelectionModel().getSelectedItem();
		logger.traceEntry(serialPortName);

		closePort();

		if(serialPortName==null){
			serialPort = null;
			GlobalPacketsQueues.get(prefsName).setSerialPort(serialPort);
			return;
		}

		try{

			if(serialPortName.equals(NETWORK_SELECTION)){
				Optional<Pair<String, String>> result = showDialog();
				result.ifPresent(r->GlobalPacketsQueues.get(getPrefsName()).setNetwork(r));
				GlobalPacketsQueues.get(prefsName).setSerialPort(null);
			}else{
				PacketSender sp;
				serialPort = sp = new PacketSender(serialPortName);
				GlobalPacketsQueues.get(prefsName).setSerialPort(serialPort);
				openPort();
				// close serial port on application exit
				{
					// be shutdown hook
					Runtime.getRuntime().addShutdownHook(new Thread(() -> finalize(sp)));

					// or by window event listener
					Optional.ofNullable(getScene()).map(scene -> scene.getWindow()).map(w -> {
						w.addEventHandler(WindowEvent.WINDOW_HIDING, e -> finalize(sp));
						return w;
					}).orElseGet(() -> {
						sceneProperty().addListener(new SceneHideListener(e -> finalize(sp)));
						return null;
					});
				}
			}

			prefs.put(getPrefsName(), serialPortName);


		}catch(Exception ex){
			catchError(ex);
		}
	}

    void finalize(PacketSender serialPort){
    	logger.entry(serialPort);

    	Optional
		.ofNullable(serialPort)

		//Close serial port if present
		.ifPresent(sp->{
			try {

				if(sp.closePort())
					logger.info("Serial port {} is cosed.", sp);
				else
					logger.info("It is impossible to close the {}", sp);

			} catch (Throwable e1) {
				logger.catching(e1);
			}
		});
    }

    @FXML void onMenuRefresg() {
    	logger.traceEntry();

    	final SingleSelectionModel<String> selectionModel = getSelectionModel();
		final String selectedItem = selectionModel.getSelectedItem();

		setItems();

		selectionModel.select(selectedItem);
    }

    public void openPort(){
		logger.traceEntry();

		Optional
		.ofNullable(serialPort)
		.ifPresent(serialPort->{
			
			try {

				if(serialPort.openPort()){
					setSerialPortStatus( SerialPortStatus.OPEND);
					logger.debug("Serial Port {} is opened", serialPort);

				}else{
					setSerialPortStatus( SerialPortStatus.ERROR);
					logger.error("It is not posible to open {} port", serialPort);
				}

			} catch (SerialPortException e) {
				catchError(e);
			}
		});
	}

	public void closePort(){
		logger.traceEntry();

		Optional
		.ofNullable(serialPort)
		.ifPresent(serialPort->{
			
			try {

				if(serialPort.closePort()){
					setSerialPortStatus(SerialPortStatus.CLOSED);
					logger.info("Serial Port {} is closed", serialPort.getPortName());
				}else{
					setSerialPortStatus( SerialPortStatus.ERROR);
					logger.error("It is not posible to close {} port", serialPort);
				}

			} catch (SerialPortException e) {
				catchError(e);
			}
		});
	}

	private void catchError(Exception e) {

		if(e.getLocalizedMessage().contains(SerialPortStatus.BUSY.name().toLowerCase())){
			setSerialPortStatus(SerialPortStatus.BUSY);
		}else{
			setSerialPortStatus(SerialPortStatus.ERROR);
		}

		logger.catching(e);
	}

	@Override
	protected void finalize() throws Throwable {
		if(serialPort!=null)
			synchronized (serialPort) {
				serialPort.closePort();
			}
	}

	private final Observable observable = new Observable() {

		@Override
		public void notifyObservers(Object arg) {
			setChanged();
			super.notifyObservers(arg);
		}
	};
    public void addObserver(Observer o) {
    	observable.addObserver(o);
    }

	private Optional<Pair<String, String>> showDialog() {

		Dialog<Pair<String, String>> dialog = new Dialog<>();
		dialog.setTitle("Connect");
		dialog.setHeaderText("Type a host and a port.");
		ButtonType connectButtonType = new ButtonType("Connect", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(connectButtonType, ButtonType.CANCEL);

		// Create the hast and port labels and fields.
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));
		dialog.getDialogPane().setContent(grid);


		// Host
		TextField hostTextField = new TextField();
		hostTextField.setPromptText("Host");
		hostTextField.setTooltip(new Tooltip("Host"));

		// Port
		TextField portTextField = new TextField();
		portTextField.setPromptText("Port = 10000 + Serial Port");
		portTextField.setTooltip(new Tooltip("Port = 10000 + Serial Port"));

		grid.add(new Label("Host:"), 0, 0);
		grid.add(hostTextField, 1, 0);
		grid.add(new Label("Port:"), 0, 1);
		grid.add(portTextField, 1, 1);

		// Request focus on the username field by default.
		Platform.runLater(() -> hostTextField.requestFocus());

		// Convert the result to a host-port-pair when the login button is clicked.
		dialog.setResultConverter(dialogButton -> {
		    if (dialogButton == connectButtonType) {
		        return new Pair<>(hostTextField.getText(), portTextField.getText());
		    }
		    return null;
		});

		return dialog.showAndWait();
	}
}
