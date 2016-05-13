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
import irt.gui.data.MyThreadFactory;
import irt.gui.data.listeners.NumericChecker;
import irt.gui.data.packet.interfaces.PacketToSend;
import irt.gui.data.packet.observable.calibration.GetPacket;
import irt.gui.data.packet.observable.calibration.IdPacket;
import irt.gui.data.packet.observable.calibration.ToolsPacket;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class PanelPowerMeter extends Observable{
	private final Logger logger = LogManager.getLogger();

	private static final Preferences prefs = Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);
	private final ExecutorService executor = Executors.newSingleThreadExecutor(new MyThreadFactory());

	@FXML private TextField textFieldAddress;
	@FXML private Label labelInfo;
	@FXML private Label labelValue;

	private Prologix prologix;

	private final ToolsPacket idPacket = new IdPacket();
	private final ToolsPacket getPacket = new GetPacket();

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
			logger.error(o);
			final PacketToSend tp = (PacketToSend) o;
			tp.deleteObserver(observerGetValue);

			executor.execute(() -> {
				final Double value = Optional
										.ofNullable(tp.getAnswer())
										.map(String::new)
										.map(Double::new)
										.orElse(null);

				notifyObservers(value);

				final String text = Optional
										.ofNullable(value)
										.map(d -> d.toString())
										.orElse("No Answer");

				Platform.runLater(() -> labelValue.setText(text));
			});
		}
	};

	private final Observer observerGetInfo = new Observer() {
		
		@Override
		public void update(Observable o, Object arg) {
			final PacketToSend tp = (PacketToSend) o;
			tp.deleteObserver(observerGetInfo);

			executor.execute(()->{
				final String text = Optional
										.ofNullable(tp.getAnswer())
										.map(String::new)
										.orElse("No Answer");

				Platform.runLater(()->labelInfo.setText(text));
			});
		}
	};
	@FXML private void onActionGetInfo(){
		prologix.send(textFieldAddress.getText(), idPacket, observerGetInfo);
	}

	@FXML public void onActionGetValue(){
		prologix.send(textFieldAddress.getText(), getPacket, observerGetValue);
	}

	public void setPrologix(Prologix prologix) {
		this.prologix = prologix;
	}
}
