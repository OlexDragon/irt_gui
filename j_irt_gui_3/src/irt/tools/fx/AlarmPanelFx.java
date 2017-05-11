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
import irt.controller.translation.Translation;
import irt.data.MyThreadFactory;
import irt.data.PacketWork;
import irt.data.listener.PacketListener;
import irt.data.packet.AlarmDescriptionPacket;
import irt.data.packet.AlarmStatusPacket;
import irt.data.packet.AlarmStatusPacket.AlarmSeverities;
import irt.data.packet.AlarmsIDsPacket;
import irt.data.packet.Packet;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.Packets;
import irt.data.packet.Payload;
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
	private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(new MyThreadFactory());

	private final AlarmsIDsPacket packetToSend;
	private byte unitAddress = CONVERTER;
												public byte getUnitAddress() {
													return unitAddress;
												}

												public void setUnitAddress(byte unitAddress) {
													this.unitAddress = unitAddress;
												}

	@FXML private GridPane 	gridPane;

	public AlarmPanelFx() {

		packetToSend = (AlarmsIDsPacket) Packets.ALARM_ID.getPacketWork();

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
		GuiControllerAbstract.getComPortThreadQueue().addPacketListener(this);
		gridPane.getStyleClass().add("alarms");
	}

    @FXML
    void onMouseClicked(MouseEvent event) {
    	if(event.getClickCount()==3)
    		gridPane.getChildren().clear();
    }

	private short[] availableAlarms;
	@Override
	public void run() {
		try{

			if(availableAlarms==null || availableAlarms.length==0){
				logger.trace(packetToSend);
				packetToSend.setAddr(unitAddress);
				GuiControllerAbstract.getComPortThreadQueue().add(packetToSend);
				return;
			}

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

		Optional<PacketHeader> o = Optional
										.ofNullable(packet)
										.map(p->p.getHeader())
										.filter(h->h.getGroupId()==PacketImp.GROUP_ID_ALARM)
										.filter(h->h.getPacketType()==PacketImp.PACKET_TYPE_RESPONSE);

		//Check for error
		if(o.filter(h->h.getOption()!=PacketImp.ERROR_NO_ERROR).isPresent()){
			logger.warn("packet has error {}", packet);
			return;
		}

		if(!o.isPresent())
			return;

		//Available alarms
		final Optional<short[]> alarms = o
											.filter(h->h.getPacketId()==PacketWork.PACKET_ID_ALARMS_IDs)
											.map(h->packet.getPayloads())
											.filter(pls->!pls.isEmpty())
											.map(pls->pls.get(0).getArrayShort());

		//Initialize available alarms
		if(alarms.isPresent()){

			availableAlarms=alarms.get();
			createLabels();

			logger.trace("{}", availableAlarms);
			return;
		}


		//set alarm status
		o
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

	private void createLabels() {

		final ObservableList<RowConstraints> rowConstraints = gridPane.getRowConstraints();
		IntStream.range(0, availableAlarms.length).forEach(row->{

			final short alarmCode = availableAlarms[row];

			final RowConstraints rc = new RowConstraints();
			rc.vgrowProperty().set(Priority.SOMETIMES);
			rowConstraints.add(rc);

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
		if(scheduledFuture==null || scheduledFuture.isCancelled())
			scheduledFuture = service.scheduleAtFixedRate(this, 0, 1, TimeUnit.SECONDS);
	}

	public void stop(){
		if(scheduledFuture!=null && !scheduledFuture.isCancelled())
			scheduledFuture.cancel(true);
	}

	public void shutdownNow() {
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

	public class AlarmStatus {

		private final short alarmCode;
		private final AlarmSeverities alarmSeverities;

		public AlarmStatus(byte[] bytes) {

			final ByteBuffer buffer = ByteBuffer.wrap(bytes);
			alarmCode =buffer.getShort();
			final int status = buffer.getInt(2)&7;
			logger.trace("\n{}\n{}\n{}", alarmCode, status, bytes);
			alarmSeverities = AlarmStatusPacket.AlarmSeverities.values()[status];

			logger.trace("\nbytes={}\nalarmCode={}\nalarmSeverities={}", bytes, alarmCode, alarmSeverities);
		}

	}
}
