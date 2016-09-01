package irt.controllers.serial_port;

import java.util.Observable;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.IrtGuiProperties;
import irt.data.LinkedPacketsQueue;
import irt.data.packets.enums.SerialPortStatus;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class ComboBoxSerialPort extends Observable {
	private final Logger logger = LogManager.getLogger();

	private static final Preferences prefs = Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);
	private String prefsName;

	public final LinkedPacketsQueue queue =  new LinkedPacketsQueue();

	private LinkedPacketSender	serialPort;				public LinkedPacketSender 	getSerialPort() { return serialPort; }

	private SerialPortStatus portStatus; public SerialPortStatus getSerialPortStatus() { return portStatus; }

	@FXML private ComboBox<String> serialPortComboBox;

	public void initialize(String prefsName) {

		try{
			ObservableList<String> items = serialPortComboBox.getItems();
			items.clear();
			items.addAll(SerialPortList.getPortNames());

			this.prefsName = prefsName;

			String serialPortName = prefs.get(prefsName, null);
			if(serialPortName!=null && !serialPortName.contains("Select")){
				serialPortComboBox.getSelectionModel().select(serialPortName);
				onActionSelectSerialPort();
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

				serialPort = new LinkedPacketSender(serialPortName);
				openPort();

				prefs.put(prefsName, serialPortName);
				notifyObservers();
			}

			queue.setComPort(serialPort);
		}catch(Exception ex){
			catchError(ex);
		}
	}

	synchronized public void openPort(){
		logger.traceEntry();

		
		if(serialPort!=null && !serialPort.isOpened()){

			try {

				if(serialPort.openPort())
					logger.info("Serial Port {} is opened", serialPort);
				else
					logger.error("It is not posible to open {} port", serialPort);

				notifyObservers();

			} catch (SerialPortException e) {
				catchError(e);
			}

		}
	}

	public void closePort(){

		if(serialPort!=null && serialPort.isOpened()){

			try {

				if(serialPort.closePort())
					logger.info("Serial Port {} is closed", serialPort.getPortName());
				else
					logger.error("It is not posible to close {} port", serialPort);

			} catch (SerialPortException e) {
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
					serialPort.closePort();
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

	public LinkedPacketsQueue getQueue() {
		return queue;
	}
}
