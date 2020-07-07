package irt.gui.controllers.calibration;

import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.IrtGuiProperties;
import irt.gui.controllers.calibration.tools.Tool;
import irt.gui.controllers.calibration.tools.data.ToolsFrequency;
import irt.gui.controllers.calibration.tools.data.ToolsPower;
import irt.gui.controllers.calibration.tools.enums.FrequencyUnits;
import irt.gui.controllers.calibration.tools.enums.PowerUnits;
import irt.gui.controllers.calibration.tools.enums.ToolsState;
import irt.gui.data.MyThreadFactory;
import irt.gui.data.listeners.NumericChecker;
import irt.gui.data.packet.interfaces.PacketToSend;
import irt.gui.data.packet.observable.calibration.IdPacket;
import irt.gui.data.packet.observable.calibration.ToolsFrequencyPacket;
import irt.gui.data.packet.observable.calibration.ToolsOutputPacket;
import irt.gui.data.packet.observable.calibration.ToolsPacket;
import irt.gui.data.packet.observable.calibration.ToolsPowerPacket;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class PanelSignalGenerator implements Tool{

	private static final String NO_ANSWER = "No Answer";

	private final Logger logger = LogManager.getLogger();

	private static final Preferences prefs = Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);
	private final ExecutorService EXECUTOR = Executors.newFixedThreadPool(5, new MyThreadFactory());

	@FXML private Label labelId;
	@FXML private TextField textFieldAddress;
	@FXML private TextField textFieldFrequency;
	@FXML private TextField textFieldPower;
	@FXML private ComboBox<ToolsState> comboBoxOutput;

	private PanelPrologix prologix;

	private final ToolsPacket idPacket = new IdPacket();
	private final ToolsPacket frPacket = new ToolsFrequencyPacket();
	private final ToolsPacket pwPacket = new ToolsPowerPacket();
	private final ToolsPacket outPacket = new ToolsOutputPacket();

	public PanelSignalGenerator() {
		idPacket.addObserver(observerGetInfo);
	}

	private Observer observerGetInfo = new Observer() {
		
		@Override
		public void update(Observable o, Object arg) {
			logger.traceEntry("{}", o);

			final PacketToSend tp = (PacketToSend) o;

			EXECUTOR.execute(()->{
				final String text = Optional
										.ofNullable(tp.getAnswer())
										.map(String::new)
										.map(String::trim)
										.orElse(NO_ANSWER);

				Platform.runLater(()->labelId.setText(text));
			});
		}
	};

	private Observer observerFrequency = new Observer() {
		
		@Override
		public void update(Observable o, Object arg) {
			logger.traceEntry("{}", o);

			final PacketToSend tp = (PacketToSend) o;
			tp.deleteObserver(observerFrequency);

			EXECUTOR.execute(()->{
				final byte[] answer = tp.getAnswer();
				final Double d = getDouble(answer).orElse(null);

				final String text = Optional
										.ofNullable(d)
										.map(FrequencyUnits::toString)
										.orElse(NO_ANSWER);

				Platform.runLater(()->textFieldFrequency.setText(text));
			});
		}
	};

	private Observer observerPower = new Observer() {
		
		@Override
		public void update(Observable o, Object arg) {
			logger.traceEntry("{}", o);

			final PacketToSend tp = (PacketToSend) o;
			tp.deleteObserver(observerPower);

			EXECUTOR.execute(()->{
				final byte[] answer = tp.getAnswer();
				final Double d = getDouble(answer).orElse(null);

				final String text = Optional
										.ofNullable(d)
										.map(PowerUnits.DBM::valueOf)
										.orElse(NO_ANSWER);

				Platform.runLater(()->textFieldPower.setText(text));
			});
		}
	};

	private Observer observerOutput = new Observer() {
		
		@Override
		public void update(Observable o, Object arg) {
			logger.traceEntry("{}", o);
			o.deleteObserver(observerOutput);

			final PacketToSend tp = (PacketToSend) o;

			EXECUTOR.execute(()->{
				final ToolsState ts = Optional
										.ofNullable(tp.getAnswer())
										.map(ToolsState::valueOf)
										.orElse(null);

				Platform.runLater(()->comboBoxOutput.getSelectionModel().select(ts));
			});
		}
	};

	@FXML private void initialize() {
		new NumericChecker(textFieldAddress.textProperty());
		final String addr = prefs.get("sgAddr", "19");
		textFieldAddress.setText(addr);
		textFieldAddress.focusedProperty().addListener((observable, oldValue, newValue)->{
			if(!newValue)
				prefs.put("powerMeterAddr", textFieldAddress.getText());
		});

		comboBoxOutput.getItems().addAll(ToolsState.values());
	}

	@FXML private void onActionSignalGeneratorGet(ActionEvent event) {
		try {

			prologix.send(textFieldAddress.getText(), idPacket);
			Thread.sleep(1000);

			onActionGetFrequency();

			onActionSetPower();

			onActionGetRF();

			} catch (Exception e) {
				logger.catching(e);
			}
	}

	@FXML private void onActionSetFrequency() {
		final String text = textFieldFrequency.getText();
		if(text.matches(".*\\d+.*")){
			frPacket.getCommand().setValue(new ToolsFrequency(text));
			frPacket.deleteObservers();
			prologix.send(textFieldAddress.getText(), frPacket);
		}else{
			frPacket.addObserver(observerFrequency);
			prologix.send(textFieldAddress.getText(), frPacket);
		}
	}

	@FXML private void onActionGetFrequency() {
		frPacket.addObserver(observerFrequency);
		prologix.send(textFieldAddress.getText(), frPacket);
	}

	@FXML private void onActionSetPower() {

		final String power = textFieldPower.getText();
		final String addr = textFieldAddress.getText();


		if(power.matches(".*\\d+.*")){//if has value
			pwPacket.getCommand().setValue(new ToolsPower(power));
			pwPacket.deleteObservers();
			prologix.send(addr, pwPacket);

		}else{
			pwPacket.addObserver(observerPower);
			prologix.send(addr, pwPacket);
		}
	}

	@FXML private void onActionGetPower() {
		pwPacket.addObserver(observerPower);
		prologix.send(textFieldAddress.getText(), pwPacket);
	}

	@FXML private void onActionSetRF() {
		logger.traceEntry();

		if(comboBoxOutput.getSelectionModel().getSelectedItem()==null){
			outPacket.addObserver(observerOutput);
			outPacket.deleteObservers();
			prologix.send(textFieldAddress.getText(), outPacket);
		}else{
			outPacket.getCommand().setValue(comboBoxOutput.getSelectionModel().getSelectedItem());
			prologix.send(textFieldAddress.getText(), outPacket);
		}
	}

	@FXML private void onActionGetRF() {
		outPacket.addObserver(observerOutput);
		prologix.send(textFieldAddress.getText(), outPacket);
	}

	public void setPrologix(PanelPrologix prologix) {
		this.prologix = prologix; 
	}

	private void sendCommand(final ToolsPacket packet, Observer... observers) {
		logger.traceEntry("{}; {}", packet, observers);
		Optional
		.ofNullable(observers)
		.ifPresent(os->{
			for(Observer o:os)
				if(o!=null)
					packet.addObserver(o);
		});
		final String addr = textFieldAddress.getText();
		prologix.send(addr, packet);
	}

	@Override public void get(Commands command, Observer observer) {
		synchronized (this) {try { wait(200); } catch (InterruptedException e) { }}

		switch(command){
		case GET:
		case OUTPUT:
			sendCommand(outPacket, observer, observerOutput);
			break;

		case POWER:
			sendCommand(pwPacket, observer, observerPower);
			break;

		case FREQUENCY:
			sendCommand(frPacket, observer, observerFrequency);
			break;

		default:
		}
	}

	@Override public <T> Future<T> get(Commands command) {

		Task<T> task = new Task<>();
		final Future<T> future = EXECUTOR.submit(task);

		get(command, task.getObserver(command));

		return future;
	}

	@Override public void set(Commands command, Object valueToSend, Observer observer) {
		logger.traceEntry("{}; {}; {}", command, valueToSend, observer);

		ToolsPacket packet;
		switch(command){

		case GET:
		case OUTPUT:
			outPacket.getCommand().setValue(valueToSend);
			packet = outPacket;
			break;

		case POWER:

			String value;
			if(valueToSend instanceof Double)
				value = PowerUnits.DBM.valueOf((double) valueToSend);
			else
				value = valueToSend.toString();

			Platform.runLater(()->{
				textFieldPower.setText(value);
				onActionSetPower();
			});
			return;

		case FREQUENCY:
			packet = frPacket;
			break;

		default:
			logger.debug("unused command - {}", command);
			return;
		}

		if(observer==null)
			sendCommand(packet, (Observer[])null);
		else
			sendCommand(packet, observer, observerOutput);
	}

	public static Optional<Double> getDouble(final byte[] answer) {
		return Optional
				.ofNullable(answer)
				.map(String::new)
				.map(s->s.split("\n"))
				.map(split->Arrays
						.stream(split)
						.filter(s->Character.isDigit(s.charAt(s.length()-1)))
						.reduce((a, b) -> b)	//find last value
						.orElse(null))
				.filter(s->!s.isEmpty())
				.map(Double::parseDouble);
	}

	@Override public void set(Commands command, Object valueToSend) {
		set(command, valueToSend, null);
	}

	//****************************   class Task   *********************************************
	private class Task<T> implements Callable<T> {

		private Object thisObject = this;
		private T value;

		@Override
		public T call() throws Exception {
			synchronized (this) {wait(1000);}
			return value;
		}

		public Observer getObserver(Commands command){
			Observer observer;
			switch(command){
			case POWER:
			case FREQUENCY:
				observer = getDoubleObserver();
				break;
			case OUTPUT:
				observer = getToolsStateObserver();
				break;
			default:
				return null;
			}
			return observer;
		}

		@SuppressWarnings("unchecked")
		private Observer getToolsStateObserver() {
			return (o, arg)->{

				final PacketToSend tp = (PacketToSend) o;

				value = (T) Optional
						.ofNullable(tp.getAnswer())
						.map(ToolsState::valueOf)
						.orElse(null);

				synchronized (thisObject) { thisObject.notifyAll();}
			};
		}

		@SuppressWarnings("unchecked")
		private Observer getDoubleObserver() {
			final Observer observer = new Observer() {
				
				@Override public void update(Observable o, Object arg) {
					o.deleteObserver(this);
					final PacketToSend tp = (PacketToSend) o;
					final byte[] answer = tp.getAnswer();

					value = (T) getDouble(answer).orElse(null);

					synchronized (thisObject) { thisObject.notifyAll();}
				}
			};
			return observer;
		}
	}
}
