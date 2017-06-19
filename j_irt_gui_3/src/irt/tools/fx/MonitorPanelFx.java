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
import irt.controller.translation.Translation;
import irt.data.MyThreadFactory;
import irt.data.listener.PacketListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.MeasurementPacket;
import irt.data.packet.Packet;
import irt.data.packet.PacketImp;
import irt.data.packet.Packets;
import irt.data.packet.ParameterHeader;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.LinkedPacket;
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
												}

	@FXML private GridPane 	statusPane;
	@FXML private GridPane 	gridPane;

	public MonitorPanelFx() {

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
	}

    @FXML
    void onMouseClicked(MouseEvent event) {
    	if(event.getClickCount()==3)
    		gridPane.getChildren().clear();
    }

	@Override
	public void run() {
		try{
			GuiControllerAbstract.getComPortThreadQueue().add(packetToSend);
		}catch (Exception e) {
			logger.catching(e);
		}
	}

	@Override
	public void onPacketRecived(final Packet packet) {

		final Optional<Packet> ofNullable;

		if(packet instanceof LinkedPacket)
			ofNullable = Optional
								.ofNullable((LinkedPacket)packet)
								.filter(p->p.getLinkHeader()==null || p.getLinkHeader().getAddr()==packetToSend.getLinkHeader().getAddr())
								.map(Packet.class::cast);
		else
			ofNullable= Optional.ofNullable(packet);

		ofNullable
		.filter(p->p.getHeader().getPacketType()==PacketImp.PACKET_TYPE_RESPONSE)
		.filter(p->p.getHeader().getPacketId()==packetToSend.getHeader().getPacketId())
		.filter(p->p.getHeader().getGroupId()==packetToSend.getHeader().getGroupId())
		.filter(p->p.getHeader().getOption()==PacketImp.ERROR_NO_ERROR)
		.map(Packet::getPayloads)
		.map(pls->pls.stream())
		.ifPresent(stream->{
			stream
			.forEach(pl->{
				logger.entry(pl);

				Platform.runLater(()->{

					boolean isConverter = packetToSend.getLinkHeader().getAddr() == CONVERTER;
					final ParameterHeader 				parameterHeader 	= pl.getParameterHeader();
					final byte 							code 				= parameterHeader.getCode();

					final Optional<? extends ParameterHeaderCode> parameterHeaderCode = isConverter ? ParameterHeaderCodeFCM.valueOf(code) : ParameterHeaderCodeBUC.valueOf(code);

					final ParameterHeaderCode status = isConverter ? ParameterHeaderCodeFCM.STATUS : ParameterHeaderCodeBUC.STATUS;

					//Status bites
					if(code==status.getCode()){
						
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

						//Values (Power, Temperature, Current ...)
					}else{

						final Optional<Label> l = gridPane
													.getChildren()
													.parallelStream()
													.filter(Label.class::isInstance)
													.filter(label->label.getUserData()!=null)
													.filter(label->label.getUserData().equals(code))
													.map(Label.class::cast)
													.findAny();

						if(l.isPresent())
							setText(l.get(), payloadToString(parameterHeaderCode, pl));

						else{	//Create new labels

							final int size = gridPane.getRowConstraints().size();
							gridPane.getRowConstraints().add(new RowConstraints());

							final Label descriptionLabel = createDescriptionLabel(pl);
							gridPane.add(descriptionLabel, 0, size);

							final Label child = new Label(payloadToString(parameterHeaderCode, pl));
							child.getStyleClass().add("value");
							child.setUserData(code);
							child.setTooltip(new Tooltip(descriptionLabel.getText()));

							gridPane.add(child, 1, size);
							GridPane.setVgrow(child, Priority.ALWAYS);
						}
					}
				});
			});
		});
	}

	private void setStatus(List<Label> statusLabels, Payload pl) {

		final int statusBits = pl.getInt(0);

		//Lock Label
		statusLabels
		.stream().filter(n->n.getId().equals(LOCK_ID))
		.findAny()
		.map(Label.class::cast)
		.ifPresent(lbl->{

			final boolean isLocked = isLocked(statusBits);
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
		return parameterHeaderCode.isPresent() ? parameterHeaderCode.get().toString(pl.getBuffer()) : Arrays.toString(pl.getBuffer());
	}

	private Label createDescriptionLabel(Payload pl) {
		final ParameterHeader parameterHeader = pl.getParameterHeader();
		final Optional<? extends ParameterHeaderCode> code = packetToSend.getLinkHeader().getAddr()==CONVERTER ? ParameterHeaderCodeFCM.valueOf(parameterHeader.getCode()) : ParameterHeaderCodeBUC.valueOf(parameterHeader.getCode());

		String text;
		if(code.isPresent()) 
			text = Translation.getValue(String.class, code.get().name(), "TODO: tarnslate = " + code.get().name());

		else
			text = "TODO: code = " + code;

		final Label label = new Label(text + ":");
		label.getStyleClass().add("description");
		label.setPadding(new Insets(0, 5, 0, 0));

		GridPane.setHalignment(label, HPos.RIGHT);

		return label;
	}

	public void start(){

		if(scheduleAtFixedRate==null || scheduleAtFixedRate.isCancelled()){
			GuiControllerAbstract.getComPortThreadQueue().addPacketListener(this);
			scheduleAtFixedRate = service.scheduleAtFixedRate(this, 0, 3, TimeUnit.SECONDS);
		}
	}

	public void stop(){

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
		INPUT_POWER		((byte)11, b->bytesToString(b, "dbm")),
		INPUT_POWER_FCM	((byte) 4, b->bytesToString(b, "dbm")),
		OUTPUT_POWER	((byte) 5, b->bytesToString(b, "dbm")),
		UNIT_TEMPERATURE((byte) 3, b->b!=null && b.length==2 ? nFormate1.format((ByteBuffer.wrap(b).getShort())/10.0) + " C" : Arrays.toString(b)),
		CPU_TEMPERATURE	((byte)10, b->b!=null && b.length==2 ? nFormate1.format((ByteBuffer.wrap(b).getShort())/10.0) + " C" : Arrays.toString(b)),
		V5_5			((byte) 6, b->b!=null && b.length==2 ? nFormate1.format((ByteBuffer.wrap(b).getShort())/1000.0) + " V" : Arrays.toString(b)),
		V13_2			((byte) 7, b->b!=null && b.length==2 ? nFormate1.format((ByteBuffer.wrap(b).getShort())/1000.0) + " V" : Arrays.toString(b)),
		V13_2_NEG		((byte) 8, b->b!=null && b.length==2 ? nFormate1.format((ByteBuffer.wrap(b).getShort())/1000.0) + " V" : Arrays.toString(b)),
		CURRENT			((byte) 9, b->bytesToString(b, "A")),
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

			if(bytes==null || bytes.length<2)
				return "N/A";

			final StringBuilder sb = new StringBuilder();
			if(bytes.length>2)
				sb.append(ValueState.toString(bytes[0]));

			final double value = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 1, 3)).getShort()/10.0;

			sb.append(nFormate1.format(value)).append(" ").append(Translation.getValue(String.class, prefix, prefix));

			return sb.toString();
		}
	}

	public enum ParameterHeaderCodeBUC implements ParameterHeaderCode {
		NONE			( b->Arrays.toString(b)),
		INPUT_POWER		( b->bytesToString(b, "dbm")),
		OUTPUT_POWER	( b->bytesToString(b, "dbm")),
		UNIT_TEMPERATURE( b->b!=null && b.length==2 ? nFormate1.format((ByteBuffer.wrap(b).getShort())/10.0) + " C" : Arrays.toString(b)),
		STATUS			( b->Arrays.toString(b)),
		LNB1_STATUS		( b->Arrays.toString(b)),
		LNB2_STATUS		( b->Arrays.toString(b));

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
			return Arrays.stream(values()).parallel().filter(v->v.code==code).findAny();
		}

		public String toString(byte[] bytes){
			return function.apply(bytes);
		}

		private static String bytesToString(byte[] bytes, String prefix){

			if(bytes==null || bytes.length<2)
				return "N/A";

			final StringBuilder sb = new StringBuilder();
			if(bytes.length>2)
				sb.append(ValueState.toString(bytes[0]));

			final double value = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 1, 3)).getShort()/10.0;

			sb.append(nFormate1.format(value)).append(" ").append(Translation.getValue(String.class, prefix, prefix));

			return sb.toString();
		}
	}

	public enum ReferenceSource{
		UNKNOWN,
		INTERNAL,
		EXTERNAL
	}

	public enum ValueState{
		UNDEFINED(""),
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
		LOCK_SUMMARY	(5),
		PLL1			(0),
		PLL2			(1),
		PLL3			(4),
		MUTE			(2),
		MUTE_TTL		(3),
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
		service.shutdownNow();
	}
}
