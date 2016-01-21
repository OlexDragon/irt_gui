
package irt.gui.controllers.components;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.controllers.ScheduledServices;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.observable.device_debug.CallibrationModePacket;
import irt.gui.data.packet.observable.device_debug.CallibrationModePacket.CalibrationMode;
import irt.gui.errors.PacketParsingException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class ButtonCalibrationMode extends Observable implements Runnable, Observer {
	private final Logger logger = LogManager.getLogger();

	private static final ScheduledExecutorService SERVICES = ScheduledServices.services;
	private ScheduledFuture<?> scheduleAtFixedRate;

	private CallibrationModePacket packetGet;
	private CallibrationModePacket packetSetOn;
	private CallibrationModePacket packetSetOff;

	private long period = 5000; //time between requests

	private CalibrationMode callibrationMode;

	@FXML private Button button;

	@FXML void initialize() {
		try {
			button.setUserData(this);

			packetGet = new CallibrationModePacket((CalibrationMode)null);
			packetGet.addObserver(this);

			packetSetOn = new CallibrationModePacket(CalibrationMode.ON);
			packetSetOn.addObserver(this);

			packetSetOff = new CallibrationModePacket(CalibrationMode.OFF);
			packetSetOff.addObserver(this);

			start();

		} catch (PacketParsingException e) {
			logger.catching(e);
		}
	 }

	@FXML void buttonAction(ActionEvent event) {

		if(callibrationMode!=null){
			try {

				CallibrationModePacket packet = callibrationMode==CalibrationMode.ON ? packetSetOff : packetSetOn;
				logger.trace(packet);
				packet.setAnswer(null);
				SerialPortController.QUEUE.add(packet);

			} catch (Exception e) {
				logger.catching(e);
			}
		}
	}

	@Override
	public void run() {
		try{
			logger.entry(packetGet);

			packetGet.setAnswer(null);
			SerialPortController.QUEUE.add(packetGet);

		}catch(Exception ex){
			logger.catching(ex);
		}
	}

	private final Updater updater = new Updater();
	@Override
	public void update(Observable observable, Object arg) {
		logger.entry(observable);
		updater.setPacket(((LinkedPacket)observable).getAnswer());
		SERVICES.execute(updater);

		setChanged();
		notifyObservers(observable);
	}

	public void start(){
		if(scheduleAtFixedRate==null || scheduleAtFixedRate.isCancelled())
			scheduleAtFixedRate = SERVICES.scheduleAtFixedRate(this, 1, period, TimeUnit.MILLISECONDS);
	}

	public void stop(){
		if(scheduleAtFixedRate==null)
			scheduleAtFixedRate.cancel(true);
	}

	@Override
	public synchronized void addObserver(Observer o) {
		super.addObserver(o);

		callibrationMode = null;// When callibrationMode==null Observers will by notified
	}

	//********************************************** class Update   *******************************************************
	private class Updater implements Runnable{

		private byte[] aswer;	public void setPacket(byte[] answer) { this.aswer = answer; }

		@Override
		public void run() {
			
			try {

				CallibrationModePacket p = new CallibrationModePacket(aswer);

				CalibrationMode cm = p.getCallibrationMode();

				if (cm != callibrationMode) {
				logger.trace("{}:{}", callibrationMode, cm);

					callibrationMode = cm;
					final String text = "Callibration Mode is " + cm;

					Platform.runLater(() -> button.setText(text));

					setChanged();
					notifyObservers(cm);
				}

			} catch (Exception e) {
				logger.catching(e);
			}
		}
	}
}
