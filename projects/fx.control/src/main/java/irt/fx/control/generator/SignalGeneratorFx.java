package irt.fx.control.generator;

import java.io.IOException;
import java.util.Observer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.prefs.Preferences;

import javax.activation.UnsupportedDataTypeException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.IrtGuiProperties;
import irt.data.tools.ToolsFrequency;
import irt.data.tools.enums.FrequencyUnits;
import irt.data.tools.enums.PowerUnits;
import irt.data.tools.enums.SignalGenerators;
import irt.data.tools.enums.ToolsPower;
import irt.data.tools.enums.ToolsState;
import irt.fx.control.prologix.PrologixFx;
import irt.packet.ToolsFrequencyPacket;
import irt.packet.ToolsIdPacket;
import irt.packet.ToolsOutputPacket;
import irt.packet.ToolsPacket;
import irt.packet.ToolsPowerPacket;
import irt.packet.interfaces.PacketToSend;
import irt.serial.port.enums.SerialPortStatus;
import irt.services.listeners.NumericChecker;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

public class SignalGeneratorFx extends AnchorPane{

	private static final String GENERATOR = "generator";

	public static final String SG_ADDR = "sgAddr";

	private Logger logger = LogManager.getLogger();
	private static final Preferences prefs = Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);

    @FXML private GridPane gridPane;
    @FXML private TextField textFieldAddress;
    @FXML private ChoiceBox<SignalGenerators> choiceBoxModelSelect;
    @FXML private Label labelId;
    @FXML private TextField textFieldFrequency;
    @FXML private TextField textFieldPower;
    @FXML private ChoiceBox<ToolsState> choiceBoxOutput;

    private PrologixFx prologix;
	private final ToolsPacket idPacket	= new ToolsIdPacket();
	private final ToolsPacket frPacket	= new ToolsFrequencyPacket();
	private final ToolsPacket pwPacket	= new ToolsPowerPacket();
	private final ToolsPacket outPacket	= new ToolsOutputPacket();
	private EventHandler<ActionEvent> onAction;

	public SignalGeneratorFx() {

    	FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/signal_generator.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

    	final Observer idObserver = (o, arg)->{

    		final byte[] answer = ((PacketToSend)o).getAnswer();
    		Platform.runLater(()->labelId.setText(answer!=null ? new String(answer) : "N/A"));
    	};
		idPacket.addObserver(idObserver);
	}

    @FXML private void initialize() {
		new NumericChecker(textFieldAddress.textProperty());
		final String addr = prefs.get(SG_ADDR, "19");
		textFieldAddress.setText(addr);
		textFieldAddress.focusedProperty().addListener((observable, oldValue, newValue)->{
			if(!newValue)
				prefs.put(SG_ADDR, textFieldAddress.getText());
		});

		choiceBoxOutput.getItems().addAll(ToolsState.values());
		onAction = choiceBoxOutput.getOnAction();

		choiceBoxModelSelect.getItems().addAll(SignalGenerators.values());
		int index = prefs.getInt(GENERATOR, 0);
		choiceBoxModelSelect.getSelectionModel().select(index);
    }

    @FXML void onSelectTool() {
			SingleSelectionModel<SignalGenerators> selectionModel = choiceBoxModelSelect.getSelectionModel();
			SignalGenerators selectedItem = selectionModel.getSelectedItem();
			idPacket.updateCommand(selectedItem);
			frPacket.updateCommand(selectedItem);
			pwPacket.updateCommand(selectedItem);
			outPacket.updateCommand(selectedItem);
			prefs.putInt(GENERATOR, selectionModel.getSelectedIndex());
    }

    @FXML void onGetAll() {
		prologix.send(textFieldAddress.getText(), idPacket);
		onGetFrequency();
		onGetPower();
		onGetRF();
    }

	@FXML void onGetFrequency() {
		logger.traceEntry();
		frPacket.addObserver((o,arg)->Platform.runLater(()->{

			frPacket.deleteObservers();
			final byte[] answer = ((PacketToSend)o).getAnswer();
			setText(textFieldFrequency, answer, ()->FrequencyUnits.toString(answer));

		}));
		prologix.send(textFieldAddress.getText(), frPacket);
	}

    @FXML void onRememberAddr() {
    	prefs.put(SG_ADDR, textFieldAddress.getText());
    }

	private void setText(TextField textField, byte[] answer, Supplier<String> supplier) {

		final ObservableList<String> styleClass = textField.getStyleClass();

		if(answer!=null){
			final String text = supplier.get();
			textField.setText(text);
			styleClass.remove("error");

		}else if(!styleClass.contains("error"))
				styleClass.add("error");
	}

    @FXML void onSetFrequency() {
    	final String text = textFieldFrequency.getText();

    	if(text.isEmpty() || !text.matches(".*\\d+.*")){
    		onGetFrequency();
    		return;
    	}

		try {
			frPacket.getCommand().setValue(new ToolsFrequency(text));
		} catch (UnsupportedDataTypeException e) {
			logger.catching(e);
		}
		frPacket.deleteObservers();
		prologix.send(textFieldAddress.getText(), frPacket);
    }

    @FXML void onGetPower() {
		pwPacket.addObserver((o,arg)->{

			pwPacket.deleteObservers();
			final byte[] answer = ((PacketToSend)o).getAnswer();
			setText(textFieldPower, answer, ()->PowerUnits.toString(answer));

		});
		prologix.send(textFieldAddress.getText(), pwPacket);
    }

    @FXML void onSetPower() {
    	final String text = textFieldPower.getText();

    	if(text.isEmpty() || !text.matches(".*\\d+.*")){
    		onGetPower();
    		return;
    	}

		try {
			pwPacket.getCommand().setValue(new ToolsPower(text));
		} catch (UnsupportedDataTypeException e) {
			logger.catching(e);
		}
		pwPacket.deleteObservers();
		prologix.send(textFieldAddress.getText(), pwPacket);
    }

    @FXML void onGetRF() {
		outPacket.addObserver((o,arg)->{

			final ObservableList<String> styleClass = choiceBoxOutput.getStyleClass();
			outPacket.deleteObservers();
			final byte[] answer = ((PacketToSend)o).getAnswer();

			Platform.runLater(()->{
				choiceBoxOutput.setOnAction(null);

				if(answer!=null){
					final ToolsState valueOf = ToolsState.valueOf(answer);
					choiceBoxOutput.getSelectionModel().select(valueOf);
					styleClass.remove("error");

				}else if(!styleClass.contains("error")){
					choiceBoxOutput.getSelectionModel().select(-1);
					styleClass.add("error");
				}

				choiceBoxOutput.setOnAction(onAction);
			});
		});
		prologix.send(textFieldAddress.getText(), outPacket);
    }

    @FXML void onSetRF(){
		logger.traceEntry();

    	final int selectedIndex = choiceBoxOutput.getSelectionModel().getSelectedIndex();

    	if(selectedIndex<0){
    		onGetRF();
    		return;
    	}

    	try {
			outPacket.getCommand().setValue(choiceBoxOutput.getSelectionModel().getSelectedItem());
			prologix.send(textFieldAddress.getText(), outPacket);
		} catch (UnsupportedDataTypeException e) {
			logger.catching(e);
		}
    }

    public void setPrologix(PrologixFx prologix){
    	this.prologix = prologix;

    	final Consumer<Boolean> consumer = comPrtNotOpend-> gridPane.getChildren().parallelStream().filter(Button.class::isInstance).map(Button.class::cast).forEach(b->b.setDisable(comPrtNotOpend));

    	//Status change action
    	prologix.addStatusChangeAction(consumer);

    	//set current status
    	consumer.accept(prologix.getSerialPortStatus() != SerialPortStatus.OPEND);
    }
}
