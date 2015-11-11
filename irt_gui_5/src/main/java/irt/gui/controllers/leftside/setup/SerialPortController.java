package irt.gui.controllers.leftside.setup;

import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.controllers.LinkedPacketSender;
import irt.gui.controllers.LinkedPacketsQueue;
import irt.gui.controllers.ScheduledServices;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class SerialPortController{

	private final Logger logger = LogManager.getLogger();

	public enum SerialPortStatus{
		NOT_SELECTED,
		CLOSED,
		OPEND,
		BUSY,
		ERROR
	}
	
	public static final String SERIAL_PORT_PREF = "serialPort";

	public static final LinkedPacketsQueue QUEUE =  new LinkedPacketsQueue();

	protected final Preferences prefs = Preferences.userRoot().node("IRT Technologies inc.");

	private volatile static LinkedPacketSender	serialPort;					public static SerialPort 	getSerialPort() { return serialPort; 			}

	private SerialPortStatus portStatus;

	@FXML private ComboBox<String> 	serialPortComboBox;
	@FXML private Button 			openClosePortButton;

	@FXML public void initialize() {
		logger.entry();
 
        selectSerialPort();

        ScheduledServices.services.scheduleAtFixedRate(new SerialPortAnalyzer(), 1, 1, TimeUnit.SECONDS);
        logger.exit();
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

	@FXML public void selectSerialPort(ActionEvent event){
		logger.entry();

		closePort();

		String serialPortName = serialPortComboBox.getSelectionModel().getSelectedItem();
		if(serialPortName!=null){

			serialPort = new LinkedPacketSender(serialPortName);
			openPort();

			prefs.put(SERIAL_PORT_PREF, serialPortName);
		}

		QUEUE.setComPort(serialPort);
	}

	private void selectSerialPort() {

        ObservableList<String> items = serialPortComboBox.getItems();
        items.clear();
		items.addAll(SerialPortList.getPortNames());

        String serialPortName = prefs.get(SERIAL_PORT_PREF, null);
        if(serialPortName!=null && !serialPortName.contains("Select")){
        	serialPortComboBox.getSelectionModel().select(serialPortName);
        	selectSerialPort(null);
        }
	}

	synchronized public void openPort(){
		logger.entry();

		
		if(serialPort!=null && !serialPort.isOpened()){

			try {

				if(serialPort.openPort())
					logger.info("Serial Port {} is opened", serialPort);
				else
					logger.error("It is not posible to open prt {}", serialPort);

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
					logger.error("It is not posible to close prt {}", serialPort);
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

	//*********************************************   SerialPortAnalyzer   ****************************************************************
	private final class SerialPortAnalyzer implements Runnable {

		@Override
		public void run() {

			SerialPortStatus portStatus = null;

			if(serialPort==null){
				portStatus = SerialPortStatus.NOT_SELECTED;

			}else if(serialPort.isOpened())
				portStatus = SerialPortStatus.OPEND;

			else if(SerialPortController.this.portStatus!=SerialPortStatus.BUSY && SerialPortController.this.portStatus!=SerialPortStatus.ERROR)
				portStatus = SerialPortStatus.CLOSED;

			if(portStatus!=SerialPortController.this.portStatus){
				if(portStatus!=null)
					SerialPortController.this.portStatus = portStatus;

				final String text = "Serial port is " + SerialPortController.this.portStatus;
				if(!openClosePortButton.getText().equals(text))
					Platform.runLater(new Runnable() {
					
						@Override
						public void run() {
							openClosePortButton.setText(text);
						}
					});
			}
		}
	}
}
