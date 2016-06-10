package irt.gui.controllers.calibration;

import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.IrtGuiProperties;
import irt.gui.controllers.calibration.tools.Prologix;
import irt.gui.controllers.calibration.tools.Tool;
import irt.gui.data.MyThreadFactory;
import irt.gui.data.listeners.NumericChecker;
import irt.gui.data.packet.interfaces.PacketToSend;
import irt.gui.data.packet.observable.calibration.GetPowerMeterPacket;
import irt.gui.data.packet.observable.calibration.IdPacket;
import irt.gui.data.packet.observable.calibration.ToolsPacket;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class PanelPowerMeter extends Observable implements Tool{

	private final Logger logger = LogManager.getLogger();

	private static final Preferences prefs = Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);
	private final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(new MyThreadFactory());

	@FXML private TextField textFieldAddress;
	@FXML private Label labelInfo;
	@FXML private Label labelValue;

	private Prologix prologix;

	private final ToolsPacket idPacket = new IdPacket();
	private final ToolsPacket getPacket = new GetPowerMeterPacket();

	public PanelPowerMeter() {
		idPacket.addObserver(observerGetInfo);
	}

	@FXML public void initialize() {
		new NumericChecker(textFieldAddress.textProperty());
		final String addr = prefs.get("powerMeterAddr", "13");
		textFieldAddress.setText(addr);
		textFieldAddress.focusedProperty().addListener((observable, oldValue, newValue)->{
			if(!newValue)
				prefs.put("powerMeterAddr", textFieldAddress.getText());
		});
	}

	private final Observer observerGetValue = new Observer() {

		@Override
		public void update(Observable o, Object arg) {

			final PacketToSend tp = (PacketToSend) o;
			tp.deleteObserver(observerGetValue);

			EXECUTOR.execute(() -> {
				try{
				final Double value = Optional
										.ofNullable(tp.getAnswer())
										.map(String::new)
										.map(String::trim)
										.map(s->s.split("\n"))
										.filter(split->split.length!=0)
										.map(split->split[split.length-1])
										.filter(s->!s.isEmpty())
										.map(Double::parseDouble)
										.orElse(null);

				notifyObservers(value);

				final String text = Optional
										.ofNullable(value)
										.map(d -> d.toString())
										.orElse("No Answer");

				Platform.runLater(() -> labelValue.setText(text));
				}catch(Exception e){
					logger.catching(e);
				}
			});
		}
	};

	private final Observer observerGetInfo = new Observer() {
		
		@Override
		public void update(Observable o, Object arg) {
			final PacketToSend tp = (PacketToSend) o;

			EXECUTOR.execute(()->{
				final String text = Optional
										.ofNullable(tp.getAnswer())
										.map(String::new)
										.orElse("No Answer");

				Platform.runLater(()->labelInfo.setText(text));
			});
		}
	};
	@FXML private void onActionGetInfo(){
		prologix.send(textFieldAddress.getText(), idPacket);
	}

	@FXML public void onActionGetValue(){
		
		getPacket.addObserver(observerGetValue);
		prologix.send(textFieldAddress.getText(), getPacket);
	}

	public void setPrologix(Prologix prologix) {
		this.prologix = prologix;
	}

	private void sendCommand(final ToolsPacket packet, Observer... observers) {
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

	@Override
	public void get(Commands command, Observer observer) {

		switch(command){
		case GET:
		case POWER:
			sendCommand(getPacket, observer, observerGetValue);
			break;

		default:
		}
	}

	@Override
	public void set(Commands command, Object valueToSend, Observer observer) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}
}
