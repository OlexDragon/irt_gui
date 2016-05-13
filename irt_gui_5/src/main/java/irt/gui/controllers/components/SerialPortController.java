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
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.IrtGuiProperties;
import irt.gui.controllers.LinkedPacketSender;
import irt.gui.controllers.LinkedPacketsQueue;
import irt.gui.controllers.enums.Baudrate;
import irt.gui.controllers.socket.SocketWorker;
import irt.gui.data.MyThreadFactory;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.observable.configuration.AttenuationPacket;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;

public class SerialPortController implements Initializable{
	private final Logger logger = LogManager.getLogger();
	
	private final ExecutorService executor = Executors.newFixedThreadPool(5, new MyThreadFactory());

	public static final String SERIAL_PORT_PREF = "serialPort";
	public static final String ADDRESSES 		= "unit_addresses";

	private static LinkedPacketsQueue queue; public static LinkedPacketsQueue getQueue() { return queue; }

	private static final Preferences prefs = Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);

	private static LinkedPacketSender	serialPort;				public static LinkedPacketSender 	getSerialPort() { return serialPort; }

	private static final ScheduledExecutorService SERVICES = LinkedPacketsQueue.SERVICES;

	@FXML private AnchorPane anchorPane;
	@FXML private ComboBoxSerialPort	serialPortComboBoxController;
	@FXML private ButtonOpenSerialPort  openClosePortButtonController;
    @FXML private ComboBoxUnitAddress 	comboBoxUnitAddressController;
	@FXML private Menu					menuBaudrate;

	private ResourceBundle bundle;

	private final AddressesScaner 		addressesScaner 	= new AddressesScaner();

	@FXML public void initialize() {

		fillMenuBaudrate();

		serialPortComboBoxController.initialize(SERIAL_PORT_PREF);
		queue = serialPortComboBoxController.getQueue();
        comboBoxUnitAddressController.addObserver((o, address)->{
        	queue.setUnitAddress(((Integer)address).byteValue());
        });
	}

	@Override public void initialize(URL location, ResourceBundle resources) {
		initialize();
		bundle = resources;
//		this.location = location;

		openClosePortButtonController.setComboBoxSerialPort(serialPortComboBoxController);

		serialPort = serialPortComboBoxController.getSerialPort();
		queue = serialPortComboBoxController.getQueue();
		serialPortComboBoxController.addObserver((o, arg)->{
			serialPort = serialPortComboBoxController.getSerialPort();
		});
	}

	@FXML public void onActionScanUnitAddres(){
		TextInputDialog dialog = new TextInputDialog(prefs.get(ADDRESSES, "101 102 254"));
		dialog.setTitle(bundle.getString("address.scan"));
		dialog.setHeaderText(bundle.getString("address.scan.text"));
		dialog.initOwner(anchorPane.getScene().getWindow());
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
									alert.initOwner(anchorPane.getScene().getWindow());
									alert.setTitle(bundle.getString("choice"));
									alert.setHeaderText(bundle.getString("address.choose"));
//									alert.setContentText("Choose your option.");
									alert.getButtonTypes().setAll(buttons);
									Optional<ButtonType> result = alert.showAndWait();
									final ButtonType buttonType = result.get();
									if(buttonType.getButtonData()==ButtonData.OK_DONE){
										queue.setUnitAddress((byte) Integer.parseInt(buttonType.getText()));
									}
								});

							}
						});
					});

					queue.add(packet, false);
				} catch (Exception e) {
					logger.catching(e);
				}
			});
		}

		public void alertMessage() {
			Platform.runLater(()->{
				Alert alert = new Alert(AlertType.WARNING);
				alert.initOwner(anchorPane.getScene().getWindow());
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
