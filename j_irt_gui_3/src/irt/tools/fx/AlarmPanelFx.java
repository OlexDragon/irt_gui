package irt.tools.fx;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiControllerAbstract;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.controller.serial_port.SerialPortInterface;
import irt.controller.translation.Translation;
import irt.data.ThreadWorker;
import irt.data.listener.PacketListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketGroupIDs;
import irt.data.packet.PacketWork.AlarmsPacketIds;
import irt.data.packet.PacketIDs;
import irt.data.packet.Packets;
import irt.data.packet.Payload;
import irt.data.packet.alarm.AlarmDescriptionPacket;
import irt.data.packet.alarm.AlarmStatusPacket;
import irt.data.packet.alarm.AlarmStatusPacket.AlarmSeverities;
import irt.data.packet.alarm.AlarmStatusPacket.AlarmStatus;
import irt.data.packet.alarm.AlarmsIDsPacket;
import irt.data.packet.interfaces.LinkedPacket;
import irt.data.packet.interfaces.Packet;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

public class AlarmPanelFx extends AnchorPane implements Runnable, PacketListener, JavaFxPanel{

	private static final String DESCRIPTION = "description";
	private static final String VALUE = "value";

	private final static Logger logger = LogManager.getLogger();

	public static final byte CONVERTER = MonitorPanelFx.CONVERTER;

	private ScheduledFuture<?> scheduledFuture;
	private ScheduledExecutorService service;

	private final AlarmsIDsPacket packetAlarmIDs;

	private Byte unitAddress = CONVERTER;
												public byte getUnitAddress() {
													return unitAddress;
												}

												public void setUnitAddress(byte unitAddress) {
//													logger.error("unitAddress: {}", unitAddress);
													this.unitAddress = unitAddress;
													packetAlarmIDs.setAddr(unitAddress);
													clear();
												}

	private GridPane 	gridPane;

	public AlarmPanelFx() {

		//Show Stack Trace
//		logger.error(Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).reduce((s1, s2) -> s1 + "\n" + s2).get());

		Thread currentThread = Thread.currentThread();
		currentThread.setName(getClass().getSimpleName() + "-" + currentThread.getId());
//		logger.error("AlarmPanelFx");

		packetAlarmIDs = (AlarmsIDsPacket) Packets.ALARM_ID.getPacketAbstract();

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("GridPanel.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();

        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	}

	@FXML protected void initialize() {
		gridPane = (GridPane) getChildren().get(0);
		gridPane.getStyleClass().add("alarms");

	}

    @FXML  void onMouseClicked(MouseEvent event) { if(event.getClickCount()==3) clear(); }

	private void clear() {
		Optional
		.ofNullable(gridPane)
		.ifPresent(p->{

			Platform.runLater(()->{
				
				gridPane.getChildren().clear();
				gridPane.getRowConstraints().clear();
				availableAlarms = null;
				index = 0;
			});
		});
	}

 	private short[] availableAlarms;
	private int index ;
//	private Object summaryAlarm;
	private SerialPortInterface serialPort;

	private int delay;
	private boolean availableAlarmsSent;
	@Override
	public void run() {
//		logger.error("{}", unitAddress);

		if(unitAddress==null)
			return;

		final SerialPortInterface serialPort = ComPortThreadQueue.getSerialPort();
		if(this.serialPort==null)
			this.serialPort = serialPort;

		//Stop this tread if serial port is changed
		if(Optional
				.ofNullable(this.serialPort)
				.filter(sp->sp!=serialPort)
				.isPresent()){

			shutdownNow();
			return;
		}

		try{

			final ComPortThreadQueue queue = GuiControllerAbstract.getComPortThreadQueue();

			boolean haveToGetAlarmIDs = !Optional.ofNullable(availableAlarms).filter(a->a.length>0).isPresent();
			if(haveToGetAlarmIDs && availableAlarmsSent){
				availableAlarmsSent = false;
				return;
			}

			if(haveToGetAlarmIDs){

				GuiControllerAbstract.getComPortThreadQueue().add(packetAlarmIDs);
				availableAlarmsSent = true;
				return;
			}

			final int size = queue.size();

			if(size>ComPortThreadQueue.QUEUE_SIZE_TO_DELAY && delay<0)
				delay = ComPortThreadQueue.DELAY_TIMES;
			else if(size<=ComPortThreadQueue.QUEUE_SIZE_TO_RESUME)
				delay = 0;

			if(++index>=availableAlarms.length)
				index = 0;


			IntStream
				.range(0, availableAlarms.length)
				.mapToObj(i->new AlarmStatusPacket( unitAddress, AlarmsPacketIds.valueOf(availableAlarms[i], false).orElse(AlarmsPacketIds.STATUS)))
				.forEach(queue::add);

			final Stream<AlarmDescriptionPacket> descriptionPackets = gridPane

					.getChildren()
					.parallelStream()
					.filter(Label.class::isInstance)
					.map(Label.class::cast)
					.filter(l->l.getStyleClass().contains(DESCRIPTION))
					.filter(l->l.getText().isEmpty())
					.map(Label::getUserData)
					.map(Short.class::cast)
					.map(alarmCode->AlarmsPacketIds.valueOf(alarmCode, true))
//					.peek(logger::error)
					.map(packet->new AlarmDescriptionPacket(unitAddress, packet.orElse(AlarmsPacketIds.STATUS)));

			descriptionPackets
			.forEach(queue::add);

		}catch (Exception e) {
			logger.catching(e);
		}
	}

	@Override
	public void onPacketReceived(final Packet packet) {
		new ThreadWorker(()->{

			Optional<Packet> oPacket = Optional.ofNullable(packet);
			final Optional<PacketHeader> alarmGroupId = oPacket.flatMap(this::filterByAddressAndGroup);

			if(!alarmGroupId.isPresent())
				return;

			// has response
			final Optional<PacketHeader> hasResponse = alarmGroupId.filter(h->h.getPacketType()==PacketImp.PACKET_TYPE_RESPONSE);

			if(!hasResponse.isPresent()){
				logger.warn("Unit is not connected {}", packet);
				return;
			}

			//no error
			final Optional<PacketHeader> noError = hasResponse.filter(h->h.getError()==PacketImp.ERROR_NO_ERROR);

			if(!noError.isPresent()){
				logger.warn("Packet has error {}", packet);
				return;
			}

			//Summary alarm
			Optional<Short> oPacketId = noError.map(PacketHeader::getPacketId);
			if(	oPacketId.filter(PacketIDs.ALARMS_SUMMARY::match).isPresent()){

//				final Optional<? extends PacketSuper> p = Packets.cast(packet);
//				p.ifPresent(pa->{
//					Object sa = pa.getValue();
//					if(!sa.equals(summaryAlarm)){
//						summaryAlarm = sa;
//					};
//				});
				return;
			}
			Optional<Payload> oPayload = oPacket
											.map(Packet::getPayloads)
											.map(List::stream)
											.flatMap(Stream::findAny);

			//Available alarms
			if(oPacketId.filter(PacketIDs.ALARMS_ALL_IDs::match).isPresent() && oPayload.filter(pl->pl.getParameterHeader().getCode()==2).isPresent()){

			//Initialize available alarms
				final Optional<short[]> alarms = oPayload
													.map(Payload::getArrayShort);

				availableAlarms = alarms.get();
				logger.debug("All alarm cods: {}", availableAlarms);
				createLabels();

				return;
			}

			//set alarm status
			oPayload
			.ifPresent(pl->{

				byte[] buffer = pl.getBuffer();
				if(pl.getParameterHeader().getCode()==PacketImp.ALARM_STATUS)
									//Alarm Status
									setAlarmStatus(buffer);

				else if(pl.getParameterHeader().getCode()==PacketImp.ALARM_DESCRIPTION)
									//Alarm description
									setAlarmDescription(buffer);

				else
					logger.warn(packet);
			});
		}, "AlarmPanelFx.onPacketReceived()");
	}

	private Optional<PacketHeader> filterByAddressAndGroup(final Packet packet) {
		final Optional<Packet> o = Optional.ofNullable(packet);

		if(!o.isPresent())
			return Optional.empty();

		final byte addr = o.filter(LinkedPacket.class::isInstance).map(LinkedPacket.class::cast).map(LinkedPacket::getLinkHeader).map(LinkHeader::getAddr).orElse((byte) 0);

		if(addr!=unitAddress)
			return Optional.empty();

		final Optional<PacketHeader> sameGroupId = o.map(p->p.getHeader())
												.filter(h->PacketGroupIDs.ALARM.match(h.getGroupId()));
		return sameGroupId;
	}

	private void createLabels() {

		final ObservableList<RowConstraints> rowConstraints = gridPane.getRowConstraints();

		IntStream.range(0, availableAlarms.length)
		.forEach(
				row->{

					final short alarmCode = availableAlarms[row];

					if(rowConstraints.size()<availableAlarms.length){

						final RowConstraints rc = new RowConstraints();
						rc.vgrowProperty().set(Priority.SOMETIMES);
			
						rowConstraints.add(rc);
					}

					//create new labels
					final Label description = new Label();
					description.setUserData(alarmCode);
					description.getStyleClass().add(DESCRIPTION);
					description.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
					description.setAlignment(Pos.CENTER_RIGHT);
					description.setPadding(new Insets(0, 10, 0, 0));

					final Label label = new Label();
					label.setUserData(alarmCode);
					label.getStyleClass().add(VALUE);
					label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
					label.setAlignment(Pos.CENTER);

					GridPane.setMargin(description, new Insets(1, 0, 1, 0));
					GridPane.setMargin(label, new Insets(1, 0, 1, 0));

					Platform.runLater(()->{
						gridPane.add(description, 0, row);
						gridPane.add(label, 1, row);
					});
				});
	}

	private void setAlarmDescription(byte[] buffer) {
//		logger.error("{}", buffer);

		Optional
		.ofNullable(buffer)
		.map(AlarmDescription::new)
		.ifPresent(alarmDescription->{

			final String textToSet = Translation.getValue(String.class, alarmDescription.description, alarmDescription.description);
//			logger.error("{} : {}", alarmDescription, buffer);

			gridPane
			.getChildren()
			.parallelStream()
			.filter(Label.class::isInstance)
			.map(Label.class::cast)
			.filter(n->n.getUserData().equals(alarmDescription.code))
			.filter(n->n.getStyleClass().contains(DESCRIPTION))
			.findAny()
			.ifPresent(label->{

				//set status
				final String text = label.getText();

				if(!text.equals(textToSet))
					Platform.runLater(()->label.setText(textToSet));
			});
		});
	}

	public void setAlarmStatus(byte[] buffer){
		Optional
		.ofNullable(buffer)
		.map(AlarmStatus::new)
		.ifPresent(as->{

			final String severityName = as.getAlarmSeverities().name();
			final String textToSet = Translation.getValue(String.class, severityName, as.getAlarmSeverities().toString());

			gridPane
			.getChildren()
			.parallelStream()
			.filter(Label.class::isInstance)
			.map(Label.class::cast)
			.filter(n->n.getUserData().equals(as.getAlarmCode()))
			.filter(n->n.getStyleClass().contains(VALUE))
			.findAny()
			.ifPresent(label->{

				//set status
				final String text = label.getText();

				if(!text.equals(textToSet))
					Platform.runLater(()->{
						label.setText(textToSet);
						final ObservableList<String> styleClass = label.getStyleClass();
						Arrays.stream(AlarmSeverities.values()).map(AlarmSeverities::name).forEach(clazz->styleClass.remove(clazz));
						styleClass.add(severityName);
					});
			});
		});
	}

	public void start(){
//		logger.error("");

		if(Optional.ofNullable(scheduledFuture).filter(s->!s.isDone()).isPresent())
			return;

		if(!Optional.ofNullable(service).filter(s->!s.isShutdown()).isPresent())
			service = Executors.newSingleThreadScheduledExecutor(new ThreadWorker("AlarmPanelFx"));

		GuiControllerAbstract.getComPortThreadQueue().addPacketListener(this);
		scheduledFuture = service.scheduleAtFixedRate(this, 0, 3, TimeUnit.SECONDS);
	}

	public void stop(){
//		logger.error("");

		GuiControllerAbstract.getComPortThreadQueue().removePacketListener(this);
		Optional.ofNullable(scheduledFuture).filter(s->!s.isDone()).ifPresent(s->s.cancel(true));
		Optional.ofNullable(service).filter(s->!s.isShutdown()).ifPresent(ScheduledExecutorService::shutdownNow);
	}

	@Override
	public void shutdownNow() {
		stop();
	}

	public class AlarmDescription {

		public final short code;
		public final String description;

		public AlarmDescription(byte[] bytes) {

			final ByteBuffer buffer = ByteBuffer.wrap(bytes);
			code =buffer.getShort();
//			final int status = buffer.getInt(2)&7;
			description = new String(bytes).trim();
//			logger.error(description);

			logger.trace("\n bytes={}\n Code={}\n Description=\"{}\"", bytes, code, description);
		}

		@Override
		public String toString() {
			return "AlarmDescription [Code=" + code + ", Description=" + description + "]";
		}
	}
}
