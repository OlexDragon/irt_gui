package irt.serial.port.controllers;

import java.util.Observable;
import java.util.Optional;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.IrtGuiProperties;
import irt.serial.port.enums.SerialPortStatus;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
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

public class ComboBoxSerialPort extends Observable {
	private final Logger logger = LogManager.getLogger();

	private static final String NETWORK_SELECTION = "Network";
	private static final Preferences prefs = Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);
	private String prefsName;

	public final PacketsQueue queue =  new PacketsQueue();

	private PacketSender	serialPort;				public PacketSender 	getSerialPort() { return serialPort; }

	private SerialPortStatus portStatus = SerialPortStatus.NOT_SELECTED;
	public SerialPortStatus getSerialPortStatus() { return portStatus; }
	public void setSerialPortStatus(SerialPortStatus portStatus) {
		logger.entry(portStatus);
		this.portStatus = portStatus;
		notifyObservers(portStatus);
	}

	@FXML private ComboBox<String> comboBoxSerialPort;

	public void initialize(String prefsName) {
		logger.entry(prefsName);

		try{
			addItems();

			this.prefsName = prefsName;

			String serialPortName = prefs.get(prefsName, null);
			final int indexOf = comboBoxSerialPort.getItems().indexOf(serialPortName);
			if(serialPortName!=null && indexOf>=0){
				comboBoxSerialPort.getSelectionModel().select(indexOf);
				onActionSelectSerialPort();
			}
		}catch(Exception ex){
			logger.catching(ex);
		}
		comboBoxSerialPort.sceneProperty().addListener((obs, oldScene, newScene) -> {

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

	private void addItems() {
		ObservableList<String> items = comboBoxSerialPort.getItems();
		items.clear();
		items.addAll(SerialPortList.getPortNames());

		//Network 
		items.add(NETWORK_SELECTION);
	}

	@FXML private void onActionSelectSerialPort(){
		logger.traceEntry();
		try{

			closePort();

			String serialPortName = comboBoxSerialPort.getSelectionModel().getSelectedItem();
			if(serialPortName!=null){

				if(serialPortName.equals(NETWORK_SELECTION)){
					closePort();
					Optional<Pair<String, String>> result = showDialog();
					result.ifPresent(r->queue.setNetwork(r));
				}else{
					serialPort = new PacketSender(serialPortName);
					openPort();
				}

				prefs.put(prefsName, serialPortName);
				notifyObservers();
			}

			queue.setComPort(serialPort);
		}catch(Exception ex){
			catchError(ex);
		}
	}

    @FXML void onActionMenuRefresg() {

    	final SingleSelectionModel<String> selectionModel = comboBoxSerialPort.getSelectionModel();
		final String selectedItem = selectionModel.getSelectedItem();

		addItems();

		selectionModel.select(selectedItem);
    }

	synchronized public void openPort(){
		logger.traceEntry();

		Optional
		.ofNullable(serialPort)
		.ifPresent(serialPort->{
			
			try {

				if(serialPort.openPort()){
					setSerialPortStatus( SerialPortStatus.OPEND);
					logger.debug("Serial Port {} is opened", serialPort);

				}else
					logger.error("It is not posible to open {} port", serialPort);

				notifyObservers();

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
				}else
					logger.error("It is not posible to close {} port", serialPort);

			} catch (SerialPortException e) {
				catchError(e);
			}

			notifyObservers();
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

	@Override
	public void notifyObservers() {
		notifyObservers(serialPort);
	}

	@Override
	public void notifyObservers(Object arg) {
		setChanged();
		super.notifyObservers(arg);
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

	public PacketsQueue getQueue() {
		return queue;
	}
}
