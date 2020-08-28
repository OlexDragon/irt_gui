
package irt.tools.fx.module;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiControllerAbstract;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.data.ThreadWorker;
import irt.data.RedundancyControllerUnitStatus;
import irt.data.listener.PacketListener;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp.PacketGroupIDs;
import irt.data.packet.PacketSuper;
import irt.data.packet.PacketWork.PacketIDs;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.redundancy.RedundancyControllerStatusPacket;
import irt.data.packet.redundancy.RedundancyControllerStatusPacket.StatusFlags;
import irt.data.packet.redundancy.StandbyModePacket;
import irt.data.packet.redundancy.SwitchoverModePacket;
import irt.data.packet.redundancy.SwitchoverPacket;
import irt.tools.fx.interfaces.StopInterface;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SelectionModel;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class ControlPanelIrPcFx extends AnchorPane implements PacketListener, Runnable, StopInterface {

	protected final static Logger logger = LogManager.getLogger();

	protected final static String BUTTON_1_NAME = "Protection A";
	protected final static String BUTTON_2_NAME = "Protection B";
	protected final static String BUTTON_3_NAME = "Restor Default";

	private final static int UNIT_A = 1;
	private final static int UNIT_B = 2;
	private final static int UNIT_S = 3;

	private final static int OPERATIONAL = 1;
	private final static int CONNECTED = 2;
	private final static int UNIT_ALARM = 3;
	private final static int REDUNDANCY_READY = 4;

	private final Byte addr;

	private final Map<StatusFlags, Node> nodesMap = new HashMap<>();
	private final Map<Integer, Map<Integer, Node>> unitsFiesdsMap = new HashMap<>();

	public ControlPanelIrPcFx(Byte addr) {
		Thread currentThread = Thread.currentThread();
		currentThread.setName(getClass().getSimpleName() + "-" + currentThread.getId());

		this.addr = addr;

		packets.add(new RedundancyControllerStatusPacket(addr));

		final URL resource = getClass().getResource("ControlPanelIrPc.fxml");

		FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	}

	private ScheduledFuture<?> scheduledFuture;
	private ScheduledExecutorService service;

	private final List<PacketSuper> packets = new ArrayList<>();

	@FXML private Button btn1;
	@FXML private Button btn2;

	@FXML private ChoiceBox<SwitchoverModes> cbSwitchoverMode;
	@FXML private ChoiceBox<StandbyModes> cbStandbyMode;

	@FXML private BorderPane bpRedundancyReady;
	@FXML private BorderPane bpSwitch1Ready;
	@FXML private BorderPane bpSwitch2Ready;

	@FXML private AnchorPane apUnitsStatus;

	@FXML private BorderPane bpPresentUnit1;
	@FXML private BorderPane bpPresentUnit2;
	@FXML private BorderPane bpPresentUnit3;

	@FXML private BorderPane bpAlarmUnitA;
	@FXML private BorderPane bpAlarmUnitB;
	@FXML private BorderPane bpAlarmUnitS;

	@FXML private BorderPane bpUnitReady1;
	@FXML private BorderPane bpUnitReady2;
	@FXML private BorderPane bpUnitReady3;

	@FXML private BorderPane bpRedundancyReady1;
	@FXML private BorderPane bpRedundancyReady2;
	@FXML private BorderPane bpRedundancyReady3;

//	private final ChangeListener<? super SwitchoverModes> switchoverModesListener = (o, oV, nV)->cbAction(nV);
//	private final ChangeListener<? super StandbyModes> standbyModesListener = (o, oV, nV)->cbAction(nV);

	private void cbAction(Object enumValue) {
		try {

//			logger.error("cbAction({})", enumValue);

			final Method method = enumValue.getClass().getMethod("getPacket");
			final PacketSuper packetWork = (PacketSuper) method.invoke(enumValue);
			packetWork.setAddr(addr);
			GuiControllerAbstract.getComPortThreadQueue().add(packetWork);

		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			logger.catching(e);
		}
	}

	@FXML public void initialize() {

		nodesMap.put(StatusFlags.REDUNDANCY_READY	, bpRedundancyReady);
		nodesMap.put(StatusFlags.STANDBY_POWER_MODE	, cbStandbyMode);
		nodesMap.put(StatusFlags.SW1_READY			, bpSwitch1Ready);
		nodesMap.put(StatusFlags.SW2_READY			, bpSwitch2Ready);
		nodesMap.put(StatusFlags.SWITCHOVER_MODE	, cbSwitchoverMode);

		Map<Integer, Node> map = new HashMap<>();
		unitsFiesdsMap.put(UNIT_A, map);
		map.put(OPERATIONAL		, bpUnitReady1);
		map.put(CONNECTED		, bpPresentUnit1);
		map.put(UNIT_ALARM, bpAlarmUnitA);
		map.put(REDUNDANCY_READY, bpRedundancyReady1);

		map = new HashMap<>();
		unitsFiesdsMap.put(UNIT_B, map);
		map.put(OPERATIONAL		, bpUnitReady2);
		map.put(CONNECTED		, bpPresentUnit2);
		map.put(UNIT_ALARM, bpAlarmUnitB);
		map.put(REDUNDANCY_READY, bpRedundancyReady2);

		map = new HashMap<>();
		unitsFiesdsMap.put(UNIT_S, map);
		map.put(OPERATIONAL		, bpUnitReady3);
		map.put(CONNECTED		, bpPresentUnit3);
		map.put(UNIT_ALARM		, bpAlarmUnitS);
		map.put(REDUNDANCY_READY, bpRedundancyReady3);

		final ObservableList<SwitchoverModes> switchoverModesList = FXCollections.observableArrayList(SwitchoverModes.values());
		switchoverModesList.remove(SwitchoverModes.NONE);
		cbSwitchoverMode.setItems(switchoverModesList);
		cbSwitchoverMode.setUserData(SwitchoverModes.class);
//		cbSwitchoverMode.getSelectionModel().selectedItemProperty().addListener(switchoverModesListener);
		cbSwitchoverMode.setStyle("-fx-font-size: 12px;");
		cbSwitchoverMode.setOnAction(e->cbAction(cbSwitchoverMode.getSelectionModel().getSelectedItem()));

		final ObservableList<StandbyModes> standbyModesList = FXCollections.observableArrayList(StandbyModes.values());
		standbyModesList.remove(StandbyModes.NONE);
		cbStandbyMode.setItems(standbyModesList);
		cbStandbyMode.setUserData(StandbyModes.class);
//		cbStandbyMode.getSelectionModel().selectedItemProperty().addListener(standbyModesListener);
		cbStandbyMode.setStyle("-fx-font-size: 12px;");

		cbStandbyMode.setOnAction(e->cbAction(cbStandbyMode.getSelectionModel().getSelectedItem()));
		

		btn1.setFont(new Font(12));
		btn2.setFont(new Font(12));
		//		showAllChildren(this);
	}

    @FXML  void btn1Action() {
    	final int switchNumber = btn2.isDisable() ? 3 : 1;
		final SwitchoverPacket switchoverPacket = new SwitchoverPacket(addr, switchNumber);
		GuiControllerAbstract.getComPortThreadQueue().add(switchoverPacket);
    	run();
    }

    @FXML void btn2Action() {
    	final int switchNumber = btn1.isDisable() ? 3 : 2;
		final SwitchoverPacket switchoverPacket = new SwitchoverPacket(addr, switchNumber);
		GuiControllerAbstract.getComPortThreadQueue().add(switchoverPacket);
       	run();
    }

//	private void showAllChildren(Object parent) {
//		logger.error(parent);
//		Optional.of(parent).filter(Parent.class::isInstance).map(Parent.class::cast).map(p->p.getChildrenUnmodifiable()).map(ObservableList::stream).orElse(Stream.empty()).forEach(this::showAllChildren);
//	}

	@Override
	public void onPacketReceived(Packet packet) {

		final Optional<Packet> oPacket = Optional.of(packet);
		final Optional<PacketHeader> oHeader = oPacket.map(Packet::getHeader);

		// Return if not write packet
		if(!oHeader.map(PacketHeader::getGroupId).filter(PacketGroupIDs.REDUNDANCY::match).isPresent())
			return;

		new ThreadWorker(
				()->{

					Optional<Map<?, ?>> oValue = oHeader

							.map(PacketHeader::getPacketId)
							.flatMap(PacketIDs::valueOf)
							.flatMap(pId->pId.valueOf(packet))
							.filter(Map.class::isInstance)
							.map(Map.class::cast);

					logger.trace("{}; {}", oValue, packet);

					// Set flags value
					oValue
					.map(map->map.get(RedundancyControllerStatusPacket.FLAGS))
					.map(Map.class::cast)
					.map(Map::entrySet)
					.map(es->(Stream<?>)es.stream())
					.orElse(Stream.empty())
					.map(Map.Entry.class::cast)
					.forEach(
							entry->{
								final StatusFlags key = (StatusFlags)entry.getKey();
								final Object value = entry.getValue();
								final Optional<Node> oNode = Optional.ofNullable(nodesMap.get(key));

								switch(key) {
								case SW1_READY:
								case SW2_READY:
								case REDUNDANCY_READY:
							
									String text = value.toString();
									showText(oNode, text);

									String className = Optional.of(value).filter(YesNo.class::isInstance).map(v->v==YesNo.YES ? "no_error" : "error").orElse("");
									oNode.ifPresent(setClassName(className));
									break;
								case STANDBY_POWER_MODE:
								case SWITCHOVER_MODE:
									oNode
									.filter(ChoiceBox.class::isInstance)
									.map(cb->(ChoiceBox<?>) cb)
									.ifPresent(
											cb->
											Platform.runLater(
												()->{
													SelectionModel<?> selectionModel = cb.getSelectionModel();
													Object selectedItem = selectionModel.getSelectedItem();
													if(selectedItem==null || !selectedItem.equals(value)) {
														EventHandler<ActionEvent> onAction = cb.getOnAction();
														cb.setOnAction(null);
														int indexOf = cb.getItems().indexOf(value);
														selectionModel.select(indexOf);
														cb.setOnAction(onAction);
													}
												}));
								}
							});

			
					// Set units status
					oValue
					.map(map->(List<?>)map.get(RedundancyControllerStatusPacket.STATUS))
					.map(List::stream)
					.orElse(Stream.empty())
					.map(RedundancyControllerUnitStatus.class::cast)
					.forEach(
							unitStatus->{

								int unitId = unitStatus.getId();
								unitsFiesdsMap
								.get(unitId)
								.entrySet()
								.stream()
								.forEach(
										entry->{

											String text;
											String className = "";
											int key = entry.getKey();

											switch(key) {
											case OPERATIONAL:
												YesNo op = unitStatus.getOperational();
												className = op==YesNo.NO ? "error" : "no_error";
												text = op.toString();
												break;
											case CONNECTED:
												YesNo con = unitStatus.getConnected();
												className = con==YesNo.NO ? "error" : "no_error";
												text = con.toString();
												break;
											case REDUNDANCY_READY:
												UnitStatusNames statusName = unitStatus.getStatusName();
												text = statusName.toString();
												setButtonsText(unitId, statusName);
												break;
											case UNIT_ALARM:
											default:
												Optional<YesNo> map = Optional.of(unitStatus.getConnected()).filter(YesNo.YES::equals).map(connected->unitStatus.getUnitAlarm());
												text = map.map(YesNo::toString).orElse("");
												className = map.map(y->y!=YesNo.NO ? "error" : "no_error").orElse("");
											}

											Optional<Node> oNode = Optional.ofNullable(entry.getValue());
											showText(oNode, text);

											oNode.ifPresent(setClassName(className));
										});
							});
				}, "ControlPanelIrPcFx.onPacketReceived()");
	}

	private Consumer<? super Node> setClassName(final String className) {
		return n->Platform.runLater(
				()->{
					ObservableList<String> styleClass = n.getStyleClass();
					if(!styleClass.contains(className)) {
						styleClass.remove("error");
						styleClass.remove("no_error");
						if(!className.isEmpty())
							styleClass.add(className);
					}
				});
	}

	private void setButtonsText(int unitId, UnitStatusNames statusName) {

		final boolean isAutomatic = Optional.ofNullable(cbSwitchoverMode).map(ChoiceBox::getSelectionModel).map(SelectionModel::getSelectedItem).map(item->item.equals(SwitchoverModes.AUTOMATIC)).isPresent();
		Boolean disable1 = true;
		Boolean disable2 = true;

		String button1Text;
		String button2Text;

		switch(statusName){
		case ONLINE:
			button1Text = unitId==1 ? BUTTON_1_NAME : null;
			button2Text = unitId==2 ? BUTTON_2_NAME : null;

			if(isAutomatic) break;

			disable1 = unitId==1 ? false : null;
			disable2 = unitId==2 ? false : null;
			break;
		case STANDBY:
			button1Text = unitId==1 ? BUTTON_3_NAME : null;
			button2Text = unitId==2 ? BUTTON_3_NAME : null;

			if(isAutomatic) break;

			disable1 = unitId==1 ? false : null;
			disable2 = unitId==2 ? false : null;
			break;
		case PROTECTION_A:
			button1Text = unitId==3 ? BUTTON_3_NAME : null;
			button2Text = unitId==3 ? BUTTON_2_NAME : null;

			if(isAutomatic) break;

			disable1 = unitId==3 ? false : null;
			disable2 = unitId==3 ? true : null;
			break;
		case PROTECTION_B:
			button1Text = unitId==3 ? BUTTON_1_NAME : null;
			button2Text = unitId==3 ? BUTTON_3_NAME : null;

			if(isAutomatic) break;

			disable1 = unitId==3 ? true : null;
			disable2 = unitId==3 ? false : null;
			break;
		default:
			button1Text = BUTTON_1_NAME;
			button2Text = BUTTON_2_NAME;
		}

		logger.debug("unitId: {}; statusName: {};  button1Text: {}; disable1: {}; button2Text: {}; disable2: {};", unitId, statusName, button1Text, disable1, button2Text, disable2);
		
		Optional.of(button1Text).filter(text->!btn1.getText().equals(text)).ifPresent(text->Platform.runLater(()->btn1.setText(text)));
		Optional.ofNullable(disable1).filter(d->btn1.isDisable()!=d).ifPresent(d->Platform.runLater(()->btn1.setDisable(d)));

		Optional.of(button2Text).filter(text->!btn2.getText().equals(text)).ifPresent(text->Platform.runLater(()->btn2.setText(text)));
		Optional.ofNullable(disable2).filter(d->btn2.isDisable()!=d).ifPresent(d->Platform.runLater(()->btn2.setDisable(d)));
	}

	private void showText(final Optional<Node> oNode, String text) {
		oNode
		.filter(BorderPane.class::isInstance)
		.map(BorderPane.class::cast)
		.map(bp->bp.lookup("Text"))
		.map(Text.class::cast)
		.filter(t->!t.getText().equals(text))
		.ifPresent(t->Platform.runLater(()->t.setText(text)));
	}

	@Override
	public void run() {
		 ComPortThreadQueue comPortThreadQueue = GuiControllerAbstract.getComPortThreadQueue();
		packets.forEach(comPortThreadQueue::add);
	}

	public enum SwitchoverModes{
		NONE,
		AUTOMATIC,
		MANUAL;

		private SwitchoverModePacket packet;

		private SwitchoverModes(){
			packet = new SwitchoverModePacket((byte)0, this);
		}

		public static SwitchoverModes parse(Integer flag){
			int f = (flag>>4)&3;

			return values()[f];
		}

		public PacketSuper getPacket(){
			return packet;
		}
	}

	public enum StandbyModes{
		NONE,
		HOT,
		COLD;

		private StandbyModePacket packet;

		private StandbyModes(){
			packet = new StandbyModePacket((byte)0, this);
		}

		public static StandbyModes parse(Integer flag){
			int f = (flag>>8)&3;

			return values()[f];
		}

		public PacketSuper getPacket(){
			return packet;
		}
	}

	public enum Ready{
		UNKNOWN,
		READY,
		WARNING,
		ALARM;

		public static Ready parse(Integer flag){
			int f = (flag>>2)&3;

			return values()[f];
		}
	}
	public enum YesNo{
		NO,
		YES;

		public static YesNo parse(Integer flag){
			return flag>0 ? YES : NO;
		}
	}
//	public enum Alarms{
//		NO_ALARM("No Alarm"),
//		ALARM("Alarm");
//
//		private String message;
//		private Alarms(String message){
//			this.message = message;
//		}
//
//		public static Alarms parse(Integer flag){
//			return flag>0 ? ALARM : NO_ALARM;
//		}
//
//		@Override
//		public String toString(){
//			return message;
//		}
//	}
	public enum  UnitStatusNames{
			UNKNOWN			("Unknown"),
			STANDALONE		("Standalone"),
			ONLINE			("Online"),
			STANDBY			("Standby"),
			PROTECTION_A	("Protection A"),
			PROTECTION_B	("Protection B"),
			UNKNOWN_2		("Unknown");

			private String name;

			private UnitStatusNames(String name){
				this.name = name;
			}

			public static UnitStatusNames parse(Integer flag){
				return values()[flag&7];
			}

			@Override
			public String toString(){
				return name;
			}
	}

	public enum  AlarmSeverityNames{
			NO_ALARM		("No alarm"),
			INDETERMINATE	("Indeterminate"),
			WARNING			("Warning"),
			MINOR			("Minor"),
			MAJOR			("Major"),
			CRITICAL		("Critical"),
			UNKNOWN			("Unknown");

			private String name;

			private AlarmSeverityNames(String name){
				this.name = name;
			}

			public static AlarmSeverityNames parse(Integer flag){
				return values()[flag&7];
			}

			@Override
			public String toString(){
				return name;
			}
		}

	public void start() {

		if(Optional.ofNullable(service).filter(s->!s.isShutdown()).isPresent())
			return;

		service = Executors.newSingleThreadScheduledExecutor(new ThreadWorker("ControlPanelIrPcFx"));
		scheduledFuture = service.scheduleAtFixedRate(this, 1, 10, TimeUnit.SECONDS);
		GuiControllerAbstract.getComPortThreadQueue().addPacketListener(this);
	};

	@Override
	public void stop() {

		GuiControllerAbstract.getComPortThreadQueue().removePacketListener(this);
		Optional.ofNullable(scheduledFuture).filter(ft->!ft.isDone()).ifPresent(ft->ft.cancel(true));
		Optional.ofNullable(service).filter(s->!s.isShutdown()).ifPresent(ScheduledExecutorService::shutdownNow);
	}
}
