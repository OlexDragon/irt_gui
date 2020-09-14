package irt.tools.fx;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import irt.controller.GuiControllerAbstract;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.data.ThreadWorker;
import irt.data.StringData;
import irt.data.listener.PacketListener;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketWork;
import irt.data.packet.PacketIDs;
import irt.data.packet.Payload;
import irt.data.packet.denice_debag.DeviceDebugHelpPacket;
import irt.data.packet.interfaces.Packet;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;

public class HelpPaneFx extends AnchorPane implements PacketListener, JavaFxPanel, Runnable{

	private TextArea textArea;

	private HelpPaneFx() {
		Thread currentThread = Thread.currentThread();
		currentThread.setName(getClass().getSimpleName() + "-" + currentThread.getId());

		textArea = new TextArea();
		getChildren().add(textArea);
		AnchorPane.setTopAnchor(textArea, 0.0);
		AnchorPane.setBottomAnchor(textArea, 0.0);
		AnchorPane.setLeftAnchor(textArea, 0.0);
		AnchorPane.setRightAnchor(textArea, 0.0);
	}

	private ScheduledFuture<?> scheduledFuture;
	private ScheduledExecutorService service;
	private byte linkAddr;

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
		scheduledFuture = service.scheduleAtFixedRate(this, 0, 5, TimeUnit.SECONDS);
	}

	@Override
	public void stop() {

		GuiControllerAbstract.getComPortThreadQueue().removePacketListener(this);
		Optional.ofNullable(scheduledFuture).filter(s->!s.isDone()).ifPresent(s->s.cancel(true));
		Optional.ofNullable(service).filter(s->!s.isShutdown()).ifPresent(ScheduledExecutorService::shutdownNow);
	}

	@Override
	public void setUnitAddress(byte unitAddress) {
		linkAddr = unitAddress;
		start();
	}

	@Override
	public void run() {

		final ComPortThreadQueue queue = GuiControllerAbstract.getComPortThreadQueue();
		PacketWork packetWork = new DeviceDebugHelpPacket(linkAddr);
		queue.add(packetWork);
//		LogManager.getLogger().error(packetWork);
	}

	@Override
	public void onPacketReceived(Packet packet) {
		Optional<Packet> oPacket = Optional.of(packet);
		Optional<PacketHeader> oHeader = oPacket.map(Packet::getHeader);

		if(!oHeader.map(PacketHeader::getPacketId).filter(PacketIDs.DEVICE_DEBUG_HELP::match).isPresent())
			return;

		if(oHeader.map(PacketHeader::getPacketType).filter(pt->pt!=PacketImp.PACKET_TYPE_RESPONSE).isPresent())
			return;

		packet.getPayloads().stream().findAny().map(Payload::getStringData).map(StringData::toString).ifPresent(text->textArea.setText(text));
		stop();
	}

	//	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	//	^																						^
	//	^ 										STATIC 											^
	//	^																						^
	//	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

	private static JavaFxFrame helpFrame;
	private static HelpPaneFx root;

	public static JavaFxFrame getHelpFrame(byte linkAddr) {
		JavaFxFrame frame = Optional.ofNullable(helpFrame).orElseGet(
				()->{
					root = new HelpPaneFx();
					helpFrame = new JavaFxFrame(root, null);
					helpFrame.setSize(300, 500);
					return helpFrame;
				});
		root.setUnitAddress(linkAddr);
		return frame;
	}
}
