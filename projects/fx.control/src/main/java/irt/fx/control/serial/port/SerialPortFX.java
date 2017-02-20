package irt.fx.control.serial.port;

import java.io.IOException;
import java.net.URL;
import java.util.Observer;

import irt.serial.port.controllers.PacketSender;
import irt.serial.port.enums.SerialPortStatus;
import irt.serial.port.fx.ButtonOpenSerialPortFX;
import irt.serial.port.fx.ComboBoxSerialPortFx;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;

public class SerialPortFX extends HBox {

    public SerialPortFX() {

    	final URL resource = getClass().getResource("/fxml/serial_port.fxml");

		FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	}

	@FXML  private ComboBoxSerialPortFx comboBoxSerialPort;
    @FXML  private ButtonOpenSerialPortFX serialPortButton;

    @FXML public void initialize() {
    	serialPortButton.setComboBoxSerialPort(comboBoxSerialPort);
    }

    public void initialize(String prefsName) {
		comboBoxSerialPort.initialize(prefsName);
	}

	public PacketSender 	getSerialPort() {
		return comboBoxSerialPort.getSerialPort();
	}

	public void openPort(){
		comboBoxSerialPort.openPort();
	}

	public void closePort(){
		comboBoxSerialPort.closePort();
	}

	public void addObserver(Observer observer) {
		comboBoxSerialPort.addObserver(observer);
	}

	public String getPrefsName() {
		return comboBoxSerialPort.getPrefsName();
	}

	public SerialPortStatus getSerialPortStatus() {
		return comboBoxSerialPort.getSerialPortStatus();
	}
}
