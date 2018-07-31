package irt.tools.fx.module;

import java.awt.HeadlessException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiControllerAbstract;
import irt.data.MyThreadFactory;
import irt.data.listener.PacketListener;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketSuper;
import irt.data.packet.PacketWork.PacketIDs;
import irt.data.packet.Payload;
import irt.data.packet.control.ActiveModulePacket;
import irt.data.packet.interfaces.Packet;
import irt.tools.fx.AlarmPanelFx;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;

public class ModuleSelectFxPanel extends JFXPanel implements Runnable, PacketListener{
	private static final long serialVersionUID = -7284252793969855433L;
	private final static Logger logger = LogManager.getLogger();

	private ScheduledFuture<?> scheduledFuture;
	private ScheduledExecutorService service;

	private Scene scene;
	private final Byte linkAddr;
	private Consumer<PacketSuper> consumer;
	private List<Button> buttons;

	private final  ActiveModulePacket packet;

	public ModuleSelectFxPanel(Byte linkAddr, byte[] bytes, Consumer<PacketSuper> consumer) throws HeadlessException {
		Thread currentThread = Thread.currentThread();
		currentThread.setName(getClass().getSimpleName() + "-" + currentThread.getId());

		addAncestorListener(new AncestorListener() {

			@Override
			public void ancestorRemoved(AncestorEvent event) {
				stop();
			}

			@Override public void ancestorMoved(AncestorEvent event) { }

			@Override
			public void ancestorAdded(AncestorEvent event) {

				if(Optional.ofNullable(scheduledFuture).filter(s->!s.isDone()).isPresent())
					return;

				if(!Optional.ofNullable(service).filter(s->!s.isShutdown()).isPresent())
					service = Executors.newSingleThreadScheduledExecutor(new MyThreadFactory("ModuleSelectFxPanel.service"));

				GuiControllerAbstract.getComPortThreadQueue().addPacketListener(ModuleSelectFxPanel.this);
				scheduledFuture = service.scheduleAtFixedRate(ModuleSelectFxPanel.this, 1, 10, TimeUnit.SECONDS);
			}
		});
		this.consumer = consumer;
		this.linkAddr = linkAddr;
		packet = new ActiveModulePacket(linkAddr, null);

		Platform.runLater(()->{
			try{

				HBox hBox = new HBox();
				addButtons(hBox, bytes);

				AnchorPane root = new AnchorPane();
				root.getChildren().add(hBox);
				hBox.setStyle("-fx-background-color: BISQUE;");

				AnchorPane.setLeftAnchor(hBox, 0.0);
				AnchorPane.setRightAnchor(hBox, 0.0);
				AnchorPane.setTopAnchor(hBox, 0.0);
				AnchorPane.setBottomAnchor(hBox, 0.0);

				scene = new Scene(root);
				final String externalForm = AlarmPanelFx.class.getResource("fx.css").toExternalForm();
				scene.getStylesheets().add(externalForm);
				setScene(scene);
			}catch (Exception e) {
				logger.catching(e);
			}
		});

	}

	private void addButtons(HBox hBox, byte[] bytes) {

		// find end points
		final int[] array = IntStream.range(0, bytes.length).filter(b->bytes[b]==0).toArray();

		Map<Byte, String> map = new HashMap<>();

		int start = 0;
		for(int end=0; ;){
			map.put(bytes[start], new String(Arrays.copyOfRange(bytes, ++start, array[end])));

			start = array[end] + 1;

			if(++end >= array.length)
				break;
		}

		buttons =  map.entrySet()
								.stream().map(
										es->{
											Button b = new Button();

											b.setMaxWidth(Double.MAX_VALUE);
											b.setMaxHeight(Double.MAX_VALUE);
											b.setFont(new Font(12));

											HBox.setHgrow(b, Priority.ALWAYS);

											b.setText(es.getValue());
											b.setUserData(es.getKey());

											b.setOnAction(e->consumer.accept(new ActiveModulePacket(linkAddr, (Byte) b.getUserData())));
											return b;
										})
								.collect(Collectors.toList());

		hBox.getChildren().addAll(buttons);

		setSize(250, 30);
		final int size = buttons.size();
		if(size>1)
			setLocation(280, 124);
		else{
			setLocation(280, 80);
			stop();
		}
	}

	public List<Button> getButtons() {
		return Optional.ofNullable(buttons).orElse(new ArrayList<>());
	}

	@Override
	public void onPacketReceived(Packet packet) {

		final Optional<Packet> oPacket = Optional.of(packet);
		final Optional<PacketHeader> oPacketHeader = oPacket.map(Packet::getHeader);

		if(!oPacketHeader.map(PacketHeader::getPacketId).filter(PacketIDs.CONTROL_ACTIVE_MODULE::match).isPresent())
			return;

//		logger.error(packet);

		if(!oPacketHeader.map(PacketHeader::getPacketType).filter(pId->pId==PacketImp.PACKET_TYPE_RESPONSE).isPresent())
			return;
		if(!oPacketHeader.map(PacketHeader::getOption).filter(pId->pId==PacketImp.ERROR_NO_ERROR).isPresent()) {
			logger.warn(packet);
			return;
		}

		new MyThreadFactory(()->{

			getButtons().forEach(b->b.getStyleClass().remove("activeButton"));
			oPacket.map(Packet::getPayloads).map(List::stream).flatMap(Stream::findAny).map(Payload::getByte).map(b->--b).filter(b->b<buttons.size()).map(buttons::get).ifPresent(b->b.getStyleClass().add("activeButton"));
		}, "ModuleSelectFxPanel.onPacketReceived()");
	}

	public int countButtons(){
		return Optional.ofNullable(buttons).map(List::size).orElse(0);
	}

	@Override
	public void run() {
		GuiControllerAbstract.getComPortThreadQueue().add(packet);
	}

	private void stop() {

		GuiControllerAbstract.getComPortThreadQueue().removePacketListener(this);
		Optional.ofNullable(scheduledFuture).filter(ft->!ft.isDone()).ifPresent(ft->ft.cancel(true));
		Optional.ofNullable(service).filter(s->!s.isShutdown()).ifPresent(ScheduledExecutorService::shutdownNow);
	}
}
