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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiControllerAbstract;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.data.MyThreadFactory;
import irt.data.listener.PacketListener;
import irt.data.packet.PacketSuper;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.Payload;
import irt.data.packet.PacketWork.PacketIDs;
import irt.data.packet.control.ActiveModulePacket;
import irt.data.packet.interfaces.Packet;
import irt.tools.fx.ControlPanelIrPcFx;
import irt.tools.fx.interfaces.StopInterface;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;

public class ModuleSelectFxPanel extends JFXPanel implements Runnable, PacketListener, StopInterface{
	private static final long serialVersionUID = -7284252793969855433L;
	private final static Logger logger = LogManager.getLogger();

	private final ScheduledFuture<?> scheduledFuture;
	private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(new MyThreadFactory());

	private Scene scene;
	private final Byte linkAddr;
	private Consumer<PacketSuper> consumer;
	private List<Button> buttons;

	private final ComPortThreadQueue comPortThreadQueue;
	private final  ActiveModulePacket packet;

	public ModuleSelectFxPanel(Byte linkAddr, byte[] bytes, Consumer<PacketSuper> consumer) throws HeadlessException {

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
				final String externalForm = ControlPanelIrPcFx.class.getResource("fx.css").toExternalForm();
				scene.getStylesheets().add(externalForm);
				setScene(scene);
			}catch (Exception e) {
				logger.catching(e);
			}
		});

		comPortThreadQueue = GuiControllerAbstract.getComPortThreadQueue();
		comPortThreadQueue.addPacketListener(this);

		scheduledFuture = service.scheduleAtFixedRate(this, 1, 10, TimeUnit.SECONDS);
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
											final PacketSuper p = new ActiveModulePacket(linkAddr, es.getKey());
											b.setOnAction(e->consumer.accept(p));
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
	public void onPacketRecived(Packet packet) {

		new MyThreadFactory(()->{

			final Optional<Packet> oPacket = Optional.of(packet);
			final Optional<PacketHeader> oPacketHeader = oPacket.map(Packet::getHeader);

			final boolean packetIdMach = oPacketHeader.map(PacketHeader::getPacketId).filter(pId->PacketIDs.CONTROL_ACTIVE_MODULE.match(pId)).isPresent();
			final boolean isResponse = oPacketHeader.map(PacketHeader::getPacketType).filter(pId->pId==PacketImp.PACKET_TYPE_RESPONSE).isPresent();
			final boolean noError = oPacketHeader.map(PacketHeader::getOption).filter(pId->pId==PacketImp.ERROR_NO_ERROR).isPresent();

			if(!(packetIdMach && isResponse && noError))
				return;

			getButtons().forEach(b->b.getStyleClass().remove("activeButton"));
			oPacket.map(Packet::getPayloads).filter(pl->!pl.isEmpty()).map(List::stream).flatMap(Stream::findAny).map(Payload::getByte).map(b->--b).map(buttons::get).ifPresent(b->b.getStyleClass().add("activeButton"));
		});
	}

	public int countButtons(){
		return Optional.ofNullable(buttons).map(List::size).orElse(0);
	}

	@Override
	public void run() {
		comPortThreadQueue.add(packet);
	}

	@Override
	public void stop() {
		comPortThreadQueue.removePacketListener(this);
		Optional.ofNullable(scheduledFuture).filter(ft->!ft.isDone()).ifPresent(ft->ft.cancel(true));
		Optional.of(service).filter(s->!s.isShutdown()).ifPresent(ScheduledExecutorService::shutdownNow);
	}
}
