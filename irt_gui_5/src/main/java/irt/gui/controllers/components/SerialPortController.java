package irt.gui.controllers.components;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.IrtGuiProperties;
import irt.gui.controllers.LinkedPacketSender;
import irt.gui.controllers.LinkedPacketsQueue;
import irt.gui.controllers.enums.Baudrate;
import irt.gui.controllers.enums.SerialPortStatus;
import irt.gui.controllers.socket.SocketWorker;
import irt.gui.data.MyThreadFactory;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.observable.configuration.AttenuationPacket;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class SerialPortController implements Initializable{

	private static final String NETWORK_SELECTION = "Network";
	private final Logger logger = LogManager.getLogger();
	
	private final ExecutorService executor = Executors.newFixedThreadPool(5, new MyThreadFactory());

	public static final String SERIAL_PORT_PREF = "serialPort";
	public static final String ADDRESSES 		= "unit_addresses";

	public static final LinkedPacketsQueue QUEUE =  new LinkedPacketsQueue();

	private static final Preferences prefs = Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);

	private static LinkedPacketSender	serialPort;				public static LinkedPacketSender 	getSerialPort() { return serialPort; }

	private static final ScheduledExecutorService SERVICES = LinkedPacketsQueue.SERVICES;

	private SerialPortStatus portStatus;

	@FXML private ComboBox<String> 	serialPortComboBox;
	@FXML private Button 			openClosePortButton;
	@FXML private Menu				menuBaudrate;

    @FXML private ComboBoxUnitAddress 	comboBoxUnitAddressController;

	private ResourceBundle bundle;

	private final AddressesScaner 		addressesScaner 	= new AddressesScaner();

	@FXML public void initialize() {
 
        selectSerialPort();
		fillMenuBaudrate();

		SERVICES.scheduleAtFixedRate(new SerialPortAnalyzer(), 1, 3, TimeUnit.SECONDS);

        comboBoxUnitAddressController.addObserver((o, address)->{
        	QUEUE.setUnitAddress(((Integer)address).byteValue());
        });
	}

	@Override public void initialize(URL location, ResourceBundle resources) {
		initialize();
		bundle = resources;
//		this.location = location;
	}

	@FXML public void onActionScanUnitAddres(){
		TextInputDialog dialog = new TextInputDialog(prefs.get(ADDRESSES, "101 102 254"));
		dialog.setTitle(bundle.getString("address.scan"));
		dialog.setHeaderText(bundle.getString("address.scan.text"));
		dialog.initOwner(openClosePortButton.getScene().getWindow());
//		dialog.initModality(Modality.WINDOW_MODAL);
		Optional<String> result = dialog.showAndWait();

		result
		.ifPresent(addresses -> {
			addressesScaner.setAddrersses(addresses);
			SERVICES.execute(addressesScaner);
		});
	}

	@FXML public void onActionNetworkInfo(){
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Information Dialog");
		alert.setHeaderText("Network Info");
		StringBuilder sb = new StringBuilder();
		try {

			final InetAddress localHost = InetAddress.getLocalHost();
			sb.append("Computer name: ");
			sb.append(localHost.getHostName());
			sb.append(", IP: ");
			sb.append(localHost.getHostAddress());

			final String numder = serialPort!=null ? serialPort.getPortName().replaceAll("\\D", "") : "0";
			int port = SocketWorker.SERVER_PORT + Integer.parseInt(numder);
			if(port>SocketWorker.SERVER_PORT){
				sb.append(", Port: ");
				sb.append(port);
			}else
				sb.append(", Server is not running");

		} catch (UnknownHostException e) {
			sb.append("Information can not be obtained");
		}

		alert.setContentText(sb.toString());

		alert.showAndWait();
	}

	@FXML public void onActionSelectSerialPort(){
		logger.entry();

		closePort();

		String serialPortName = serialPortComboBox.getSelectionModel().getSelectedItem();
		if(serialPortName!=null){

			if(serialPortName.equals(NETWORK_SELECTION)){
				closePort();
				Optional<Pair<String, String>> result = showDialog();
				result.ifPresent(r->QUEUE.setNetwork(r));
			}else{
				serialPort = new LinkedPacketSender(serialPortName);
				openPort();
			}

			prefs.put(SERIAL_PORT_PREF, serialPortName);
		}

		QUEUE.setComPort(serialPort);
	}

	@FXML public void onOpenClosePortButtonClicked(ActionEvent event){
		logger.entry();

		switch(portStatus){
		case OPEND:
			closePort();
			break;
		default:
			openPort();
		case NOT_SELECTED:
		}
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

	private void selectSerialPort() {

        ObservableList<String> items = serialPortComboBox.getItems();
        items.clear();
		items.addAll(SerialPortList.getPortNames());

		//Network 
		items.add(NETWORK_SELECTION);

        String serialPortName = prefs.get(SERIAL_PORT_PREF, null);
        if(serialPortName!=null && !serialPortName.contains("Select")){
        	serialPortComboBox.getSelectionModel().select(serialPortName);
        	onActionSelectSerialPort();
        }
	}

	synchronized public void openPort(){
		logger.entry();

		
		if(serialPort!=null && !serialPort.isOpened()){

			try {

				if(serialPort.openPort())
					logger.info("Serial Port {} is opened", serialPort);
				else
					logger.error("It is not posible to open {} port", serialPort);

			} catch (SerialPortException e) {
				catchError(e);
			}

		}
	}

	synchronized public void closePort(){
		logger.entry();

		if(serialPort!=null && serialPort.isOpened()){

			try {
				if(serialPort.closePort())
					logger.info("Serial Port {} is closed", serialPort.getPortName());
				else
					logger.error("It is not posible to close {} port", serialPort);
			} catch (SerialPortException e) {
				catchError(e);
			}
		}
	}

	private void catchError(SerialPortException e) {
		if(e.getLocalizedMessage().contains(SerialPortStatus.BUSY.name().toLowerCase()))
			portStatus = SerialPortStatus.BUSY;
		else
			portStatus = SerialPortStatus.ERROR;

		logger.catching(e);
	}

	private void fillMenuBaudrate() {
		final ToggleGroup toggleGroup = new ToggleGroup();
		final List<MenuItem> collect = Arrays
										.stream(Baudrate.values())
										.map(b->createMenuItem(b, toggleGroup))
										.collect(Collectors.toList());
		final ObservableList<MenuItem> items = menuBaudrate.getItems();
		items.addAll(collect);
		selectBaudraet(items);
	}

	public void selectBaudraet(ObservableList<MenuItem> items) {

		if(serialPort==null)
			return;

		final int b = prefs.getInt("baudrate", LinkedPacketSender.getBaudrate().getBaudrate());
		final Baudrate valueOf = Baudrate.valueOf(b);
		serialPort.setBaudrate(valueOf);
		items
		.stream()
		.forEach(i->{
			if(i.getUserData() == valueOf)
				((RadioMenuItem)i).setSelected(true);
		});
	}

	public RadioMenuItem createMenuItem(Baudrate b, ToggleGroup toggleGroup) {
		final RadioMenuItem menuItem = new RadioMenuItem(b.toString());
		menuItem.setToggleGroup(toggleGroup);
		menuItem.setUserData(b);
		menuItem.setOnAction(e->{
			final MenuItem source = (MenuItem)e.getSource();
				final Baudrate userData = (Baudrate)source.getUserData();
				serialPort.setBaudrate(userData);

				final int baudrate = userData.getBaudrate();
				prefs.putInt("baudrate", baudrate);
		});
		return menuItem;
	}

	//*********************************************   SerialPortAnalyzer   ****************************************************************
	private final class SerialPortAnalyzer implements Runnable {

		@Override
		public void run() {

			SerialPortStatus portStatus = null;

			if(serialPort==null){
				portStatus = SerialPortStatus.NOT_SELECTED;
				addStyleClass("warning", "error", "connected");

			}else if(serialPort.isOpened()){
				portStatus = SerialPortStatus.OPEND;
				addStyleClass("connected", "error", "warning");

			}else if(SerialPortController.this.portStatus!=SerialPortStatus.BUSY && SerialPortController.this.portStatus!=SerialPortStatus.ERROR){
				portStatus = SerialPortStatus.CLOSED;
				addStyleClass("warning", "connected", "error");
			}else
				addStyleClass("error", "connected", "warning");

			if(portStatus!=SerialPortController.this.portStatus){
				if(portStatus!=null)
					SerialPortController.this.portStatus = portStatus;

				final String text = "Serial port is " + SerialPortController.this.portStatus;
				if(!openClosePortButton.getText().equals(text))
					Platform.runLater(()->openClosePortButton.setText(text));
			}
		}

		private void addStyleClass(String toAdd, String... toRemove) {
			Platform.runLater(()->{
				final ObservableList<String> styleClass = openClosePortButton.getStyleClass();
				if(!styleClass.contains(toAdd)){
					styleClass.removeAll(toRemove);
					styleClass.add(toAdd);
				}
			});
		}
	}

	//*********************************************   AddressesScaner   ****************************************************************
	private final class AddressesScaner implements Runnable {

		String addresses; private void setAddrersses(String addresses) { this.addresses = addresses; }

		@Override
		public void run() {

			final String[] split = addresses.split("\\D+");
			if(split.length==0 || (split.length==1 && split[0].isEmpty())){
				alertMessage();
				return;
			}

			prefs.put(ADDRESSES, Arrays.toString(split));

			final List<Integer> bytes = Arrays
					.stream(split)
					.filter(r->!r.isEmpty())
					.map(Integer::parseInt)
					.collect(Collectors.toList());

			final int size = bytes.size();
			final Boolean[] results = new Boolean[size];

			bytes
			.forEach(addr->{
				try {
					final AttenuationPacket packet = new AttenuationPacket();
					packet.getLinkHeader().setAddr(addr.byteValue());
					packet.addObserver((o, arg)->{
						executor.execute(()->{

							final LinkedPacket lp = (LinkedPacket)o;
							final int indexOf = bytes.indexOf(lp.getLinkHeader().getAddr()&0xFF);
							results[indexOf] = lp.getAnswer()!=null;
							if(!Arrays.asList(results).contains(null)){

								Platform.runLater(()->{
									
									final List<ButtonType> buttons = createButtons(bytes, results);

									Alert alert = new Alert(AlertType.CONFIRMATION);
									alert.initOwner(openClosePortButton.getScene().getWindow());
									alert.setTitle(bundle.getString("choice"));
									alert.setHeaderText(bundle.getString("address.choose"));
//									alert.setContentText("Choose your option.");
									alert.getButtonTypes().setAll(buttons);
									Optional<ButtonType> result = alert.showAndWait();
									final ButtonType buttonType = result.get();
									if(buttonType.getButtonData()==ButtonData.OK_DONE){
										QUEUE.setUnitAddress((byte) Integer.parseInt(buttonType.getText()));
									}
								});

							}
						});
					});

					QUEUE.add(packet, false);
				} catch (Exception e) {
					logger.catching(e);
				}
			});
		}

		public void alertMessage() {
			Platform.runLater(()->{
				Alert alert = new Alert(AlertType.WARNING);
				alert.initOwner(openClosePortButton.getScene().getWindow());
				alert.setTitle(bundle.getString("warning"));
				alert.setHeaderText(bundle.getString("error.input"));
				alert.showAndWait();
			});
		}

		public List<ButtonType> createButtons(final List<Integer> bytes, Boolean[] results) {
			List<ButtonType> buttons = new ArrayList<>();
			for(int i=0; i<bytes.size() ; i++){
				if(results[i]){
					ButtonType button = new ButtonType(bytes.get(i).toString(), ButtonData.OK_DONE);
					buttons.add(button);
				}
			}
			buttons.add(new ButtonType("Cancel", ButtonData.CANCEL_CLOSE));
			return buttons;
		}
	}
}
