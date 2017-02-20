package irt.serial.port.fx;

import java.io.IOException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.IrtGuiProperties;
import irt.serial.port.controllers.PacketSender;
import irt.serial.port.enums.SerialPortStatus;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;

public class ButtonOpenSerialPortFX extends Button implements Observer {
	private final Logger logger = LogManager.getLogger();

	private ComboBoxSerialPortFx comboBoxSerialPortFx;

	public ButtonOpenSerialPortFX() {

    	final URL resource = getClass().getResource("/fxml/ButtonOpenSerialPort.fxml");
		FXMLLoader fxmlLoader = new FXMLLoader(resource, IrtGuiProperties.BUNDLE);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
//		fxmlLoader.setLocation(resource);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	}

	public void setComboBoxSerialPort(ComboBoxSerialPortFx comboBoxSerialPortFx) {
		logger.entry(comboBoxSerialPortFx);

		if(this.comboBoxSerialPortFx!=null)
			throw new ExceptionInInitializerError("ComboBoxSerialPortFx");

		this.comboBoxSerialPortFx = comboBoxSerialPortFx;
		setSerialPortStatus(comboBoxSerialPortFx.getSerialPortStatus());

		comboBoxSerialPortFx.addObserver(this);
	}

	private void setSerialPortStatus(SerialPortStatus serialPortStatus) {
		logger.entry(serialPortStatus);

		if(serialPortStatus==null){
			addStyleClass("warning", "error", "connected");
			return;
		}
		switch(serialPortStatus){
		case BUSY:
		case ERROR:
		case NOT_FOUND:
			addStyleClass("error", "warning", "connected");
			break;
		case CLOSED:
		case NOT_SELECTED:
			addStyleClass("warning", "error", "connected");
			break;
		case OPEND:
			addStyleClass("connected", "error", "warning");
		
		}

		Platform.runLater(()->setText("Serial port is " + serialPortStatus));
	}

	@FXML private void onOpenClosePortButtonClicked(){

		final PacketSender serialPort = comboBoxSerialPortFx.getSerialPort();
		if(serialPort.isOpened())
			comboBoxSerialPortFx.closePort();
		else
			comboBoxSerialPortFx.openPort();
	}

	private void addStyleClass(String toAdd, String... toRemove) {
		logger.trace("add '{}' class; remove classes: {}", toAdd, toRemove);

		Platform.runLater(()->{
			final ObservableList<String> styleClass = getStyleClass();
			if(!styleClass.contains(toAdd)){
				styleClass.removeAll(toRemove);
				styleClass.add(toAdd);
			}
		});
	}

	@Override
	public void update(Observable o, Object arg) {
		logger.entry(arg);

		setSerialPortStatus(comboBoxSerialPortFx.getSerialPortStatus());
	}
}
