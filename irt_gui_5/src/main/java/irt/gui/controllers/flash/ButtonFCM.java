package irt.gui.controllers.flash;

import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.controllers.LinkedPacketSender;
import irt.gui.controllers.components.SerialPortController;
import irt.gui.data.ToHex;
import irt.gui.data.packet.observable.production.ConnectFCMPacket;
import irt.gui.errors.PacketParsingException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import jssc.SerialPort;
import jssc.SerialPortException;

public class ButtonFCM implements Observer, Initializable {

	private final Logger logger = LogManager.getLogger();

	private ConnectFCMPacket packet;

	@FXML private Button button;

	private ResourceBundle bundle;

	private int parity;

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
		final LinkedPacketSender serialPort = SerialPortController.getSerialPort();
		parity = serialPort.getParity();
		serialPort.setParity(SerialPort.PARITY_NONE);
		try {
			serialPort.setParams();
		} catch (SerialPortException e1) {
			logger.catching(e1);
		}

		button.setText(bundle.getString("connect.connecting"));
		SerialPortController.QUEUE.add(packet, true);
	}

	@Override public void update(Observable o, Object arg) {
		logger.entry( ToHex.bytesToHex(((ConnectFCMPacket)o).toBytes()), o);

		final LinkedPacketSender serialPort = SerialPortController.getSerialPort();
		serialPort.setParity(parity);
		try {
			serialPort.setParams();
		} catch (SerialPortException e1) {
			logger.catching(e1);
		}

		//		try {

		//					7E FE 00 00 00 03 00 1B 64 00 00 00 02 00 00 8B 08 7E
		//					7E FE 00 00 00 03 00 78 64 00 00 00 02 00 00 5A 51 7E
		//					0x7E, (byte) 0xFE, 0x00, 0x00, 0x00, 0x03, 0x00, 0x78, 0x64, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x5A, 0x51, 0x7E

		//			ConnectFCMPacket p = new ConnectFCMPacket(((ConnectFCMPacket)o).getAnswer(), true);
//			logger.trace(p);
//
//		} catch (PacketParsingException e) {
//			logger.catching(e);
//		}
	}
}
