package irt.tools.fx;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiControllerAbstract;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.controller.serial_port.SerialPortInterface;
import irt.controller.translation.Translation;
import irt.data.DeviceType;
import irt.data.ThreadWorker;
import irt.data.listener.PacketListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketID;
import irt.data.packet.PacketImp;
import irt.data.packet.Packets;
import irt.data.packet.interfaces.LinkedPacket;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.measurement.MeasurementPacket;
import irt.irt_gui.IrtGui;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class MonitorPanelFx extends AnchorPane implements Runnable, PacketListener, JavaFxPanel{

	private final static Logger logger = LogManager.getLogger();

	public static final byte CONVERTER = 0;

//	private static final String UNMUTED = "unmuted";
//	private static final String MUTED = "muted";
//	private static final String UNLOCKED = "unlocked";
//	private static final String LOCKED = "locked";
//	private static final String LOCK_ID = "lock_status";
//	private static final String MUTE_ID = "mute_status";

	private ScheduledFuture<?> scheduleAtFixedRate;
	private ScheduledExecutorService service;

	private byte unitAddress;

	private final MeasurementPacket packetToSend = (MeasurementPacket) Packets.MEASUREMENT_ALL.getPacketAbstract();

												public byte getUnitAddress() {
													return Optional.ofNullable(unitAddress).orElse((byte)0);
												}

												public void setUnitAddress(byte unitAddress) {
													this.unitAddress = unitAddress;
													packetToSend.setAddr(unitAddress);
												}

	private GridPane 	statusPane;
	private GridPane 	gridPane;

	private Optional<DeviceType> deviceType;

	public MonitorPanelFx(Optional<DeviceType> deviceType) {
		this.deviceType = deviceType;

		MeasurementPacket.setDeviceType(deviceType.orElse(null));

		Thread currentThread = Thread.currentThread();
		currentThread.setUncaughtExceptionHandler((t, e) -> logger.catching(e));

		currentThread.setName(getClass().getSimpleName() + "-" + currentThread.getId());

		try {

			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MonitorPanel.fxml"));
			fxmlLoader.setRoot(this);
			fxmlLoader.setController(this);

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

    	if(event.getClickCount()==2) {

    		StringSelection selection = new StringSelection(tooltip);
    		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    		clipboard.setContents(selection, selection);

    	}else if(event.getClickCount()==3)
    		gridPane.getChildren().clear();
    }

	private SerialPortInterface serialPort;

	@Override
	public void run() {

		try{

			final SerialPortInterface serialPort = ComPortThreadQueue.getSerialPort();
			if(this.serialPort==null || this.serialPort!=serialPort)
				this.serialPort = serialPort;

//			logger.error("this.serialPort: {}; serialPort: {}; isOpened: {}; equals: {}", this.serialPort, serialPort, serialPort.isOpened(), this.serialPort==serialPort);
			if(Optional.ofNullable(this.serialPort).filter(sp->sp==serialPort).map(sp->!sp.isOpened()).orElse(true)){
				shutdownNow();
				return;
			}
			logger.debug("Packet to send: {}", packetToSend);

			GuiControllerAbstract.getComPortThreadQueue().add(packetToSend);

		}catch (Exception e) {
			logger.catching(e);
		}
	}

	private String tooltip;
	@Override
	public void onPacketReceived(final Packet packet) {

		new ThreadWorker(()->{

			Optional<Packet> oPacket = Optional.ofNullable(packet);

			if(notMyPacket(oPacket)) return;

			logger.traceEntry("{}", packet);

			oPacket
			.flatMap(PacketID.MEASUREMENT_ALL::valueOf)
			.map(v->(Map<?, ?>)v)
			.ifPresent(

					map->{

						logger.trace(map);

						if(IrtGui.isProduction()) 
							setMonitorPanelTooltip(map);

						if(deviceType.map(dt->dt!=DeviceType.LNB_REDUNDANCY_1x2).orElse(true))
						// Set Status
						Optional
						.ofNullable((List<?>)map.remove("STATUS"))
						.ifPresent(this::setStatus);

						// Remove Undefined and Not Available fields
						Map<?, ?> collect = map.entrySet().parallelStream()

								.filter(
										entry->{
											final Object value = entry.getValue();
											return !value.equals("N/A") && !value.equals("UNDEFINED");
										})
								.collect(Collectors.toMap(e->e.getKey(), e->e.getValue()));

						logger.trace(collect);
						// Feel Monitor fields
						setValues(collect);
					});
		}, "MonitorPanelFx.onPacketReceived()");
	}

	private void setMonitorPanelTooltip(Map<?, ?> map) {

		final String string = map.toString();

		if(Optional.ofNullable(tooltip).filter(string::equals).isPresent())
			return;

		tooltip = string;
//		Tooltip.install(this, new Tooltip(string));
	}

	private boolean notMyPacket(Optional<Packet> oPacket) {
		Optional<PacketHeader> 	oHeader = oPacket.map(Packet::getHeader);

		Byte addr = oPacket.filter(LinkedPacket.class::isInstance).map(LinkedPacket.class::cast).map(LinkedPacket::getLinkHeader).map(LinkHeader::getAddr).orElse((byte) 0);
		if(addr!=unitAddress)
			return true;
		
		if(!oHeader.filter(h->h.getPacketType()==PacketImp.PACKET_TYPE_RESPONSE).map(PacketHeader::getPacketId).filter(PacketID.MEASUREMENT_ALL::match).isPresent())
			return true;

		if(oHeader.map(PacketHeader::getPacketType).filter(t->t!=PacketImp.PACKET_TYPE_RESPONSE).isPresent())
			return true;

		if(oHeader.map(PacketHeader::getError).filter(t->t!=PacketImp.ERROR_NO_ERROR).isPresent()) 
			return true;

		return false;
	}

	private void setValues(Map<?, ?> map) {
		logger.traceEntry("{}", map);

		final ObservableList<Node> children = gridPane.getChildren();
		final Set<? extends Entry<?,?>> entrySet = map.entrySet();

		Platform.runLater(
				()->{
					if(children.size()/2<map.size())
						children.clear();

					if(children.isEmpty()) {
						ThreadWorker.runThread(
								()->{
									Iterator<?> iterator = entrySet.stream().sorted((a,b)->((String)a.getKey()).compareTo((String)b.getKey())).iterator();
									final int size = entrySet.size();
									IntStream.range(0, size).forEach(
											rowIndex->{

												if(!iterator.hasNext())
													return;

												Entry<?, ?> next = (Entry<?, ?>) iterator.next();
												String key = next.getKey().toString();
												Label descriptionLabel = new Label(Translation.getValue(String.class, key, "* " + key) + ": ");
												descriptionLabel.setPadding(new Insets(0, 5, 0, 0));
												descriptionLabel.getStyleClass().add("description");
												setLabelProperties(descriptionLabel, Pos.CENTER_RIGHT);

												Label valueLabel = new Label(next.getValue().toString());
												valueLabel.getStyleClass().add("value");
												valueLabel.setUserData(key);
												setLabelProperties(valueLabel, Pos.CENTER_LEFT);

												Platform.runLater(
														()->{
															gridPane.add(descriptionLabel, 0, rowIndex);
															gridPane.add(valueLabel, 1, rowIndex);
														});
											});
								}, "Monitor Fields Creation");
					}else {

						ThreadWorker.runThread(
								()->{
									final Map<Object, Node> collect = children.stream().filter(node->node.getUserData()!=null).collect(Collectors.toMap(Node::getUserData, node->node));
									entrySet.forEach(s->Optional.ofNullable((Label) collect.get(s.getKey().toString())).ifPresent(label->Platform.runLater(()->label.setText(s.getValue().toString()))));
								}, "Monitor Set Values");
					}
				});
	}

	private void setStatus(final List<?> status) {
		logger.traceEntry("{}", status);

		if(status==null || status.isEmpty())
			return;

		List<?> collect = status.stream().filter(phc->phc==StatusBitsBUC.UNLOCKED || phc==StatusBitsBUC.LOCKED || phc==StatusBitsBUC.MUTE || phc==StatusBitsBUC.INTERNAL || phc==StatusBitsFCM.LOCK  || phc==StatusBitsFCM.MUTE || phc==StatusBitsFCM.MUTE_TTL).collect(Collectors.toList());


		String toolTipText = status.toString().replaceAll(",", "\n").replaceAll("[\\[\\]]", "");
		Tooltip tooltip = new Tooltip(toolTipText);

		int statusSize = collect.size();
		final ObservableList<Node> children = statusPane.getChildren();
		final int size = children.size();

		if(size>statusSize)
			IntStream.range(statusSize, size).boxed().sorted(Collections.reverseOrder()).forEach(index->Platform.runLater(()->children.remove((int)index)));

		IntStream.range(0, statusSize).forEach(

				index->{

					// Show only UNLOCKED, LOCKED, MUTE statuses
					String statusBit = collect.get(index).toString();
					String string = Translation.getValue(String.class, statusBit, statusBit);

					Platform.runLater(
							()->{

								if(children.size()<=index) {

									final Label label = new Label(string);
									label.getStyleClass().add(statusBit);
									label.setTooltip(tooltip);
									label.setUserData(statusBit);
									setLabelProperties(label, Pos.CENTER);
									statusPane.add(label, index, 0);

								}else {

									Label label = (Label) children.get(index);
									String text = label.getText();
									if(!text.equals(string)) {
										final Object userData = label.getUserData();
										if(!statusBit.equals(userData)) {
											final ObservableList<String> styleClass = label.getStyleClass();
											styleClass.remove(userData);
											styleClass.add(statusBit);
											label.setUserData(statusBit);
										}
										label.setText(string);
									}

									Tooltip t = label.getTooltip();
									if(!t.getText().equals(toolTipText))
										t.setText(toolTipText);
								}
							});
				});		
	}

	private void setLabelProperties(final Label label, Pos alignment) {
		label.setAlignment(alignment);
		label.setMaxWidth(Double.MAX_VALUE);
		label.setMaxHeight(Double.MAX_VALUE);
		GridPane.setHgrow(label, Priority.ALWAYS);
		GridPane.setVgrow(label, Priority.ALWAYS);
		GridPane.setFillWidth(label, true);
	}

	public void start(){
//		logger.error("");

		if(Optional.ofNullable(scheduleAtFixedRate).filter(sfr->!sfr.isDone()).isPresent())
			return;

		if(!Optional.ofNullable(service).filter(sfr->!sfr.isShutdown()).isPresent())
			service = Executors.newSingleThreadScheduledExecutor(new ThreadWorker("MonitorPanelFx.service"));

			
			GuiControllerAbstract.getComPortThreadQueue().addPacketListener(this);
			scheduleAtFixedRate = service.scheduleAtFixedRate(this, 0, 3, TimeUnit.SECONDS);
	}

	public void stop(){
		//Show Stack Trace
//		logger.error(Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).reduce((s1, s2) -> s1 + "\n" + s2).get());

		GuiControllerAbstract.getComPortThreadQueue().removePacketListener(this);
		Optional.ofNullable(scheduleAtFixedRate).filter(sfr->!sfr.isDone()).ifPresent(sfr->sfr.cancel(true));
		Optional.ofNullable(service).filter(s->!s.isShutdown()).ifPresent(ScheduledExecutorService::shutdownNow);
	}

	private static NumberFormat nFormate1 = new DecimalFormat("#0.0");
//	private static NumberFormat nFormate3 = new DecimalFormat("#0.000");
	public interface ParameterHeaderCode{
		byte getCode();
		int ordinal();
		String name();
		String toString(byte[] bytes);
		ParameterHeaderCode getStatus();
		List<StatusBits> parseStatusBits(int statusBits);
		Entry<String, String> toEntry(byte[] bytes);
	}

	public enum ParameterHeaderCodeFCM implements ParameterHeaderCode{
		NONE			((byte) 0, b->Arrays.toString(b)),
		SUMMARY_ALARM	((byte) 1, b->Arrays.toString(b)),
		STATUS			((byte) 2, b->Arrays.toString(b)),
		INPUT_POWER_FCM	((byte) 4, b->bytesToString(b, "dbm")),
		OUTPUT_POWER	((byte) 5, b->bytesToString(b, "dbm")),
		UNIT_TEMPERATURE((byte) 3, b->b!=null && b.length==2 ? nFormate1.format((ByteBuffer.wrap(b).getShort())/10.0) + " �C" : Arrays.toString(b)),
		V5_5			((byte) 6, b->b!=null && b.length==2 ? nFormate1.format((ByteBuffer.wrap(b).getShort())/1000.0) + " V" : Arrays.toString(b)),
		V13_2			((byte) 7, b->b!=null && b.length==2 ? nFormate1.format((ByteBuffer.wrap(b).getShort())/1000.0) + " V" : Arrays.toString(b)),
		V13_2_NEG		((byte) 8, b->b!=null && b.length==2 ? nFormate1.format((ByteBuffer.wrap(b).getShort())/1000.0) + " V" : Arrays.toString(b)),
		CURRENT			((byte) 9, b->bytesToString(b, "A")),
		CPU_TEMPERATURE	((byte)10, b->b!=null && b.length==2 ? nFormate1.format((ByteBuffer.wrap(b).getShort())/10.0) + " �C" : Arrays.toString(b)),
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

		public static Optional<? extends ParameterHeaderCode> valueOf(Byte code){
			return Arrays.stream(values()).parallel().filter(v->v.code==code).findAny();
		}

		public String toString(byte[] bytes){
			return function.apply(bytes);
		}

		private static String bytesToString(byte[] bytes, String prefix){
			return MonitorPanelFx.bytesToString(bytes, prefix);
		}

		@Override
		public ParameterHeaderCode getStatus() {
			return STATUS;
		}

		@Override
		public List<StatusBits> parseStatusBits(int statusBits) {
			return StatusBitsFCM.parse(statusBits);
		}

		@Override
		public Entry<String, String> toEntry(byte[] bytes) {
			return new AbstractMap.SimpleEntry<>(name(), function.apply(bytes));
		}
	}

	public enum ParameterHeaderCodeBUC implements ParameterHeaderCode {
		NONE			( b->Arrays.toString(b)),
		INPUT_POWER		( b->bytesToString(b, "dbm")),
		OUTPUT_POWER	( b->bytesToString(b, "dbm")),
		UNIT_TEMPERATURE( b->b!=null && b.length==2 ? ((b[0]&0x80)>0 && b[1]==0) ? "UNDEFINED" : nFormate1.format((ByteBuffer.wrap(b).getShort())/10.0) + " �C" : Arrays.toString(b)),
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

		public static Optional<? extends ParameterHeaderCode> valueOf(Byte code){
			return Arrays.stream(values()).parallel().filter(v->v.code==code).findAny();
		}

		public String toString(byte[] bytes){
			return function.apply(bytes);
		}

		private static String bytesToString(byte[] bytes, String prefix){
			return MonitorPanelFx.bytesToString(bytes, prefix);
		}

		private static String lnbReady(byte[] bytes) {

			if(bytes==null)
				return "N/A";

			if(bytes.length==1) {
				final String string = bytes[0]==1 ? "ready" : "ready.not";
				return Translation.getValue(String.class, string, string);
			}

			return bytesToString(bytes, "dbm");
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

		@Override
		public ParameterHeaderCode getStatus() {
			return STATUS;
		}

		@Override
		public List<StatusBits> parseStatusBits(int statusBits) {
			return StatusBitsBUC.parse(statusBits);
		}

		@Override
		public Entry<String, String> toEntry(byte[] bytes) {

			final String name;

			if(this==LNB1_STATUS && bytes.length>1) {
				name = REFLECTED_POWER.name();
			}else
				name = name();

			return new AbstractMap.SimpleEntry<>(name, function.apply(bytes));
		}
	}
	public enum ParameterHeaderCodeLNB implements ParameterHeaderCode {
		NONE			( b->Arrays.toString(b)),
		INPUT_POWER		( b->bytesToString(b, "dbm")),
		OUTPUT_POWER	( b->bytesToString(b, "dbm")),
		UNIT_TEMPERATURE( b->b!=null && b.length==2 ? ((b[0]&0x80)>0 && b[1]==0) ? "UNDEFINED" : nFormate1.format((ByteBuffer.wrap(b).getShort())/10.0) + " C" : Arrays.toString(b)),
		STATUS			( b->systemStatus(b)),
		LNBA_STATUS		( b->lnbReady(b)),
		LNBB_STATUS		( b->lnbReady(b)),
		LNBS_STATUS 	( b->lnbReady(b)),
		DOWNLINK_WAVEGUIDE_SWITCH( b->switchPosition(b)),
		DOWNLINK_STATUS(b->null);// Downlink status is deprecated

		private final Function<byte[], String> function;
		private final byte code;

		public byte getCode() {
			return code;
		}

		private static String systemStatus(byte[] bs) {

			final int b = bs[0]&0xff;
			switch(b) {
			case 10:
				return "Prohibited";
			case 11:
				return "Default";
			case 12:
				return "Protection LNB A";
			case 13:
				return "Protection LNB B";
			}
			return "Unknown";
		}

		private ParameterHeaderCodeLNB(Function<byte[], String> function){
			this.code = (byte) ordinal();
			this.function = function;
		}

		public static Optional<? extends ParameterHeaderCode> valueOf(Byte code){
			return Arrays.stream(values()).parallel().filter(v->v.code==code).findAny();
		}

		public String toString(byte[] bytes){
			return function.apply(bytes);
		}

		private static String bytesToString(byte[] bytes, String prefix){
			return MonitorPanelFx.bytesToString(bytes, prefix);
		}

		private static String lnbReady(byte[] bytes) {

			if(bytes==null)
				return "N/A";

			if(bytes.length==1) {
				final String string = bytes[0]==1 ? "ready" : "ready.not";
				return Translation.getValue(String.class, string, string);
			}

			return bytesToString(bytes, "dbm");
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

		@Override
		public ParameterHeaderCode getStatus() {
			return STATUS;
		}

		@Override
		public List<StatusBits> parseStatusBits(int statusBits) {
			return StatusBitsBUC.parse(statusBits);
		}

		@Override
		public Entry<String, String> toEntry(byte[] bytes) {

			return new AbstractMap.SimpleEntry<>(name(), function.apply(bytes));
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
			return values()[index&3].operator;
		}
	}

	public interface StatusBits{
		
	}

	public enum StatusBitsFCM implements StatusBits{
		PLL1			(0),
		PLL2			(1),
		MUTE			(2),
		MUTE_TTL		(3),
		PLL3			(4),
		LOCK			(5),	// LOCK_SUMMARY
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

		public static List<StatusBits> parse(int statusBits){
			return Arrays.stream(values()).filter(sb->(sb.bitMask&statusBits)!=0).collect(Collectors.toList());
		}

		@Override
		public String toString(){
			return name();
		}
	}

	public enum StatusBitsBUC implements StatusBits{

		MUTE		(1, 1),
		PLL_UNKNOWN	(0, 6),
		LOCKED		(2, 6),
		UNLOCKED	(4, 6),
		INTERNAL	(16, 16); 

		private int value;
		private int bitMask;

		private StatusBitsBUC(int value, int bitMask){
			this.value= value;
			this.bitMask = bitMask;
		}

		public static List<StatusBits> parse(int statusBits) {
			return Arrays.stream(values()).filter(sb->(sb.bitMask&statusBits)==sb.value).collect(Collectors.toList());
		}

		public boolean isOn(int statusBits){
			return (statusBits & bitMask) == value;
		}

		@Override
		public String toString(){
			return name();
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

	public void refresh() {

		gridPane.getChildren().parallelStream().filter(Label.class::isInstance).collect(Collectors.groupingBy(GridPane::getRowIndex))
		.entrySet().forEach(
				e->{
					e.getValue().parallelStream().map(Node::getUserData).filter(ud->ud!=null).findAny().map(Object::toString)
					.ifPresent(
							ud->{
								final Optional<String> oTranslation = Optional.ofNullable(Translation.getValue(ud, null));

								if(!oTranslation.isPresent())
									logger.warn("'{}' - Have to add to Translation", ud);

								oTranslation
								.ifPresent(
										text->e.getValue().parallelStream().filter(n->n.getUserData()==null).findAny().map(Label.class::cast)
										.ifPresent(l->Platform.runLater(()->l.setText(text))));
								});
				});
	}
}
