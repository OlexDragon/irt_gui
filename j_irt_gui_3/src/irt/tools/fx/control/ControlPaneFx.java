package irt.tools.fx.control;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import irt.controller.GuiControllerAbstract;
import irt.data.ThreadWorker;
import irt.data.listener.PacketListener;
import irt.data.packet.PacketIDs;
import irt.data.packet.PacketWork;
import irt.data.packet.Payload;
import irt.data.packet.configuration.LnbStatusPacket;
import irt.data.packet.configuration.LnbSwitchOverPacket;
import irt.data.packet.interfaces.LinkedPacket;
import irt.data.packet.interfaces.Packet;
import irt.tools.fx.JavaFxPanel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.layout.AnchorPane;

public class ControlPaneFx extends AnchorPane implements PacketListener, JavaFxPanel, Runnable {

	private ScheduledFuture<?> scheduledFuture;
	private ScheduledExecutorService service;
	private final byte linkAddr;
	private final List<PacketWork> packets = new ArrayList<>();

	@FXML private ChoiceBox<String> cbMode;
    @FXML private Button btnDefault;
    @FXML private Button btnOverA;
    @FXML private Button btnOverB;

    private final EventHandler<ActionEvent> onAction;

	public ControlPaneFx(byte linkAddr) {

		this.linkAddr = linkAddr;
		onAction = 

	    		e->{
	    			int selectedIndex = cbMode.getSelectionModel().getSelectedIndex();
	    			final LnbStatusPacket lnbStatusPacket = new LnbStatusPacket(linkAddr, (byte)++selectedIndex);
	    			GuiControllerAbstract.getComPortThreadQueue().add(lnbStatusPacket);
	    		};

		try {

			final URL resource = getClass().getResource("Lnb_1x2.fxml");
			FXMLLoader fxmlLoader = new FXMLLoader(resource);
			fxmlLoader.setRoot(this);
			fxmlLoader.setController(this);

            fxmlLoader.load();

		} catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	}

	@FXML public void initialize() {

		final ObservableList<String> modesList = FXCollections.observableArrayList(new String[] { "Automatic", "Manual" });
		cbMode.setItems(modesList);
		cbMode.setOnAction(onAction);
		packets.add(new LnbStatusPacket(linkAddr, null));
	}

    @FXML void onDefault() {
		final LnbSwitchOverPacket lnbStatusPacket = new LnbSwitchOverPacket(linkAddr, (byte)11);
		GuiControllerAbstract.getComPortThreadQueue().add(lnbStatusPacket);
    }

    @FXML void onOverA() {
		final LnbSwitchOverPacket lnbStatusPacket = new LnbSwitchOverPacket(linkAddr, (byte)12);
		GuiControllerAbstract.getComPortThreadQueue().add(lnbStatusPacket);
    }

    @FXML void onOverB() {
		final LnbSwitchOverPacket lnbStatusPacket = new LnbSwitchOverPacket(linkAddr, (byte)13);
		GuiControllerAbstract.getComPortThreadQueue().add(lnbStatusPacket);
    }

    private int count;
	@Override
	public void run() {
		if(count>=packets.size())
			count = 0;

		final PacketWork packetWork = packets.get(count);
		GuiControllerAbstract.getComPortThreadQueue().add(packetWork);

		++count;
	}

	@Override
	public void shutdownNow() {
		stop();
	}

	@Override
	public void start() {

		if(Optional.ofNullable(scheduledFuture).filter(s->!s.isDone()).isPresent())
			return;

		Optional.ofNullable(service)
		.filter(s->!s.isShutdown())
		.orElseGet(()->service = Executors.newSingleThreadScheduledExecutor(new ThreadWorker("AlarmPanelFx")));

		GuiControllerAbstract.getComPortThreadQueue().addPacketListener(this);
		scheduledFuture = service.scheduleAtFixedRate(this, 1, 5, TimeUnit.SECONDS);
	}

	@Override
	public void stop() {

		GuiControllerAbstract.getComPortThreadQueue().removePacketListener(this);
		Optional.ofNullable(scheduledFuture).filter(s->!s.isDone()).ifPresent(s->s.cancel(true));
		Optional.ofNullable(service).filter(s->!s.isShutdown()).ifPresent(ScheduledExecutorService::shutdownNow);
	}

	@Override
	public void setUnitAddress(byte unitAddress) {
	}

	private int status;
	@Override
	public void onPacketReceived(final Packet packet) {
		ThreadWorker.runThread(
				()->{
					
					final short packetId = packet.getHeader().getPacketId();
					if(PacketIDs.MEASUREMENT_ALL.match(packetId)) {
						packet.getPayloads().parallelStream().filter(pl->pl.getParameterHeader().getCode()==4).map(Payload::getBuffer).filter(b->b!=null && b.length==1).map(b->b[0]&0xff).findAny()
						.ifPresent(
								v->{

									if(v==status)
										return;

									switch(v) {
									case 11:
										Platform.runLater(
												()->{
													btnDefault.setDisable(true);
													btnOverA.setDisable(false);
													btnOverB.setDisable(false);
												});
										break;
									case 12:
										Platform.runLater(
												()->{
													btnDefault.setDisable(false);
													btnOverA.setDisable(true);
													btnOverB.setDisable(false);
									});
										break;
									case 13:
										Platform.runLater(
												()->{
													btnDefault.setDisable(false);
													btnOverA.setDisable(false);
													btnOverB.setDisable(true);
												});
										break;
									default:
										Platform.runLater(
												()->{
													btnDefault.setDisable(true);
													btnOverA.setDisable(true);
													btnOverB.setDisable(true);
												});
									}

									status = v;
								});
						return;
					}
					packets.parallelStream().map(LinkedPacket.class::cast).filter(p->p.getHeader().getPacketId()==packetId).findAny()
					.ifPresent(
							p->{
								final Optional<PacketIDs> oPacketIDs = PacketIDs.valueOf(packetId);
								oPacketIDs.flatMap(pid->pid.valueOf(packet)).map(Integer.class::cast).filter(v->v>0).map(v->--v)
								.ifPresent(
										v->Platform.runLater(
												()->{

													final PacketIDs packetID = oPacketIDs.get();
													switch(packetID) {
													case CONFIGURATION_LNB_STATUS:
														final SingleSelectionModel<String> selectionModel = cbMode.getSelectionModel();

														if(selectionModel.getSelectedIndex()==v)
															return;

														cbMode.setOnAction(null);
														selectionModel.select(v);
														cbMode.setOnAction(onAction);
														break;

													default:
													}
												}));
							});

				}, "control panel");
	}

}
