package irt.serial.port.controllers;

import java.util.Observable;
import java.util.Observer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.serial.port.enums.SerialPortStatus;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class ButtonOpenSerialPort implements Observer {
	private final Logger logger = LogManager.getLogger();

	private ComboBoxSerialPort comboBoxSerialPort;

	public void setComboBoxSerialPort(ComboBoxSerialPort comboBoxSerialPort) {
		logger.entry(comboBoxSerialPort);

		if(this.comboBoxSerialPort!=null)
			throw new ExceptionInInitializerError("ComboBoxSerialPort");

		this.comboBoxSerialPort = comboBoxSerialPort;
		final SerialPortStatus serialPortStatus = comboBoxSerialPort.getSerialPortStatus();

		if(serialPortStatus!=null){
			addStyleClass("error", "warning", "connected");
			setText(serialPortStatus);
		}else{

			final PacketSender serialPort = comboBoxSerialPort.getSerialPort();
			setSerialPortStatus(serialPort);
		}

		comboBoxSerialPort.addObserver(this);
	}

	private void setSerialPortStatus(PacketSender serialPort) {

		SerialPortStatus portStatus;
		if(serialPort==null){
			portStatus = SerialPortStatus.NOT_SELECTED;
			addStyleClass("warning", "error", "connected");

		}else if(serialPort.isOpened()){
			portStatus = SerialPortStatus.OPEND;
			addStyleClass("connected", "error", "warning");

		}else{
			portStatus = SerialPortStatus.CLOSED;
			addStyleClass("warning", "connected", "error");
		}

		setText(portStatus);
	}

	private void setText(SerialPortStatus portStatus) {
		logger.entry(portStatus);

		final String text = "Serial port is " + portStatus;
		Platform.runLater(()->serialPortButton.setText(text));
	}

	@FXML private Button serialPortButton;

	@FXML private void onOpenClosePortButtonClicked(){

		final PacketSender serialPort = comboBoxSerialPort.getSerialPort();
		if(serialPort.isOpened())
			comboBoxSerialPort.closePort();
		else
			comboBoxSerialPort.openPort();
	}

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

	@Override
	public void update(Observable o, Object arg) {
		logger.entry(o, arg);

		if(arg instanceof SerialPortStatus){
			addStyleClass("error", "warning", "connected");
			setText((SerialPortStatus) arg);
		}else{
			final PacketSender sp = ((ComboBoxSerialPort)o).getSerialPort();
			setSerialPortStatus(sp);
		}
	}
}
