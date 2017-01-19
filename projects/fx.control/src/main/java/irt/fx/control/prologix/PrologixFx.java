package irt.fx.control.prologix;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.prologix.PrologixDeviceType;
import irt.fx.control.prologix.interfaces.Prologix;
import irt.fx.control.serial.port.SerialPortFX;
import irt.packet.ToolsComandsPacket;
import irt.packet.interfaces.PacketToSend;
import irt.packet.prologix.PrologixAddrPacket;
import irt.packet.prologix.PrologixEoiPacket;
import irt.packet.prologix.PrologixModePacket;
import irt.packet.prologix.PrologixPacket;
import irt.packet.prologix.PrologixReadAfterWritePacket;
import irt.packet.prologix.PrologixReadPacket;
import irt.packet.prologix.PrologixSaveCfgPacket;
import irt.serial.port.enums.SerialPortStatus;
import irt.services.MyThreadFactory;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class PrologixFx extends AnchorPane implements Prologix, Observer {

	public PrologixFx() {

    	FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/prologix.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	}

	private final Logger logger = LogManager.getLogger();

	private final ExecutorService executor = Executors.newSingleThreadExecutor(new MyThreadFactory());

	private final PrologixPacket packetMode = new PrologixModePacket();
	private final PrologixPacket packetAddr = new PrologixAddrPacket();
	private final PrologixPacket packetSaveCfg = new PrologixSaveCfgPacket();
	private final PrologixPacket packetReadAfterWrite = new PrologixReadAfterWritePacket();
	private final PrologixPacket packetEoi = new PrologixEoiPacket();
	private final PrologixPacket packetRead = new PrologixReadPacket();

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

	private final Observer oMode = (o, arg) -> {
		executor.execute(() -> {

			final String text = Optional
					.ofNullable(((PrologixPacket) o).getAnswer())
					.map(PrologixDeviceType::valueOf)
					.map(Objects::toString)
					.orElse("No Answer");

			Platform.runLater(() -> {
				setText(labelOperatingMode, text);
			});
		});
		o.deleteObservers();
	};
	private final Observer oAddr = (o, arg) -> {
		executor.execute(() -> {
			final String addr = Optional
					.ofNullable(((PrologixPacket) o).getAnswer())
					.map(String::new)
					.map(String::trim)
					.orElse("No Answer");
			Platform.runLater(() -> setText(labelAddr, addr));
		});
		o.deleteObservers();
	};
	private final Observer oSaveCfg = (o, arg) -> {
		executor.execute(() -> {

			final String text = booleanToString(o);
			Platform.runLater(() -> setText(labelSaveCfg, text));
		});
		o.deleteObservers();
	};
	private final Observer oReadAfterWrite = (o, arg) -> {
		executor.execute(() -> {

			final String text = booleanToString(o);
			Platform.runLater(() -> setText(labelReadAfterWrite, text));
		});
		o.deleteObservers();
	};
	private final Observer oEoi = (o, arg) -> {
		executor.execute(() -> {

			final String text = booleanToString(o);
			Platform.runLater(() -> setText(labelEoi, text));
		});
		o.deleteObservers();
	};

	private List<Consumer<Boolean>> statusChangeActions = new ArrayList<>();
	private SerialPortStatus serialPortStatus = SerialPortStatus.NOT_SELECTED;		public SerialPortStatus getSerialPortStatus() { return serialPortStatus; }

	@FXML public void initialize() {

		serialPortFx.addObserver(this);
		serialPortFx.initialize(PrologixFx.class.getSimpleName());
		anchorPane.getChildren().remove(gridPane);

		disableButtons(serialPortFx.getSerialPortStatus()!=SerialPortStatus.OPEND);
		statusChangeActions.add(comPrtNotOpend->disableButtons(comPrtNotOpend));
	}

	private void disableButtons(Boolean comPrtNotOpend) {
		hBox.getChildren().parallelStream().filter(Button.class::isInstance).map(Button.class::cast).forEach(b->b.setDisable(comPrtNotOpend));
	}

	private void setText(Label label, final String text) {
		logger.entry(text);

		if(!text.equals(label.getText()))
			label.setText(text);
	}

	private String booleanToString(Observable o) {
		return Optional
									.ofNullable(((PrologixPacket) o).getAnswer())
									.map(String::new)
									.map(String::trim)
									.map(s->s.equals("1"))
									.map(Objects::toString)
									.orElse("No Answer");
	}

	@FXML public void onGet() {
		logger.traceEntry();

		packetMode.addObserver(oMode);
	   	send(packetMode);
	   	packetAddr.addObserver(oAddr);
    	send(packetAddr);
    	packetEoi.addObserver(oEoi);
    	send(packetEoi);
    	packetReadAfterWrite.addObserver(oReadAfterWrite);
    	send(packetReadAfterWrite);
    	packetSaveCfg.addObserver(oSaveCfg);
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

	public void send(PacketToSend prologixPacket) {
		logger.entry(prologixPacket);

		serialPortFx.queue.add(prologixPacket, false);
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
			final ToolsComandsPacket p = new ToolsComandsPacket(ps);

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
		logger.entry(o, arg);
		Optional
		.ofNullable(arg)
		.filter(SerialPortStatus.class::isInstance)
		.map(sps->(SerialPortStatus) sps)
		.ifPresent(sps->{

			serialPortStatus = sps;

			boolean opend = sps == SerialPortStatus.OPEND;

			if(opend)
				onPreset();

			statusChangeActions.parallelStream().forEach(a->a.accept(!opend));
		});
	}

	public void claseSerialPort() {
		serialPortFx.closePort();
	}

	public void addStatusChangeAction(Consumer<Boolean> consumer) {
		statusChangeActions.add(consumer);
	}
}
