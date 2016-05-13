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

public class PanelSignalGenerator {
	private static final String NO_ANSWER = "No Answer";

	private final Logger logger = LogManager.getLogger();

	private static final Preferences prefs = Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);
	private final ExecutorService executor = Executors.newFixedThreadPool(5, new MyThreadFactory());

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

	private Observer observerGetInfo = new Observer() {
		
		@Override
		public void update(Observable o, Object arg) {
			logger.entry(o);

			final PacketToSend tp = (PacketToSend) o;
			tp.deleteObserver(observerGetInfo);

			executor.execute(()->{
				final String text = Optional
										.ofNullable(tp.getAnswer())
										.map(String::new)
										.orElse(NO_ANSWER);

				Platform.runLater(()->labelId.setText(text));
			});
		}
	};

	private Observer observerFrequency = new Observer() {
		
		@Override
		public void update(Observable o, Object arg) {
			logger.entry(o);

			final PacketToSend tp = (PacketToSend) o;
			tp.deleteObserver(observerFrequency);

			executor.execute(()->{
				final Double d = Optional
										.ofNullable(tp.getAnswer())
										.map(String::new)
										.map(Double::parseDouble)
										.orElse(null);

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
			logger.entry(o);

			final PacketToSend tp = (PacketToSend) o;
			tp.deleteObserver(observerPower);

			executor.execute(()->{
				final Double d = Optional
										.ofNullable(tp.getAnswer())
										.map(String::new)
										.map(Double::parseDouble)
										.orElse(null);

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
			logger.entry(o);

			final PacketToSend tp = (PacketToSend) o;
			tp.deleteObserver(observerOutput);

			executor.execute(()->{
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

			prologix.send(textFieldAddress.getText(), idPacket, observerGetInfo);
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
			prologix.send(textFieldAddress.getText(), frPacket, null);
		}else
			prologix.send(textFieldAddress.getText(), frPacket, observerFrequency);
	}

	@FXML private void onActionGetFrequency() {
		prologix.send(textFieldAddress.getText(), frPacket, observerFrequency);
	}

	@FXML private void onActionSetPower() {
		logger.entry();

		final String text = textFieldPower.getText();
		if(text.matches(".*\\d+.*")){
			pwPacket.getCommand().setValue(new ToolsPower(text));
			prologix.send(textFieldAddress.getText(), pwPacket, null);
		}else
			prologix.send(textFieldAddress.getText(), pwPacket, observerPower);
	}

	@FXML private void onActionGetPower() {
		prologix.send(textFieldAddress.getText(), pwPacket, observerPower);
	}

	@FXML private void onActionSetRF() {
		logger.entry();

		if(comboBoxOutput.getSelectionModel().getSelectedItem()==null)
				prologix.send(textFieldAddress.getText(), outPacket, observerOutput);
		else{
			outPacket.getCommand().setValue(comboBoxOutput.getSelectionModel().getSelectedItem());
			prologix.send(textFieldAddress.getText(), outPacket, null);
		}
	}

	@FXML private void onActionGetRF() {
		prologix.send(textFieldAddress.getText(), outPacket, observerOutput);
	}

	public void setPrologix(PanelPrologix prologix) {
		this.prologix = prologix; 
	}
}
