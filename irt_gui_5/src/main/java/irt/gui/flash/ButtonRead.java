package irt.gui.flash;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.controllers.components.SerialPortController;
import irt.gui.data.MyThreadFactory;
import irt.gui.data.packet.Packet;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.observable.flash.EmptyPacket;
import irt.gui.data.packet.observable.flash.ReadPacket;
import irt.gui.flash.PanelFlash.UnitAddress;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

public class ButtonRead extends Observable implements Observer, Initializable {

	public static final String WARNING = "warning";

	@FXML private Button button;

	private final ReadPacket readPacket = new ReadPacket();
	private final EmptyPacket emptyPacket = new EmptyPacket();
	private final LinkedPacket dataPacket = new EmptyPacket(){ @Override public byte[] toBytes() { return dataToSend; }};
	private final ReadObservable sendLengthObsorver = new ReadObservable();

	private UnitAddress unitAddress;
	private ResourceBundle bundle;
	private int readFromAddress;
	private byte[] dataToSend;

	private final ExecutorService executor = Executors.newSingleThreadExecutor(new MyThreadFactory());

	private final Observer sendAddressObsorver = (o, arg)->{
		executor.execute(()->{
			
			if(PanelFlash.checkAswer("Send Address: ", (LinkedPacket)o, button)){

				dataToSend = PanelFlash.LENGTH;
				dataPacket.deleteObservers();

				final Observer reset = sendLengthObsorver.reset();
				dataPacket.addObserver(reset);

				SerialPortController.QUEUE.add(dataPacket, false);

			}else
				Platform.runLater(()->{
					button.setText(bundle.getString("read"));
					button.getStyleClass().remove(ButtonRead.WARNING);
				});
		});
	};

	private final Observer readCommandObserver = (o, arg)->{
		executor.execute(()->{
			
			if(PanelFlash.checkAswer("Read Command: ", (LinkedPacket)o, button)){

				byte[] address = Packet.toBytes(readFromAddress);
				dataToSend = PanelFlash.addCheckSum(address);
				dataPacket.deleteObservers();
				dataPacket.addObserver(sendAddressObsorver);
				SerialPortController.QUEUE.add(dataPacket, false);
			}else
				Platform.runLater(()->{
					button.setText(bundle.getString("read"));
					button.getStyleClass().remove(ButtonRead.WARNING);
				});
		});
	};

	@Override public void initialize(URL location, ResourceBundle resources) {
		bundle = resources;
//		this.location = location;

		readPacket.addObserver(readCommandObserver);
		emptyPacket.addObserver(sendLengthObsorver);
	}

	@FXML private void onAction() {
		readFromAddress = unitAddress.getAddr();
		button.setText(bundle.getString("read.reading"));
		addWarningClass();
		SerialPortController.QUEUE.add(readPacket, false);
		notifyObservers();
	}

	@Override public void update(Observable o, Object arg) {
		if(arg instanceof UnitAddress){
			unitAddress = (UnitAddress)arg;
			button.setTooltip(new Tooltip(unitAddress.toString()));
		}
	}

	@Override public void notifyObservers() {
		setChanged();
		super.notifyObservers();
	}

	@Override public void notifyObservers(Object arg) {
		setChanged();
		super.notifyObservers(arg);
	}

	public static byte[] removeFF(byte[] answer) {
		int count = 0;
		int end = -1;
		for(int i=0; i<answer.length; i++){

			if(answer[i]==(byte)0xFF){
				count++;

				if(end<0)
					end = i;

				if(count>2)
					return Arrays.copyOf(answer, end);

			}else{
				count = 0;
				end = -1;
			}
		}
		return answer;
	}

	private void addWarningClass() {
		final ObservableList<String> styleClass = button.getStyleClass();
		if(!styleClass.contains(ButtonRead.WARNING))
			styleClass.add(ButtonRead.WARNING);
	}

	//*********************  class ReadObservable   *************************
	private class ReadObservable implements Observer{

		private final Logger logger = LogManager.getLogger();
		private int totalLength;

		@Override
		public void update(Observable o, Object arg) {

			LinkedPacket packet = (LinkedPacket) o;
			byte[] answer = packet.getAnswer();
			int l;

			if(answer == null || (l = answer.length)==0){
				PanelFlash.showAlert(AlertType.ERROR, "It is impossible to read the data", button);
				dataPacket.deleteObservers();
				return;
			}

			byte[] a = removeFF(answer);
			boolean hasMore = a.length==answer.length;

			if(totalLength == 0)
				a = Arrays.copyOfRange(a, 1, a.length);

			String textToShow;
			try {

				textToShow = new String(a, "UTF-8");

			} catch (UnsupportedEncodingException e) {
				logger.catching(e);
				textToShow = "\n***  Read Error  ***   ";
			}

			totalLength += l;

			ButtonRead.this.notifyObservers(textToShow);

			if(hasMore)
				if(totalLength<257){
					SerialPortController.QUEUE.add(emptyPacket, false);
				}else{
					readFromAddress += PanelFlash.MAX_VAR_RAM_SIZE;
					SerialPortController.QUEUE.add(readPacket, false);
				}
			else
				Platform.runLater(()->{
					button.setText(bundle.getString("read"));
					button.getStyleClass().remove(ButtonRead.WARNING);
				});
		}

		public Observer reset(){
			totalLength = 0;
			return this;
		}
	}
}
