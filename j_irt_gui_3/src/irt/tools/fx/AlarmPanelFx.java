package irt.tools.fx;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiControllerAbstract;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.controller.serial_port.SerialPortInterface;
import irt.controller.translation.Translation;
import irt.data.MyThreadFactory;
import irt.data.listener.PacketListener;
import irt.data.packet.AlarmDescriptionPacket;
import irt.data.packet.AlarmStatusPacket;
import irt.data.packet.AlarmStatusPacket.AlarmSeverities;
import irt.data.packet.AlarmsIDsPacket;
import irt.data.packet.AlarmsSummaryPacket;
import irt.data.packet.LinkHeader;
import irt.data.packet.PacketAbstract;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.Packets;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.LinkedPacket;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.interfaces.PacketWork;
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
	private static final long DUMP_DELAY = 20;

	private ScheduledFuture<?> scheduledFuture;
	private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(new MyThreadFactory());

	private final AlarmsIDsPacket packetAlarmIDs;
	private final AlarmsSummaryPacket packetSummary;

	private byte unitAddress = CONVERTER;
												public byte getUnitAddress() {
													return unitAddress;
												}

												public void setUnitAddress(byte unitAddress) {
													this.unitAddress = unitAddress;
													packetSummary.setAddr(unitAddress);
													packetAlarmIDs.setAddr(unitAddress);
													clear();
												}

	private GridPane 	gridPane;

	public AlarmPanelFx() {

		packetAlarmIDs = (AlarmsIDsPacket) Packets.ALARM_ID.getPacketWork();
		packetSummary = (AlarmsSummaryPacket) Packets.ALARMS_SUMMARY_STATUS.getPacketWork();//This packet is for DumpController

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
		GuiControllerAbstract.getComPortThreadQueue().addPacketListener(this);
		gridPane.getStyleClass().add("alarms");

		scheduledFuture = service.scheduleAtFixedRate(this, 0, 3, TimeUnit.SECONDS);
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
	private long index ;
	private boolean statusChamge;
	private Object summaryAlarm;
	private boolean run;
	private SerialPortInterface serialPort;

	private int delay;
	@Override
	public void run() {

		final SerialPortInterface serialPort = ComPortThreadQueue.getSerialPort();
		if(this.serialPort==null)
			this.serialPort = serialPort;

		//Stop this tread if serial port is changed
		if(Optional.ofNullable(this.serialPort).filter(sp->sp==serialPort).map(sp->!sp.isOpened()).orElse(true)){
			shutdownNow();
			return;
		}

		try{

			GuiControllerAbstract.getComPortThreadQueue().add(packetSummary);// used by DumpController.onPacketReceived

			if(availableAlarms==null || availableAlarms.length==0){

				GuiControllerAbstract.getComPortThreadQueue().add(packetAlarmIDs);
				return;
			}

			final ComPortThreadQueue queue = GuiControllerAbstract.getComPortThreadQueue();
			final int size = queue.size();

			if(delay>0){
				delay--;
				return;
			}

			if(size>ComPortThreadQueue.QUEUE_SIZE_TO_DELAY && delay<=0)
				delay = ComPortThreadQueue.DELAY_TIMES;
			else if(size==0)
				delay = 0;

			if(!(run || statusChamge) && --index>0)
				return;

			index = DUMP_DELAY;
			statusChamge = false;

			IntStream
			.range(0, availableAlarms.length)
			.mapToObj(index->new AlarmStatusPacket( unitAddress, availableAlarms[index]))
			.forEach(ap->GuiControllerAbstract.getComPortThreadQueue().add(ap));

			gridPane
			.getChildren()
			.parallelStream()
			.filter(Label.class::isInstance)
			.map(Label.class::cast)
			.filter(l->l.getStyleClass().contains(DESCRIPTION))
			.filter(l->l.getText().isEmpty())
			.map(Label::getUserData)
			.map(Short.class::cast)
			.map(alarmCode->new AlarmDescriptionPacket(unitAddress, alarmCode))
			.forEach(ad->GuiControllerAbstract.getComPortThreadQueue().add(ad));

		}catch (Exception e) {
			logger.catching(e);
		}
	}

	@Override
	public void onPacketRecived(final Packet packet) {

		final Optional<PacketHeader> alarmGroupId = filterByAddressAndGroup(packet);

		if(!alarmGroupId.isPresent())
			return;

		// has response
		final Optional<PacketHeader> hasResponse = alarmGroupId.filter(h->h.getPacketType()==PacketImp.PACKET_TYPE_RESPONSE);

		if(!hasResponse.isPresent()){
			logger.warn("Unit is not connected {}", packet);
			return;
		}

		//no error
		final Optional<PacketHeader> noError = hasResponse
												.filter(h->h.getOption()==PacketImp.ERROR_NO_ERROR);

		if(!noError.isPresent()){
			logger.warn("Packet has error {}", packet);
			return;
		}

		//Summary alarm
		if(noError
		.filter(h->h.getPacketId()==PacketWork.PACKET_ID_ALARMS_SUMMARY).isPresent()){

			final Optional<? extends PacketAbstract> p = Packets.cast(packet);
			p.ifPresent(pa->{
				Object sa = pa.getValue();
				if(!sa.equals(summaryAlarm)){
					statusChamge = true;
					summaryAlarm = sa;
				};
			});
			return;
		}

		//Available alarms
		final Optional<short[]> alarms = noError.filter(h->h.getPacketId()==PacketWork.PACKET_ID_ALARMS_IDs)
											.map(h->packet.getPayloads())
											.filter(pls->!pls.isEmpty())
											.map(pls->pls.get(0).getArrayShort());

		//Initialize available alarms
		if(alarms.isPresent()){

			availableAlarms = alarms.get();
			createLabels();

			return;
		}

		//set alarm status
		noError
		.map(h->packet.getPayloads())
		.filter(pls->!pls.isEmpty())
		.map(pls->pls.stream())
		.map(stream->stream.collect(Collectors.partitioningBy(pl->pl.getParameterHeader().getCode()==PacketImp.ALARM_STATUS)))
		.ifPresent(map->{

								//Alarm Status
								map.get(true)
								.parallelStream()
								.map(Payload::getBuffer)
								.forEach(bs->setAlarmStatus(bs));

								//Alarm description
								map.get(false)
								.parallelStream()
								.filter(pl->pl.getParameterHeader().getCode()==PacketImp.ALARM_DESCRIPTION)
								.map(Payload::getBuffer)
								.forEach(bs->setAlarmDescription(bs));
		});
	}

	private Optional<PacketHeader> filterByAddressAndGroup(final Packet packet) {
		final Optional<Packet> o = Optional.ofNullable(packet);

		if(!o.isPresent())
			return Optional.empty();

		final byte addr = o.filter(LinkedPacket.class::isInstance).map(LinkedPacket.class::cast).map(LinkedPacket::getLinkHeader).map(LinkHeader::getAddr).orElse((byte) 0);

		if(addr!=unitAddress)
			return Optional.empty();

		final Optional<PacketHeader> sameGroupId = o.map(p->p.getHeader())
												.filter(h->h.getGroupId()==PacketImp.GROUP_ID_ALARM);
		return sameGroupId;
	}

	private void createLabels() {

		final ObservableList<RowConstraints> rowConstraints = gridPane.getRowConstraints();

		IntStream.range(0, availableAlarms.length).forEach(row->{

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
		Optional
		.ofNullable(buffer)
		.map(AlarmDescription::new)
		.ifPresent(ad->{

			final String textToSet = Translation.getValue(String.class, ad.alarmDescription, ad.alarmDescription);

			gridPane
			.getChildren()
			.parallelStream()
			.filter(Label.class::isInstance)
			.map(Label.class::cast)
			.filter(n->n.getUserData().equals(ad.alarmCode))
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

			final String severityName = as.alarmSeverities.name();
			final String textToSet = Translation.getValue(String.class, severityName, as.alarmSeverities.toString());

			gridPane
			.getChildren()
			.parallelStream()
			.filter(Label.class::isInstance)
			.map(Label.class::cast)
			.filter(n->n.getUserData().equals(as.alarmCode))
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
		run = true;
	}

	public void stop(){

		run = false;
	}

	public void shutdownNow() {

		GuiControllerAbstract.getComPortThreadQueue().removePacketListener(this);

		if(scheduledFuture!=null && !scheduledFuture.isCancelled())
			scheduledFuture.cancel(true);

		service.shutdownNow();
	}

	public class AlarmDescription {

		public final short alarmCode;
		public final String alarmDescription;

		public AlarmDescription(byte[] bytes) {

			final ByteBuffer buffer = ByteBuffer.wrap(bytes);
			alarmCode =buffer.getShort();
			final int status = buffer.getInt(2)&7;
			logger.trace("\n{}\n{}\n{}", alarmCode, status, bytes);
			alarmDescription = new String(bytes).trim();

			logger.trace("\nbytes={}\nalarmCode={}\nalarmDescription={}", bytes, alarmCode, alarmDescription);
		}

	}

	public static class AlarmStatus {

		private final short alarmCode;
		private final AlarmSeverities alarmSeverities;

		public AlarmStatus(byte[] bytes) {
			logger.trace("{}", bytes);

			final ByteBuffer buffer = ByteBuffer.wrap(bytes);
			alarmCode =buffer.getShort();

			if(bytes.length<6){
				alarmSeverities = null;
				return;
			}

			final int status = buffer.getInt(2)&7;
			alarmSeverities = AlarmStatusPacket.AlarmSeverities.values()[status];
		}


		@Override
		public int hashCode() {
			final int prime = 31;
			int result  = prime + alarmCode;
			return prime * result + ((alarmSeverities == null) ? 0 : alarmSeverities.hashCode());
		}


		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AlarmStatus other = (AlarmStatus) obj;
			if (alarmCode != other.alarmCode)
				return false;
			if (alarmSeverities != other.alarmSeverities)
				return false;
			return true;
		}


		@Override
		public String toString() {
			return "AlarmStatus [alarmCode=" + alarmCode + ", alarmSeverities=" + alarmSeverities + "]";
		}
	}
}
