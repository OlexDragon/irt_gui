package irt.gui.controllers.flash;

import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.controllers.IrtSerialPort;
import irt.gui.controllers.components.SerialPortController;
import irt.gui.data.ToHex;
import irt.gui.data.packet.observable.production.ConnectFCMPacket;
import irt.gui.errors.PacketParsingException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import jssc.SerialPort;

public class ButtonFCM implements Observer, Initializable {
	private final Logger logger = LogManager.getLogger();

	private ConnectFCMPacket packet;

	@FXML private Button button;

	private ResourceBundle bundle;
	private int parity;
	private ButtonConnect connectButton;

	public ButtonFCM() {
		try {

			packet = new ConnectFCMPacket();

		} catch (PacketParsingException e) {
			logger.catching(e);
		}
	}

	@Override public void initialize(URL location, ResourceBundle resources) {
		bundle = resources;
//		this.location = location;
		packet.addObserver(this);
	}

	@FXML private void onAction() {
		final IrtSerialPort serialPort = SerialPortController.getSerialPort();
		parity = serialPort.getParity();
		serialPort.setParity(SerialPort.PARITY_NONE);
		try {
			serialPort.setParams();
		} catch (Exception e1) {
			logger.catching(e1);
		}

		button.setText(bundle.getString("connect.connecting"));
		SerialPortController.getQueue().add(packet, true);
	}

	@Override public void update(Observable o, Object arg) {
		logger.traceEntry("{}; {}", ()->ToHex.bytesToHex(((ConnectFCMPacket)o).toBytes()), ()->o);

		final IrtSerialPort serialPort = SerialPortController.getSerialPort();
		serialPort.setParity(parity);
		try {

			serialPort.setParams();
			Thread.sleep(2000);

		} catch (Exception e1) {
			logger.catching(e1);
		}

		Platform.runLater(()->{
			button.setText(bundle.getString("connect.connected"));
			connectButton.onAction();
		});
	}

	public void setConnectButton(ButtonConnect connectButton) {
		this.connectButton = connectButton;
	}
}
