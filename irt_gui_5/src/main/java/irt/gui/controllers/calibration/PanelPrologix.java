package irt.gui.controllers.calibration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.controllers.calibration.tools.Prologix;
import irt.gui.controllers.calibration.tools.prologix.enums.PrologixDeviceType;
import irt.gui.data.MyThreadFactory;
import irt.gui.data.packet.interfaces.PacketToSend;
import irt.gui.data.packet.observable.calibration.ToolsComandsPacket;
import irt.gui.data.packet.observable.calibration.prologix.PAddrPacket;
import irt.gui.data.packet.observable.calibration.prologix.PModePacket;
import irt.gui.data.packet.observable.calibration.prologix.PReadAfterWritePacket;
import irt.gui.data.packet.observable.calibration.prologix.PReadPacket;
import irt.gui.data.packet.observable.calibration.prologix.PSaveCfgPacket;
import irt.gui.data.packet.observable.calibration.prologix.PrologixPacket;
import irt.gui.data.packet.observable.calibration.prologix.ЗEoiPacket;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class PanelPrologix implements Prologix {
	private final Logger logger = LogManager.getLogger();

	private final ExecutorService executor = Executors.newSingleThreadExecutor(new MyThreadFactory());

	private final PrologixPacket packetMode = new PModePacket();
	private final PrologixPacket packetAddr = new PAddrPacket();
	private final PrologixPacket packetSaveCfg = new PSaveCfgPacket();
	private final PrologixPacket packetReadAfterWrite = new PReadAfterWritePacket();
	private final PrologixPacket packetEoi = new ЗEoiPacket();
	private final PrologixPacket packetRead = new PReadPacket();

	@FXML  private Label labelOperatingMode;
	@FXML  private Label labelAddr;
	@FXML  private Label labelSaveCfg;
	@FXML  private Label labelReadAfterWrite;
	@FXML  private Label labelEoi;

	@FXML public void initialize() {

	   	//Get Prologix MODE
		packetMode.addObserver((o, arg) ->executor.execute(()->{

			final String text = Optional
									.ofNullable(((PrologixPacket) o).getAnswer())
									.map(PrologixDeviceType::valueOf)
									.map(Objects::toString)
									.orElse("No Answer");

			Platform.runLater(() -> {
					if(!text.equals(labelOperatingMode.getText()))
						labelOperatingMode.setText(text);
				});
		}));

		//Get Address
		packetAddr.addObserver((o, arg) -> executor.execute(() -> {
			final String addr = Optional
									.ofNullable(((PrologixPacket) o).getAnswer())
									.map(String::new)
									.map(String::trim)
									.orElse("No Answer");
			Platform.runLater(() -> {
				if(!addr.equals(labelAddr.getText()))
					labelAddr.setText(addr);
			});
		}));

		//Get save config
		packetSaveCfg.addObserver((o, arg) -> executor.execute(() -> {

			final String text = booleanToString(o);
			Platform.runLater(() -> {
				if(!text.equals(labelSaveCfg.getText()))
					labelSaveCfg.setText(text);
			});
		}));

		//Get read write
		packetReadAfterWrite.addObserver((o, arg) -> executor.execute(() -> {

			final String text = booleanToString(o);
			Platform.runLater(() -> {
				if(!text.equals(labelReadAfterWrite.getText()))
					labelReadAfterWrite.setText(text);
			});
		}));

		packetEoi.addObserver((o, arg)-> executor.execute(() -> {

			final String text = booleanToString(o);
			Platform.runLater(() -> {
				if(!text.equals(labelEoi.getText()))
					labelEoi.setText(text);
			});
		}));
//
//		packetRead.addObserver((o,arg)->{
//			LogManager.getLogger().error(o);
//		});
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

	@FXML private void onActionGet() {
		//Do not remember the configuration
		packetSaveCfg.getCommand().setValue(false);
	   	send(packetSaveCfg);

	   	send(packetMode);
    	send(packetAddr);
    	send(packetEoi);
    	send(packetReadAfterWrite);
    	send(packetSaveCfg);
    }

	@FXML public void onActionPreset(ActionEvent event) {

		//Do not remember the configuration
		packetSaveCfg.getCommand().setValue(false);
	   	send(packetSaveCfg);

	   	//Set Controller mode
	   	packetMode.getCommand().setValue(PrologixDeviceType.CONTROLLER);
	   	send(packetMode);

	   	listen();
	}

	private synchronized void send(PrologixPacket toolsPacket) {
		PanelTools.getQueue().add(toolsPacket, false);
	}

	@Override synchronized public void send(String addr, PacketToSend packet) {

		List<PacketToSend> ps = new ArrayList<>();

		if(!addr.equals(labelAddr.getText())){
			Platform.runLater(()->labelAddr.setText(addr));
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


			if(addr.equals("19"))
				logger.error("{} : {}", addr, packet);
			PanelTools.getQueue().add(p, false);
		} catch (Exception e) {
			logger.catching(e);
		}
	}

	@Override public void listen() {
		packetReadAfterWrite.getCommand().setValue(false);
		send(packetReadAfterWrite);
	}
}
