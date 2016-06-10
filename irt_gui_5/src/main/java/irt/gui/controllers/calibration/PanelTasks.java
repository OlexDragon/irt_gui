package irt.gui.controllers.calibration;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.IrtGuiProperties;
import irt.gui.controllers.calibration.enums.Calibration;
import irt.gui.controllers.calibration.process.CalibrationBuilder;
import irt.gui.controllers.calibration.tools.Tool;
import irt.gui.controllers.calibration.tools.Tool.Commands;
import irt.gui.controllers.interfaces.CalibrationProcess;
import irt.gui.data.listeners.NumericChecker;
import irt.gui.data.listeners.NumericDoubleChecker;
import irt.gui.data.packet.Packet;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.interfaces.PacketToSend;
import irt.gui.data.packet.observable.calibration.ToolsPacket;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;

public class PanelTasks {
	private final Logger logger = LogManager.getLogger();

	private static final Preferences prefs = Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);
	public final static NumberFormat FORMAT = new DecimalFormat("#0.0");

	@FXML private HBox hBox;
    @FXML private CheckBox cbInput;
    @FXML private CheckBox cbOutput;
    @FXML private CheckBox cbGain;
    @FXML private Label labelResponses;

    @FXML private TextField textFieldStepDB;
    @FXML private TextField textFieldMaxSteps;

	private Alert alert;
    private final List<Tool> tools = new ArrayList<>();
	private final Observer observer = new CheckToolsObserver();

	private final List<Calibration> calibrations = new ArrayList<>();

    @FXML public void initialize() {
 		cbInput.selectedProperty().addListener((o, oldV, newV)->setCalibration(cbInput, newV));
    	cbInput.setUserData(Calibration.INPUT_POWER);
    	cbOutput.selectedProperty().addListener((o, oldV, newV)->setCalibration(cbOutput, newV));
    	cbOutput.setUserData(Calibration.OUTPUT_POWER);
    	cbGain.selectedProperty().addListener((o, oldV, newV)->setCalibration(cbGain, newV));
    	cbGain.setUserData(Calibration.GAIN);

    	alert = new Alert(AlertType.ERROR);
		alert.setTitle("Connection Problem");

		String tmp = FORMAT.format(prefs.getDouble("step-db", 1));
		textFieldStepDB.setText(tmp);
		textFieldStepDB.focusedProperty().addListener((o, oldFocous, newFocous)->{
			if(!newFocous) {
				final double parseDouble = Math.abs(Double.parseDouble(textFieldStepDB.getText()));
				prefs.putDouble("step-db", parseDouble);
				textFieldStepDB.setText(FORMAT.format(parseDouble));
			}
		});
		new NumericDoubleChecker(textFieldStepDB.textProperty());

		tmp = Integer.toString(prefs.getInt("max-steps", 100));
		textFieldMaxSteps.setText(tmp);
		textFieldMaxSteps.focusedProperty().addListener((o, oldFocous, newFocous)->{
			if(!newFocous)
				prefs.putDouble("max-steps", Integer.parseInt(textFieldMaxSteps.getText()));
		});
		new NumericChecker(textFieldMaxSteps.textProperty());
   }

    @FXML void onButtonStart(ActionEvent e) {

    	if(calibrations.size()==0){
    		showAlert("Select the calibration mode.");
    		return;
    	}

    	Button b = (Button) e.getSource();

    	final CalibrationProcess cp = (CalibrationProcess)b.getUserData();
		boolean thereAreNoProcesses = cp==null;

    	if(thereAreNoProcesses){
        	final CalibrationProcess calibrationProcess = CalibrationBuilder.buildProcess(calibrations);
			final double stepDb = Math.abs(Double.parseDouble(textFieldStepDB.getText()));
			int steps = Integer.parseInt(textFieldMaxSteps.getText());
        	calibrationProcess.start( tools, stepDb, steps);
        	b.setUserData(calibrationProcess);
    		b.setText("Stop");
    		calibrationProcess.setStopAction(()->stop(b, calibrationProcess));

    	}else{
    		stop(b, cp);
     	}
    }

	private void stop(Button b, final CalibrationProcess cp) {

		Platform.runLater(()->{
			b.setText("Start");
			b.setUserData(null);
		});

		cp.stop();
	}

    @FXML void onCheckBoxAction(ActionEvent event) {
    	final CheckBox cb = (CheckBox) event.getSource();
		final Calibration userData = (Calibration)cb.getUserData();
		checkSelections(userData);
 
		//if selected check connections to tools
		if(cb.isSelected())
			Arrays
			.stream(userData.getTools())
			.forEach(ud->{
				tools
				.stream()
				.filter(t->t.getClass().equals(ud))
				.forEach(t->{
					t.get(Commands.GET, observer);
				});
			});
    }

    @FXML void onMenuCopy(ActionEvent event) {
    	final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        final Tooltip tooltip = labelResponses.getTooltip();
		content.putString(tooltip!=null ? tooltip.getText() : labelResponses.getText());
//        content.putHtml("<b>Some</b> text");
        clipboard.setContent(content);
    }

    @FXML void onMenuClear(ActionEvent event) {
    	labelResponses.setText("");
    }

    private void checkSelections(final Calibration userData) {
    	hBox
    	.getChildren()
    	.parallelStream()
    	.filter(CheckBox.class::isInstance)
    	.map(CheckBox.class::cast)
    	.filter(userData::canNotBeSelected)
    	.forEach(c->c.setSelected(false));
	}

    public void setTools(PanelTools tools) {
    	this.tools.add(tools.getPowerMeter());
    	this.tools.add(tools.getSignalGenerator());
    	this.tools.add(tools.getBuc());
	}

    private void setCalibration(CheckBox checkBox, boolean selected){
		if(selected)
			calibrations.add((Calibration) checkBox.getUserData());
		else
			calibrations.remove((Calibration) checkBox.getUserData());
   	}

	private void showAlert(final String alertText) {
		if(!alert.isShowing())
			Platform.runLater(()->{
				if(alert.getOwner()==null)
					alert.initOwner(hBox.getScene().getWindow());
				alert.setHeaderText(alertText);
				alert.show();

				cbGain.setSelected(false);
				cbInput.setSelected(false);
				cbOutput.setSelected(false);
			});
	}

	//**************************** class TaskObservable   ***************************************
	public class CheckToolsObserver implements Observer{

		@Override
		public void update(Observable o, Object arg) {
			logger.entry(o);
			o.deleteObserver(this);

			String text = null;

			if(o instanceof LinkedPacket){
				text = Optional
				.ofNullable(((PacketToSend)o).getAnswer())
				.map(a->(LinkedPacket)Packet.createNewPacket(((PacketToSend)o).getClass(), a, true))
				.map(p->linkedPacketProcess(p))
				.orElseGet(()->{
					final String alertText = "The " + o.getClass().getSimpleName() + " not received an answer.";
					showAlert(alertText);
					return null + "; ";
				});
			}else if(o instanceof ToolsPacket)
				text = toolsPacketProcess((ToolsPacket)o);

			final String textFinal = text;

			Platform.runLater(()->{
				final String t = textFinal + labelResponses.getText();
				labelResponses.setText(t.length()>500 ? t.substring(0, 500) : t);
				labelResponses.setTooltip(new Tooltip(t));
			});
		}

		private String toolsPacketProcess(ToolsPacket packet) {
			
			final String simpleName = packet.getClass().getSimpleName();

			String text = null;
			switch(simpleName){
			case "ToolsOutputPacket":
			case "GetPowerMeterPacket":
				text = Optional
						.ofNullable(packet.getAnswer())
						.map(String::new)
						.map(String::trim)
						.orElseGet(()->{
							final String alertText = "Tools error( " + simpleName + ")";
							showAlert(alertText);
							return null;
						});
				break;
			default:
				logger.error("HAave to add case of '{}'", simpleName);
			}

			return simpleName.replace("Tools", "").replace("Packet", "") + "( " + text + " ); ";
		}

		private String linkedPacketProcess(LinkedPacket packet) {

			final String simpleName = packet.getClass().getSimpleName();

			String text = null;
			switch(simpleName){
			case "AttenuationRangePacket":
				final String alertText = "BUC error( " + simpleName + ")";
				showAlert(alertText);
				text = packet.getClass().getSimpleName().replace("Packet", "") + "; ";
				break;
			case "AttenuationPacket":
				text = packet.getClass().getSimpleName().replace("Packet", "") + "(" + packet.getPayloads().get(0).getShort(0) + "); ";
			}
			return text;
		}

	}
}
