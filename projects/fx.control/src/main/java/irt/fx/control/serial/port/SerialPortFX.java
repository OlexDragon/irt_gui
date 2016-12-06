package irt.fx.control.serial.port;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.IrtGuiProperties;
import irt.serial.port.controllers.PacketSender;
import irt.serial.port.controllers.PacketsQueue;
import irt.serial.port.enums.SerialPortStatus;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.WindowEvent;
import javafx.util.Pair;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class SerialPortFX extends HBox {
	private final Logger logger = LogManager.getLogger();

	private Observable observable = new Observable(){
														@Override public void notifyObservers() {
															notifyObservers(serialPort);
														}
														@Override public void notifyObservers(Object arg) {
															setChanged();
															super.notifyObservers(arg);
														}};

	private static final String NETWORK_SELECTION = "Network";
	private static final Preferences prefs = Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);
	private String prefsName;

	public final PacketsQueue queue =  new PacketsQueue();

	private PacketSender	serialPort;									public PacketSender 	getSerialPort() {
																				return serialPort;
																			}

	private SerialPortStatus portStatus = SerialPortStatus.NOT_SELECTED;	public SerialPortStatus getSerialPortStatus() {
																				return portStatus;
																			}
																			private void setSerialPortStatus(SerialPortStatus portStatus) {
																				logger.entry(portStatus);
																				this.portStatus = portStatus;
																				showSerialPortStatus();
																				observable.notifyObservers(portStatus);
																			}

    public SerialPortFX() {

    	FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/serial_port.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	}


    @FXML
	public void initialize() {
		
		addItems();
		showSerialPortStatus();
	}

    public void initialize(String prefsName) {
		initializeComboBox(prefsName);
		showSerialPortStatus();
	}

	private void initializeComboBox(String prefsName) {
		logger.entry(prefsName);
		try{

			this.prefsName = prefsName;

			String serialPortName = prefs.get(prefsName, null);
			final int indexOf = comboBoxSerialPort.getItems().indexOf(serialPortName);

			if(serialPortName!=null && indexOf>=0){
				comboBoxSerialPort.getSelectionModel().select(indexOf);
				onSelectSerialPort();
			}

		}catch(Exception ex){
			logger.catching(ex);
		}
	}

	@FXML
    private ComboBox<String> comboBoxSerialPort;

    @FXML
    private Button serialPortButton;

    @FXML
    void onSelectSerialPort() {
		logger.traceEntry();
		try{

			closePort();

			String serialPortName = comboBoxSerialPort.getSelectionModel().getSelectedItem();
			if(serialPortName!=null){

				if(serialPortName.equals(NETWORK_SELECTION)){

					Optional<Pair<String, String>> result = showDialog();
					result.ifPresent(r->queue.setNetwork(r));

				}else{

					final PacketSender sp = serialPort = new PacketSender(serialPortName);

					openPort();

					observable.notifyObservers();

					//close serial port on application exit
					{
						//be shutdown hook
						Runtime.getRuntime().addShutdownHook(new Thread(()->finalize(sp)));

						//or by window event listener
						Optional
						.ofNullable(comboBoxSerialPort.getScene())
						.map(scene->scene.getWindow())
						.map(w->{w.addEventHandler(WindowEvent.WINDOW_HIDING, e->finalize(sp)); return w;})
						.orElseGet(()->{comboBoxSerialPort.sceneProperty().addListener(new SceneHideListener(e->finalize(sp))); return null;});
					}

					Optional.ofNullable(prefsName).ifPresent(pn->prefs.put(prefsName, serialPortName));
					queue.setComPort(serialPort);
				}

			}

		}catch(Exception ex){
			catchExeption(ex);
		}
	}

    void finalize(PacketSender serialPort){
    	Optional
		.ofNullable(serialPort)

		//Close serial port if present
		.ifPresent(sp->{
			try {

				synchronized (sp) {
					if(sp.closePort())
						logger.info("Serial port {} is cosed.", sp);
					else
						logger.info("It is impossible to close the {}", sp);
				}

			} catch (Throwable e1) {
				logger.catching(e1);
			}
		});
    }

    @FXML
    void onSerialPortButton() {
    	Optional
    	.ofNullable(serialPort)
    	.ifPresent(sp->{
    		
    		if(serialPort.isOpened())
    			closePort();
    		else
    			openPort();
    	});
	} 

    @FXML void onMenuRefresg() {

    	final SingleSelectionModel<String> selectionModel = comboBoxSerialPort.getSelectionModel();
		final String selectedItem = selectionModel.getSelectedItem();

		addItems();

		selectionModel.select(selectedItem);
    }

	@Override
	protected void finalize() throws Throwable {
		if(serialPort!=null)
			synchronized (serialPort) {
				if(serialPort.closePort())
					logger.info("Serial port {} is cosed.", serialPort);
				else
					logger.info("It is impossible to close the {}", serialPort);
			}
	}

	//Combo box
	private void addItems() {
		ObservableList<String> items = comboBoxSerialPort.getItems();
		items.clear();
		items.addAll(SerialPortList.getPortNames());

		//Network 
		items.add(NETWORK_SELECTION);
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

				}else{
					setSerialPortStatus( SerialPortStatus.ERROR);
					logger.error("It is not posible to open {} port", serialPort);
				}

			} catch (SerialPortException e) {
				catchExeption(e);
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
				catchExeption(e);
			}
		});
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

	private void catchExeption(Exception e) {
		final String localizedMessage = e.getLocalizedMessage();
		logger.error(e);
		logger.error(localizedMessage);

		if(localizedMessage!=null && localizedMessage.contains("busy")){

			setSerialPortStatus(SerialPortStatus.BUSY);
			logger.info(e);

		}else{

			setSerialPortStatus(SerialPortStatus.ERROR);
			logger.catching(e);
		}
	}

	//Button
	private void addStyleClass(String toAdd, String... toRemove) {
		logger.trace("add '{}' class; remove classes: {}", toAdd, toRemove);

		Platform.runLater(()->{
			final ObservableList<String> styleClass = serialPortButton.getStyleClass();
			if(!styleClass.contains(toAdd)){
				styleClass.removeAll(toRemove);
				styleClass.add(toAdd);
			}
		});
	}

	private void setText() {
		logger.entry(portStatus);

		final String text = "Serial port is " + portStatus;
		Platform.runLater(()->serialPortButton.setText(text));
	}

	private void showSerialPortStatus() {

		if(serialPort==null){

			addStyleClass("warning", "error", "connected");

		}else if(serialPort.isOpened()){

			addStyleClass("connected", "error", "warning");

		}else if(portStatus==SerialPortStatus.CLOSED ){

			addStyleClass("warning", "connected", "error");

		}else

			addStyleClass("error", "connected", "warning");

		setText();
	}

	public void addObserver(Observer observer) {
		observable.addObserver(observer);
	}
}
