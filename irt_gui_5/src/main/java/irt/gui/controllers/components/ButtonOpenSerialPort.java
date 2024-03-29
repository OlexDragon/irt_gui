package irt.gui.controllers.components;

import irt.gui.controllers.enums.SerialPortStatus;
import irt.gui.controllers.serial_port.IrtSerialPort;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class ButtonOpenSerialPort {

	private ComboBoxSerialPort comboBoxSerialPort;

	public void setComboBoxSerialPort(ComboBoxSerialPort comboBoxSerialPort) {
		if(this.comboBoxSerialPort!=null)
			throw new ExceptionInInitializerError("ComboBoxSerialPort");

		this.comboBoxSerialPort = comboBoxSerialPort;
		final SerialPortStatus serialPortStatus = comboBoxSerialPort.getSerialPortStatus();

		if(serialPortStatus!=null){
			addStyleClass("error", "warning", "connected");
			setText(serialPortStatus);
		}else{

			final IrtSerialPort serialPort = comboBoxSerialPort.getSerialPort();
			setSerialPortStatus(serialPort);
		}

		comboBoxSerialPort.addObserver(( o, arg)->{
			if(arg instanceof SerialPortStatus){
				addStyleClass("error", "warning", "connected");
				setText((SerialPortStatus) arg);
			}else{
				final IrtSerialPort sp = ((ComboBoxSerialPort)o).getSerialPort();
				setSerialPortStatus(sp);
			}
		});
	}

	private void setSerialPortStatus(IrtSerialPort serialPort) {

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
		final String text = "Serial port is " + portStatus;
		if(!button.getText().equals(text))
			Platform.runLater(()->button.setText(text));
	}

	@FXML private Button button;

	@FXML public void initialize() {
		
	}

	@FXML private void onOpenClosePortButtonClicked(){

		final IrtSerialPort serialPort = comboBoxSerialPort.getSerialPort();
		if(serialPort.isOpened())
			comboBoxSerialPort.closePort();
		else
			comboBoxSerialPort.openPort();
	}

	private void addStyleClass(String toAdd, String... toRemove) {
		Platform.runLater(()->{
			final ObservableList<String> styleClass = button.getStyleClass();
			if(!styleClass.contains(toAdd)){
				styleClass.removeAll(toRemove);
				styleClass.add(toAdd);
			}
		});
	}
}
