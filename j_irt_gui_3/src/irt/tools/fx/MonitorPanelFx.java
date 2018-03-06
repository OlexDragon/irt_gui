package irt.tools.fx;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiControllerAbstract;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.controller.serial_port.SerialPortInterface;
import irt.controller.translation.Translation;
import irt.data.MyThreadFactory;
import irt.data.listener.PacketListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.MeasurementPacket;
import irt.data.packet.PacketImp;
import irt.data.packet.Packets;
import irt.data.packet.ParameterHeader;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.LinkedPacket;
import irt.data.packet.interfaces.Packet;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

public class MonitorPanelFx extends AnchorPane implements Runnable, PacketListener, JavaFxPanel{

	private final static Logger logger = LogManager.getLogger();

	public static final byte CONVERTER = 0;

	private static final String UNMUTED = "unmuted";
	private static final String MUTED = "muted";
	private static final String UNLOCKED = "unlocked";
	private static final String LOCKED = "locked";
	private static final String LOCK_ID = "lock_status";
	private static final String MUTE_ID = "mute_status";

	private ScheduledFuture<?> scheduleAtFixedRate;
	private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(new MyThreadFactory());

	private final MeasurementPacket packetToSend = (MeasurementPacket) Packets.MEASUREMENT_ALL.getPacketWork();

												public byte getUnitAddress() {
													return Optional.ofNullable(packetToSend.getLinkHeader()).map(LinkHeader::getAddr).orElse((byte)0);
												}

												public void setUnitAddress(byte unitAddress) {
													packetToSend.setAddr(unitAddress);
//													retransmitPacket.setAddr(unitAddress);
												}

//	private final RetransmitPacket retransmitPacket = new RetransmitPacket();
	private GridPane 	statusPane;
	private GridPane 	gridPane;

	public MonitorPanelFx() {
		logger.traceEntry();

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MonitorPanel.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	}

	@FXML protected void initialize() {
		VBox box = (VBox) getChildren().get(0);
		statusPane = (GridPane) box.getChildren().get(0);
		gridPane = (GridPane) box.getChildren().get(1);
	}

    @FXML
    void onMouseClicked(MouseEvent event) {
    	if(event.getClickCount()==3)
    		gridPane.getChildren().clear();
    }

	private SerialPortInterface serialPort;

//	private int retransmitDelay;
	@Override
	public void run() {

		final SerialPortInterface serialPort = ComPortThreadQueue.getSerialPort();
		if(this.serialPort==null)
			this.serialPort = serialPort;

		if(Optional.ofNullable(this.serialPort).filter(sp->sp==serialPort).map(sp->!sp.isOpened()).orElse(true)){
			shutdownNow();
			return;
		}

		try{
			logger.debug("Packet to send: {}", packetToSend);

			GuiControllerAbstract.getComPortThreadQueue().add(packetToSend);

//			if(retransmitDelay>0)
//				retransmitDelay--;
//			else{
//				retransmitDelay = 200;
//				GuiControllerAbstract.getComPortThreadQueue().add(retransmitPacket);
//			}

		}catch (Exception e) {
			logger.catching(e);
		}
	}

	@Override
	public void onPacketRecived(final Packet packet) {
		logger.trace(packet);

		final Optional<Packet> ofNullable;

		if(packet instanceof LinkedPacket)
			ofNullable = Optional
								.ofNullable((LinkedPacket)packet)
								.filter(p->p.getLinkHeader()==null || p.getLinkHeader().getAddr()==packetToSend.getLinkHeader().getAddr())
								.map(Packet.class::cast);
		else
			ofNullable= Optional.ofNullable(packet);

		final boolean isConverter = packetToSend.getLinkHeader().getAddr() == CONVERTER;

		final Optional<Packet> oResponse = ofNullable
		.filter(p->p.getHeader().getPacketType()==PacketImp.PACKET_TYPE_RESPONSE)
		.filter(p->p.getHeader().getOption()==PacketImp.ERROR_NO_ERROR);

		oResponse
		.filter(p->p.getHeader().getPacketId()==packetToSend.getHeader().getPacketId())
		.filter(p->p.getHeader().getGroupId()==packetToSend.getHeader().getGroupId())
		.map(Packet::getPayloads)
		.map(pls->pls.stream())
		.ifPresent(stream->{
			stream
			.forEach(pl->{
				logger.entry(pl);


				final ParameterHeader 				parameterHeader 	= pl.getParameterHeader();
				final byte 							code 				= parameterHeader.getCode();

				Optional<? extends ParameterHeaderCode> parameterHeaderCode = (isConverter ? ParameterHeaderCodeFCM.valueOf(code) : ParameterHeaderCodeBUC.valueOf(code))
																				.map(phc->checkOverlaps(parameterHeader, phc));

				// Downlink status is deprecated
				if(parameterHeaderCode.filter(phc->phc==ParameterHeaderCodeBUC.DOWNLINK_STATUS).isPresent())
					return;

				logger.trace("code: {}; parameterHeaderCode: {}", code, parameterHeaderCode);

				Platform.runLater(()->{

					//Status bites
					if(parameterHeaderCode.filter(phc->phc==ParameterHeaderCodeFCM.STATUS || phc==ParameterHeaderCodeBUC.STATUS).isPresent()){
						
						List<Label> statusLabels = statusPane
													.getChildren()
													.stream()
													.filter(Label.class::isInstance)
													.map(Label.class::cast)
													.collect(Collectors.toList());

						if(statusLabels.isEmpty())
							createStatusLabels(pl);

						else
							setStatus(statusLabels, pl);

						return;
					}

					//Values (Power, Temperature, Current ...)

					final Optional<Label> l = gridPane
													.getChildren()
													.parallelStream()
													.filter(Label.class::isInstance)
													.filter(label->label.getUserData()!=null)
													.filter(label->label.getUserData().equals(code))
													.map(Label.class::cast)
													.findAny();

					if(l.isPresent())	//set label text

						setText(l.get(), payloadToString(parameterHeaderCode, pl));

					else{	//Create new labels

						final int size = gridPane.getRowConstraints().size();
						gridPane.getRowConstraints().add(new RowConstraints());

						final Label descriptionLabel = createDescriptionLabel(parameterHeaderCode);
						gridPane.add(descriptionLabel, 0, size);

						String payloadToString;
						try{

							payloadToString = payloadToString(parameterHeaderCode, pl);

						}catch(Exception e){
							payloadToString = "error";
							logger.catching(e);
						}

						final Label child = new Label(payloadToString);
						child.getStyleClass().add("value");
						child.setUserData(code);
						child.setTooltip(new Tooltip(descriptionLabel.getText()));

						gridPane.add(child, 1, size);
						GridPane.setVgrow(child, Priority.ALWAYS);
					}
				});
			});
		});

		//Check retransmits number
//		ofNullable
//		.filter(p->p.getHeader().getPacketId()==PacketWork.PACKET_ID_PROTO_RETRANSNIT)
//		.map(Packet::getPayloads)
//		.flatMap(pls->pls.stream().findAny())
//		.map(pl->pl.getByte())
//		.filter(b->b>0)
//		.ifPresent(b->{
//			final RetransmitPacket p = new RetransmitPacket(retransmitPacket.getLinkHeader().getAddr(), (byte) 0);
//			GuiControllerAbstract.getComPortThreadQueue().add(p);
//		});
	}

	private void setStatus(List<Label> statusLabels, Payload pl) {

		final int statusBits = pl.getInt(0);

		//Lock Label
		statusLabels
		.stream().filter(n->n.getId().equals(LOCK_ID))
		.findAny()
		.map(Label.class::cast)
		.ifPresent(lbl->{

			final boolean isLocked = Optional.ofNullable(isLocked(statusBits)).orElse(false);
			final String lockKey = isLocked ? LOCKED : UNLOCKED;
			final String lockText = Translation.getValueWithSuplier(String.class, lockKey, ()->"TRANSLATE: " + lockKey);

			if(setText(lbl, lockText)){
				final ObservableList<String> styleClass = lbl.getStyleClass();
				styleClass.removeAll(LOCKED, UNLOCKED);
				styleClass.add(lockKey);
			}

			final StatusBitsFCM[] parse = StatusBitsFCM.parse(statusBits);
			final String tooltipText = Arrays.toString(parse);
			if(lbl.getTooltip().getText().equals(tooltipText))
				lbl.setTooltip(new Tooltip(tooltipText));
		});

		//Mute Label
		statusLabels
		.stream()
		.filter(n->n.getId().equals(MUTE_ID))
		.findAny()
		.map(Label.class::cast)
		.ifPresent(lbl->{
			
			final boolean isMuted = isMuted(statusBits);
			final String muteKey = isMuted ? MUTED : UNMUTED;
			final String muteText = Translation.getValueWithSuplier(String.class, muteKey, ()->"TRANSLATE: " + muteKey);

			if(setText(lbl, muteText)){
				final ObservableList<String> styleClass = lbl.getStyleClass();
				styleClass.removeAll(MUTED, UNMUTED);
				styleClass.add(muteKey);
			}

			final String tooltipText = parseTooltip(statusBits);
			if(!lbl.getTooltip().getText().equals(tooltipText))
				lbl.setTooltip(new Tooltip(tooltipText));
		});
	}

	private boolean setText(Label label, final String text) {
		if(!label.getText().equals(text)){
			label.setText(text);
			return true;
		}
		return false;
	}

	private void createStatusLabels(Payload pl) {
		logger.entry(pl);
		statusPane.getStyleClass().add("status");
		final int statusBits = pl.getInt(0);
		final String tooltipText = parseTooltip(statusBits);
		final Tooltip tooltip = new Tooltip(tooltipText);

		//create Lock Label
		Optional
		.ofNullable(isLocked(statusBits))
		.map(b->b ? LOCKED : UNLOCKED)
		.ifPresent(lockKey->{
			final String lockText = Translation.getValueWithSuplier(String.class, lockKey, ()->"TRANSLATE: " + lockKey);

			final Label lblLock = new Label(lockText);
			lblLock.setId(LOCK_ID);
			setLabelProperties(lblLock, lockKey);
			statusPane.add(lblLock, 0, 0);

			lblLock.setTooltip(tooltip);
		});

		//create Mute Label
		final String muteKey = isMuted(statusBits) ? MUTED : UNMUTED;
		final String muteText = Translation.getValueWithSuplier(String.class, muteKey, ()->"TRANSLATE: " + muteKey);

		final Label lblMute =  new Label(muteText);
		lblMute.setId(MUTE_ID);
		setLabelProperties(lblMute, muteKey);
		statusPane.add(lblMute, 1, 0);

		lblMute.setTooltip(tooltip);
	}

	private void setLabelProperties(final Label lblLock, String lockKey) {
		lblLock.getStyleClass().add(lockKey);
		lblLock.setPadding(new Insets(0, 5, 0, 5));
		lblLock.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		lblLock.setAlignment(Pos.CENTER);
		GridPane.setHgrow(lblLock, Priority.ALWAYS);
		GridPane.setVgrow(lblLock, Priority.ALWAYS);
	}

	private boolean isMuted(final int statusBits) {

		if(packetToSend.getLinkHeader().getAddr()==CONVERTER)
			return StatusBitsFCM.MUTE.isOn(statusBits) || StatusBitsFCM.MUTE_TTL.isOn(statusBits);

		return StatusBitsBUC.MUTE.isOn(statusBits);
	}

	private Boolean isLocked(final int statusBits) {

		if(packetToSend.getLinkHeader().getAddr()==CONVERTER)
			return StatusBitsFCM.LOCK_SUMMARY.isOn(statusBits);

		return  StatusBitsBUC.PLL_UNKNOWN.isOn(statusBits) ? /*does not have PLL*/null : StatusBitsBUC.LOCKED.isOn(statusBits);
	}

	private String parseTooltip(final int statusBits) {
		final Object[] sb = packetToSend.getLinkHeader().getAddr()==CONVERTER ? StatusBitsFCM.parse(statusBits) : StatusBitsBUC.parse(statusBits);
		return Arrays.toString(sb).replaceAll(",", "\n").replaceAll("[\\[\\]]", "");
	}

	private String payloadToString(final Optional<? extends ParameterHeaderCode> parameterHeaderCode, Payload pl) {
		
		if(!parameterHeaderCode.isPresent())
			return Arrays.toString(pl.getBuffer());

		ParameterHeaderCode c = parameterHeaderCode.get();

		return  c.toString(pl.getBuffer()); 
	}

	private Label createDescriptionLabel(Optional<? extends ParameterHeaderCode> parameterHeaderCode) {

		String text;
		if(parameterHeaderCode.isPresent()) {

			ParameterHeaderCode c = parameterHeaderCode.get();

			text = Translation.getValue(String.class, c.name(), "* " + parameterHeaderCode.get().name());

		} else
			text = "* " + parameterHeaderCode;

		final Label label = new Label(text + ":");
		label.getStyleClass().add("description");
		label.setPadding(new Insets(0, 5, 0, 0));

		GridPane.setHalignment(label, HPos.RIGHT);

		return label;
	}

	private ParameterHeaderCode checkOverlaps(final ParameterHeader parameterHeader, ParameterHeaderCode c) {

		if(c==ParameterHeaderCodeBUC.INPUT_POWER && parameterHeader.getSize()==4)
			return ParameterHeaderCodeBUC.DOWNLINK_STATUS;

		if(c==ParameterHeaderCodeBUC.STATUS && parameterHeader.getSize()==1)
			return ParameterHeaderCodeBUC.DOWNLINK_WAVEGUIDE_SWITCH;

		if(c==ParameterHeaderCodeBUC.LNB1_STATUS && parameterHeader.getSize()>2)
			return ParameterHeaderCodeBUC.REFLECTED_POWER;

		return c;
	}

	public void start(){
		logger.traceEntry();

		if(!service.isShutdown() && (scheduleAtFixedRate==null || scheduleAtFixedRate.isCancelled())){
			GuiControllerAbstract.getComPortThreadQueue().addPacketListener(this);
			scheduleAtFixedRate = service.scheduleAtFixedRate(this, 0, 3, TimeUnit.SECONDS);
		}
	}

	public void stop(){
		logger.traceEntry();

		GuiControllerAbstract.getComPortThreadQueue().removePacketListener(this);

		if(scheduleAtFixedRate!=null && !scheduleAtFixedRate.isCancelled())
			scheduleAtFixedRate.cancel(true);
	}

	private static NumberFormat nFormate1 = new DecimalFormat("#0.0");
//	private static NumberFormat nFormate3 = new DecimalFormat("#0.000");
	public interface ParameterHeaderCode{
		byte getCode();
		int ordinal();
		String name();
		String toString(byte[] bytes);
	}

	public enum ParameterHeaderCodeFCM implements ParameterHeaderCode{
		NONE			((byte) 0, b->Arrays.toString(b)),
		SUMMARY_ALARM	((byte) 1, b->Arrays.toString(b)),
		STATUS			((byte) 2, b->Arrays.toString(b)),
		INPUT_POWER_FCM	((byte) 4, b->bytesToString(b, "dbm")),
		OUTPUT_POWER	((byte) 5, b->bytesToString(b, "dbm")),
		UNIT_TEMPERATURE((byte) 3, b->b!=null && b.length==2 ? nFormate1.format((ByteBuffer.wrap(b).getShort())/10.0) + " C" : Arrays.toString(b)),
		V5_5			((byte) 6, b->b!=null && b.length==2 ? nFormate1.format((ByteBuffer.wrap(b).getShort())/1000.0) + " V" : Arrays.toString(b)),
		V13_2			((byte) 7, b->b!=null && b.length==2 ? nFormate1.format((ByteBuffer.wrap(b).getShort())/1000.0) + " V" : Arrays.toString(b)),
		V13_2_NEG		((byte) 8, b->b!=null && b.length==2 ? nFormate1.format((ByteBuffer.wrap(b).getShort())/1000.0) + " V" : Arrays.toString(b)),
		CURRENT			((byte) 9, b->bytesToString(b, "A")),
		CPU_TEMPERATURE	((byte)10, b->b!=null && b.length==2 ? nFormate1.format((ByteBuffer.wrap(b).getShort())/10.0) + " C" : Arrays.toString(b)),
		INPUT_POWER		((byte)11, b->bytesToString(b, "dbm")),
		ATTENUATION		((byte)20, b->b!=null && b.length==2 ? nFormate1.format((ByteBuffer.wrap(b).getShort())/10.0) + " dB" : Arrays.toString(b)),
		REFERENCE_SOURCE((byte)21, b->b!=null && b.length==1 && b[0]<3 ? ReferenceSource.values()[b[0]].name() : Arrays.toString(b));

		private final Function<byte[], String> function;
		private final byte code;

		public byte getCode() {
			return code;
		}

		private ParameterHeaderCodeFCM(byte code, Function<byte[], String> function){
			this.code = code;
			this.function = function;
		}

		public static Optional<? extends ParameterHeaderCode> valueOf(byte code){
			return Arrays.stream(values()).parallel().filter(v->v.code==code).findAny();
		}

		public String toString(byte[] bytes){
			return function.apply(bytes);
		}

		private static String bytesToString(byte[] bytes, String prefix){
			return MonitorPanelFx.bytesToString(bytes, prefix);
		}
	}

	public enum ParameterHeaderCodeBUC implements ParameterHeaderCode {
		NONE			( b->Arrays.toString(b)),
		INPUT_POWER		( b->bytesToString(b, "dbm")),
		OUTPUT_POWER	( b->bytesToString(b, "dbm")),
		UNIT_TEMPERATURE( b->b!=null && b.length==2 ? nFormate1.format((ByteBuffer.wrap(b).getShort())/10.0) + " C" : Arrays.toString(b)),
		STATUS			( b->Arrays.toString(b)),
		LNB1_STATUS		( b->lnbReady(b)),
		LNB2_STATUS		( b->lnbReady(b)),
		REFLECTED_POWER ( b->bytesToString(b, "dbm")),
		DOWNLINK_WAVEGUIDE_SWITCH( b->switchPosition(b)),
		DOWNLINK_STATUS(b->null);// Downlink status is deprecated

		private final Function<byte[], String> function;
		private final byte code;

		public byte getCode() {
			return code;
		}

		private ParameterHeaderCodeBUC(Function<byte[], String> function){
			this.code = (byte) ordinal();
			this.function = function;
		}

		public static Optional<? extends ParameterHeaderCode> valueOf(byte code){
			logger.entry(code);
			return Arrays.stream(values()).parallel().filter(v->v.code==code).findAny();
		}

		public String toString(byte[] bytes){
			return function.apply(bytes);
		}

		private static String bytesToString(byte[] bytes, String prefix){
			return MonitorPanelFx.bytesToString(bytes, prefix);
		}

		private static String lnbReady(byte[] bytes) {
			return Optional
					.of(bytes)
					.filter(b->b.length==1)
					.map(b->{
						final String string = b[0]==1 ? "ready" : "ready.not";
						return Translation.getValue(String.class, string, string);
					})
					.orElse("N/A");
		}

		private static String switchPosition(byte[] bytes) {
			return Optional
					.of(bytes)
					.filter(b->b.length==1)
					.map(b->b[0])
					.filter(b->b!=0)
					.map(b->{
						return (b==1 ? "LNB 1" : "LNB 2");
					})
					.orElse("N/A");
		}
	}

	public enum ReferenceSource{
		UNKNOWN,
		INTERNAL,
		EXTERNAL
	}

	public enum ValueState{
		UNDEFINED("UNDEFINED"),
		IN_RANGE(""),
		LESS_THAN("<"),
		MORE_THAN(">");

		private final String operator;

		private ValueState(String operator) {
			this.operator = operator;
		}

		public static String toString(byte index) {
			logger.entry(index);
			return values()[index&3].operator;
		}
	}

	public enum StatusBitsFCM{
		PLL1			(0),
		PLL2			(1),
		MUTE			(2),
		MUTE_TTL		(3),
		PLL3			(4),
		LOCK_SUMMARY	(5),
		INPUT_OVERDRIVE	(6),
		INPUT_LOW		(7),
		AOPC_0			(8),
		AOPC_1			(9),
		AOPC_2			(10),
		LNB_POWER_0		(11),
		LNB_POWER_1		(12),
		LNB_POWER_2		(13),
		HSE				(31); 

		private int bitMask;

		private StatusBitsFCM(int bitPosition){
			bitMask = 1<<bitPosition;
		}

		public boolean isOn(int value){
			return (value & bitMask) != 0;
		}

		public static StatusBitsFCM[] parse(int statusBits){
			return Arrays.stream(values()).filter(sb->(sb.bitMask&statusBits)!=0).toArray(size->new StatusBitsFCM[size]);
		}
	}

	public enum StatusBitsBUC{
		MUTE		(1, 1),
		PLL_UNKNOWN	(0, 6),
		LOCKED		(2, 6),
		UNLOCKED	(3, 6); 

		private int value;
		private int bitMask;

		private StatusBitsBUC(int value, int bitMask){
			this.value= value;
			this.bitMask = bitMask;
		}

		public static StatusBitsBUC[] parse(int statusBits) {
			return Arrays.stream(values()).filter(sb->(sb.bitMask&statusBits)==sb.value).toArray(size->new StatusBitsBUC[size]);
		}

		public boolean isOn(int statusBits){
			return (statusBits & bitMask) == value;
		}
	}

	public void shutdownNow() {
		GuiControllerAbstract.getComPortThreadQueue().removePacketListener(this);
		stop();
		service.shutdownNow();
		serialPort = null;
	}

	private static String bytesToString(byte[] bytes, String prefix) {

		if(bytes==null || bytes.length<2)
			return "N/A";

		final StringBuilder sb = new StringBuilder();
		double value;

		if(bytes.length>2) {
			byte index = (byte) (bytes[0]&3);// 2 bits used

			final String status = ValueState.toString(index);
			sb.append(status);

			final ValueState[] values = ValueState.values();
			if(values[index]==ValueState.UNDEFINED)
				return sb.toString();			

			value = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 1, 3)).getShort()/10.0;

		}else
			// Two bytes - Old stile
			value = ByteBuffer.wrap(bytes).getShort()/10.0;

		sb.append(nFormate1.format(value)).append(" ").append(Translation.getValue(String.class, prefix, prefix));

		return sb.toString();
	}
}
