package irt.fx.control.prologix;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.prologix.PrologixCommands;
import irt.data.prologix.PrologixDeviceType;
import irt.fx.control.prologix.interfaces.Prologix;
import irt.fx.control.serial.port.SerialPortFX;
import irt.packet.ToolsCommandsPacket;
import irt.packet.interfaces.PacketToSend;
import irt.packet.prologix.PrologixAddrPacket;
import irt.packet.prologix.PrologixEoiPacket;
import irt.packet.prologix.PrologixModePacket;
import irt.packet.prologix.PrologixPacket;
import irt.packet.prologix.PrologixReadAfterWritePacket;
import irt.packet.prologix.PrologixReadPacket;
import irt.packet.prologix.PrologixSaveCfgPacket;
import irt.serial.port.controllers.PacketsQueue;
import irt.serial.port.enums.SerialPortStatus;
import irt.serial.port.fx.ComboBoxSerialPortFx;
import irt.services.GlobalPacketsQueues;
import irt.services.listeners.NumericChecker;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

/**
 * public class PrologixFx extends AnchorPane implements Prologix, Observer {}
 */
public class PrologixFx extends AnchorPane implements Prologix, Observer {

	private final Logger logger = LogManager.getLogger();

	public static final String KEY = PrologixFx.class.getSimpleName();

	public PrologixFx() {
		logger.traceEntry();

    	FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/prologix.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
		logger.traceExit();
	}

//	private final ExecutorService executor = Executors.newSingleThreadExecutor(new MyThreadFactory());

	private final PrologixPacket packetMode 			= new PrologixModePacket();
	private final PrologixPacket packetAddr 			= new PrologixAddrPacket();
	private final PrologixPacket packetReadAfterWrite 	= new PrologixReadAfterWritePacket();
	private final PrologixPacket packetEoi 				= new PrologixEoiPacket();
	private final PrologixPacket packetRead 			= new PrologixReadPacket();
	private final PrologixPacket packetSaveCfg 			= new PrologixSaveCfgPacket();

	@FXML private AnchorPane anchorPane;
	@FXML private GridPane gridPane;
	@FXML private HBox hBox;

    @FXML private SerialPortFX serialPortFx;
    @FXML private Label statusLabel;
    @FXML private Label labelOperatingMode;
	@FXML private Label labelAddr;
	@FXML private Label labelSaveCfg;
	@FXML private Label labelReadAfterWrite;
	@FXML private Label labelEoi;

    @FXML private ChoiceBox<PrologixCommands> choiceCommand;
    @FXML private TextField tfValue;
    @FXML private Button btnSend;
    @FXML private Label lblResult;

	private List<Consumer<SerialPortStatus>> statusChangeActions = new ArrayList<>();
	private SerialPortStatus serialPortStatus = SerialPortStatus.NOT_SELECTED;		public SerialPortStatus getSerialPortStatus() { return serialPortStatus; }

	@FXML public void initialize() {
		logger.traceEntry();

		new NumericChecker(labelAddr.textProperty());

		serialPortFx.addObserver(this);
		serialPortFx.initialize(KEY);
		anchorPane.getChildren().remove(gridPane);

		disableButtons(serialPortFx.getSerialPortStatus());
		statusChangeActions.add(sps->disableButtons(sps));

		choiceCommand.setConverter(new StringConverter<PrologixCommands>() {
			
			@Override
			public String toString(PrologixCommands object) {
				return object.name();
			}
			
			@Override
			public PrologixCommands fromString(String string) {
				// TODO Auto-generated method stub
				throw new UnsupportedOperationException("Auto-generated method stub");
			}
		});
		final PrologixCommands[] values = PrologixCommands.values();
		Arrays.sort(values, (a, b)->a.name().compareTo(b.name()));
		choiceCommand.getItems().addAll(values);
	}

	private void disableButtons(SerialPortStatus serialPortStatus) {
		hBox.getChildren().parallelStream().filter(Button.class::isInstance).map(Button.class::cast).forEach(b->b.setDisable(serialPortStatus!=SerialPortStatus.OPEND));
	}

	@FXML public void onGet() {
		logger.traceEntry();

		Optional.ofNullable(serialPortFx.getSerialPort()).filter(sp->sp.isOpened()).orElseThrow(()->new RuntimeException("Serial port is not Opend."));

		packetMode			.addObserver(new AnswerParser(labelOperatingMode));
	   	send(packetMode);
	   	packetAddr			.addObserver(new AnswerParser(labelAddr));
    	send(packetAddr);
    	packetEoi			.addObserver(new AnswerParser(labelEoi));
    	send(packetEoi);
    	packetReadAfterWrite.addObserver(new AnswerParser(labelReadAfterWrite));
    	send(packetReadAfterWrite);
    	packetSaveCfg		.addObserver(new AnswerParser(labelSaveCfg));
    	send(packetSaveCfg);
    }

	@FXML public void onPreset() {
		logger.traceEntry();

		//Do not remember the configuration
		packetSaveCfg.getCommand().setValue(false);
	   	send(packetSaveCfg);

	   	//Set Controller mode
	   	packetMode.getCommand().setValue(PrologixDeviceType.CONTROLLER);
	   	send(packetMode);

	   	setPrologixToListen();
	}

    @FXML
    void onShowGrid(ActionEvent e) {

    	final ObservableList<Node> children = anchorPane.getChildren();
		if(children.contains(gridPane)){

			children.remove(gridPane);
    		((Button)e.getSource()).setText("+");

		}else{

			children.add(gridPane);
			((Button)e.getSource()).setText("-");
		}
    }

    @FXML void onSendCommand() {
    	logger.traceEntry();
    	lblResult.setText(null);

    	final PrologixCommands selectedItem = choiceCommand.getSelectionModel().getSelectedItem();

    	if(selectedItem==null)
    		return;

    	try{
    		selectedItem.setValue(tfValue.getText());
    	}catch (Exception e) {
    		logger.catching(e);
    		lblResult.setText(e.getLocalizedMessage());
    		selectedItem.setValue(null);
    	}


		String text = Optional.ofNullable(selectedItem.getValue()).map(Object::toString).orElse(null);
		Platform.runLater(()->tfValue.setText(text));
		final PrologixPacket prologixPacket = new PrologixPacket(selectedItem);

		if(text == null)
			prologixPacket.addObserver((o, arg)->Platform.runLater(()->lblResult.setText(Optional.ofNullable(((PrologixPacket)o).parseAnswer()).map(Object::toString).orElse(null))));

		else
			Platform.runLater(()->lblResult.setText(text));

		send(prologixPacket);

		Platform.runLater(()->tfValue.setText(null));
    }

	public boolean send(PacketToSend prologixPacket) {

		String prefsName = serialPortFx.getPrefsName();

		logger.trace("prefsName: {}; prologixPacket: {}", prefsName, prologixPacket);

		PacketsQueue queue = GlobalPacketsQueues.get(prefsName);

		return queue.add(prologixPacket, false);
	}

	@Override public void send(String addr, PacketToSend packet) {
		logger.entry(addr, packet);

		List<PacketToSend> ps = new ArrayList<>();

		// If necessary, set the address
		if(!addr.equals(labelAddr.getText())){
			labelAddr.setText(addr);
			packetAddr.getCommand().setValue(addr);
			ps.add(packetAddr);
		}

		ps.add(packet);

		try {
			final ToolsCommandsPacket p = new ToolsCommandsPacket(ps);

			Optional
			.of(packet.getObservers())
			.filter(os->os.length>0)
			.ifPresent(os->{
				ps.add(packetRead);
				p.addObserver((o, arg)->packet.setAnswer(((PacketToSend)o).getAnswer()));
			});

			send(p);

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	@Override public void setPrologixToListen() {
		packetReadAfterWrite.getCommand().setValue(false);
		send(packetReadAfterWrite);
	}

	@Override
	public void update(Observable o, Object arg) {
		logger.entry(arg);

		Optional
		.ofNullable(arg)
		.filter(ComboBoxSerialPortFx.class::isInstance)
		.map(sps->((ComboBoxSerialPortFx) sps).getSerialPortStatus())
		.ifPresent(sps->{

			serialPortStatus = sps;

			if(sps == SerialPortStatus.OPEND)
				onPreset();

			statusChangeActions.parallelStream().forEach(a->a.accept(sps));
		});
	}

	public void claseSerialPort() {
		serialPortFx.closePort();
	}

	public void addSerialPortStatusChangeAction(Consumer<SerialPortStatus> consumer) {
		statusChangeActions.add(consumer);
		consumer.accept(serialPortStatus);
	}

	private class AnswerParser implements Observer{

		private Label label;

		public AnswerParser(Label label) {
			this.label = label;
		}

		@Override
		public void update(Observable o, Object arg) {
			logger.entry( o, arg);

			final String text = Optional
					.ofNullable(((PrologixPacket) o).parseAnswer())
					.map(Object::toString)
					.orElse("No Answer");

			Platform.runLater(() -> {
				if(!text.equals(label.getText()))
					label.setText(text);
				});
			o.deleteObservers();
		}
		
	}
}
