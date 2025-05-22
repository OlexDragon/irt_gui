package irt.gui.controllers.components;

import java.lang.reflect.Constructor;
import java.util.Observable;
import java.util.Optional;
import java.util.prefs.Preferences;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.IrtGuiApp;
import irt.gui.IrtGuiProperties;
import irt.gui.controllers.LinkedPacketsQueue;
import irt.gui.controllers.enums.SerialPortStatus;
import irt.gui.controllers.serial_port.IrtSerialPort;
import irt.gui.controllers.serial_port.PacketSenderJSerialComm;
import irt.gui.controllers.serial_port.PacketSenderJssc;
import irt.gui.controllers.serial_port.SerialPortParams;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import jssc.SerialPortList;
import lombok.Getter;

public class ComboBoxSerialPort extends Observable {
	private static final String SELECTED_DRIVER = "selected driver";

	private final Logger logger = LogManager.getLogger();

	private static final String NETWORK_SELECTION = "Network";
	private static final Preferences prefs = Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);

	public final static SerialPortParams SERIAL_PORT_PARAMS = new SerialPortParams();
	public final LinkedPacketsQueue queue =  new LinkedPacketsQueue();

	private SerialPortStatus portStatus; public SerialPortStatus getSerialPortStatus() { return portStatus; }

	@FXML private ComboBox<String> serialPortComboBox;
    @FXML private RadioMenuItem menuJssc;
    @FXML private RadioMenuItem menyJSerialComm;

	private static boolean initialized;

	private String prefsName;
	@Getter private IrtSerialPort	serialPort;
	private Class<?> selectedDriver = PacketSenderJssc.class;

    @FXML public void initialize() {

    	ToggleGroup group = new ToggleGroup();

    	menuJssc.setUserData(PacketSenderJssc.class);
    	menuJssc.setToggleGroup(group);

    	menyJSerialComm.setUserData(PacketSenderJSerialComm.class);
    	menyJSerialComm.setToggleGroup(group);

    	Stream.of(menuJssc, menyJSerialComm).filter(mi->mi.getId().equals(prefs.get(SELECTED_DRIVER, null))).findAny()
    	.ifPresent(
    			mi->{
    				mi.setSelected(true);
    				selectedDriver = (Class<?>) mi.getUserData();
    			});
    }

    public void initialize(String prefsName) {

		try{
			ObservableList<String> items = serialPortComboBox.getItems();
			items.clear();
			items.addAll(SerialPortList.getPortNames());

			//Network 
			items.add(NETWORK_SELECTION);

			this.prefsName = prefsName;

			String serialPortName = prefs.get(prefsName, null);
			boolean guiBeenClosedProperly = initialized || prefs.getBoolean(IrtGuiApp.GUI_IS_CLOSED_PROPERLY, true);
			initialized = true;

			if(!guiBeenClosedProperly) {

				ChoiceDialog<Class<?>> alert = new ChoiceDialog<>(selectedDriver, PacketSenderJSerialComm.class, PacketSenderJssc.class) ;
				alert.setTitle("The GUI was not closed properly.");
				alert.setHeaderText("Try to select a different serial port driver.");
//				Node comboBox = (Node) alert.getDialogPane().lookup(".combo-box");
				Optional<Class<?>> oClass = alert.showAndWait();

				if(!oClass.isPresent())
					return;

				selectedDriver = oClass.get();
				Stream.of(menuJssc, menyJSerialComm).filter(mi->mi.getUserData().equals(selectedDriver)).findAny().ifPresent(mi->mi.setSelected(true));
			}

			if( serialPortName!=null && !serialPortName.contains("Select") && serialPortComboBox.getItems().contains(serialPortName)){

				prefs.putBoolean(IrtGuiApp.GUI_IS_CLOSED_PROPERLY, false);

				final SingleSelectionModel<String> selectionModel = serialPortComboBox.getSelectionModel();
				selectionModel.select(serialPortName);
				Platform.runLater(()->onActionSelectSerialPort());
			}
		}catch(Exception ex){
			logger.catching(ex);
		}
	}

	@FXML private void onActionSelectSerialPort(){
		try{

			closePort();

			String serialPortName = serialPortComboBox.getSelectionModel().getSelectedItem();
			if(serialPortName!=null){

				if(serialPortName.equals(NETWORK_SELECTION)){

					closePort();
					Optional<Pair<String, String>> result = showDialog();
					result.ifPresent(r->queue.setNetwork(r));

				}else{

					Constructor<?> constructor = selectedDriver.getConstructor(String.class);
					serialPort = (IrtSerialPort) constructor.newInstance(serialPortName);
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

    	final SingleSelectionModel<String> selectionModel = serialPortComboBox.getSelectionModel();
		final String selectedItem = selectionModel.getSelectedItem();

    	ObservableList<String> items = serialPortComboBox.getItems();
		items.clear();
		items.addAll(SerialPortList.getPortNames());

		selectionModel.select(selectedItem);
    }

	synchronized public void openPort(){
		logger.traceEntry();

		
		if(serialPort!=null && !serialPort.isOpened()){

			try {

				if(serialPort.openSerialPort())
					logger.info("Serial Port {} is opened", serialPort);
				else
					logger.error("It is not posible to open {} port", serialPort);

				notifyObservers();

			} catch (Exception e) {
				catchError(e);
			}

		}
	}

    @FXML
    void onDriverSelect(ActionEvent event) {
    	RadioMenuItem source = (RadioMenuItem) event.getSource();
    	prefs.put(SELECTED_DRIVER, source.getId());
    	selectedDriver = (Class<?>)source.getUserData();
    }

	public void closePort(){

		if(serialPort!=null && serialPort.isOpened()){

			try {

				if(serialPort.closeSerialPort()){
					portStatus = SerialPortStatus.CLOSED;
					logger.info("Serial Port {} is closed", serialPort.getPortName());
				}else
					logger.error("It is not posible to close {} port", serialPort);

			} catch (Exception e) {
				catchError(e);
			}

			notifyObservers();
		}
	}

	private void catchError(Exception e) {

		if(e.getLocalizedMessage().contains(SerialPortStatus.BUSY.name().toLowerCase()))
			notifyObservers(portStatus = SerialPortStatus.BUSY);
		else
			notifyObservers(portStatus = SerialPortStatus.ERROR);

		logger.catching(e);
	}

	@Override
	protected void finalize() throws Throwable {
		if(serialPort!=null)
			synchronized (serialPort) {
				if(serialPort.isOpened())
					serialPort.closeSerialPort();
			}
	}

	@Override
	public void notifyObservers() {
		setChanged();
		super.notifyObservers();
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

	public LinkedPacketsQueue getQueue() {
		return queue;
	}
}
