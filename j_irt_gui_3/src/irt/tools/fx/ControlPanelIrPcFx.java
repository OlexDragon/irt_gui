
package irt.tools.fx;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiControllerAbstract;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.data.MyThreadFactory;
import irt.data.RedundancyControllerUnitStatus;
import irt.data.listener.PacketListener;
import irt.data.packet.PacketAbstract;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.interfaces.PacketWork;
import irt.data.packet.redundancy.RedundancyControllerStatusPacket;
import irt.data.packet.redundancy.StandbyModePacket;
import irt.data.packet.redundancy.SwitchoverModePacket;
import irt.data.packet.redundancy.SwitchoverPacket;
import irt.tools.fx.interfaces.StopInterface;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SelectionModel;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

public class ControlPanelIrPcFx extends AnchorPane implements PacketListener, Runnable, StopInterface {

	protected final static Logger logger = LogManager.getLogger();

	protected final static String BUTTON_1_NAME = "Protection A";
	protected final static String BUTTON_2_NAME = "Protection B";
	protected final static String BUTTON_3_NAME = "Set Default";
	private Byte addr;

	public ControlPanelIrPcFx(Byte addr) {

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

		comPortThreadQueue = GuiControllerAbstract.getComPortThreadQueue();
		comPortThreadQueue.addPacketListener(this);

		scheduledFuture = service.scheduleAtFixedRate(this, 1, 10, TimeUnit.SECONDS);
	}

	private final ScheduledFuture<?> scheduledFuture;
	private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(new MyThreadFactory());
	private final ComPortThreadQueue comPortThreadQueue;

	private final List<PacketAbstract> packets = new ArrayList<>();

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
	@FXML private BorderPane bpSwitchhowerUnit1;
	@FXML private BorderPane bpSwitchhowerUnit2;
	@FXML private BorderPane bpSwitchhowerUnit3;
	@FXML private BorderPane bpUnitReady1;
	@FXML private BorderPane bpUnitReady2;
	@FXML private BorderPane bpUnitReady3;
	@FXML private BorderPane bpRedundancyReady1;
	@FXML private BorderPane bpRedundancyReady2;
	@FXML private BorderPane bpRedundancyReady3;

	private final ChangeListener<? super SwitchoverModes> switchoverModesListener = (o, oV, nV)->cbAction(nV);
	private final ChangeListener<? super StandbyModes> standbyModesListener = (o, oV, nV)->cbAction(nV);
	private void cbAction(Object enumValue) {
		try {

			final Method method = enumValue.getClass().getMethod("getPacket");
			final PacketAbstract packetWork = (PacketAbstract) method.invoke(enumValue);
			packetWork.setAddr(addr);
			comPortThreadQueue.add(packetWork);

		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			logger.catching(e);
		}
	}

	@FXML public void initialize() {

		final ObservableList<SwitchoverModes> switchoverModesList = FXCollections.observableArrayList(SwitchoverModes.values());
		switchoverModesList.remove(SwitchoverModes.NONE);
		cbSwitchoverMode.setItems(switchoverModesList);
		cbSwitchoverMode.setUserData(SwitchoverModes.class);
		cbSwitchoverMode.getSelectionModel().selectedItemProperty().addListener(switchoverModesListener);

		final ObservableList<StandbyModes> standbyModesList = FXCollections.observableArrayList(StandbyModes.values());
		standbyModesList.remove(StandbyModes.NONE);
		cbStandbyMode.setItems(standbyModesList);
		cbStandbyMode.setUserData(StandbyModes.class);
		cbStandbyMode.getSelectionModel().selectedItemProperty().addListener(standbyModesListener);

		bpSwitch1Ready.setUserData(YesNo.class);
		bpSwitch2Ready.setUserData(YesNo.class);
		bpRedundancyReady.setUserData(Ready.class);


		//		showAllChildren(this);
	}

    @FXML  void btn1Action() {
    	final int switchNumber = btn2.isDisable() ? 3 : 1;
		final SwitchoverPacket switchoverPacket = new SwitchoverPacket(addr, switchNumber);
    	comPortThreadQueue.add(switchoverPacket);
    	run();
    }

    @FXML void btn2Action() {
    	final int switchNumber = btn1.isDisable() ? 3 : 2;
		final SwitchoverPacket switchoverPacket = new SwitchoverPacket(addr, switchNumber);
       	comPortThreadQueue.add(switchoverPacket);
       	run();
    }

//	private void showAllChildren(Object parent) {
//		logger.error(parent);
//		Optional.of(parent).filter(Parent.class::isInstance).map(Parent.class::cast).map(p->p.getChildrenUnmodifiable()).map(ObservableList::stream).orElse(Stream.empty()).forEach(this::showAllChildren);
//	}

	@Override
	public void stop() {
		comPortThreadQueue.removePacketListener(this);
		Optional.of(scheduledFuture).filter(ft->!ft.isDone()).ifPresent(ft->ft.cancel(true));
		Optional.of(service).filter(s->!s.isShutdown()).ifPresent(ScheduledExecutorService::shutdownNow);
	}

	@Override
	public void onPacketRecived(Packet packet) {

		final Optional<Packet> oPacket = Optional.of(packet);
		final Optional<PacketHeader> oHeader = oPacket.map(Packet::getHeader);

		// Return if not write packet
		if(!oHeader.map(PacketHeader::getGroupId).filter(gId->gId==PacketImp.GROUP_ID_REDUNDANCY_CONTROLLER).isPresent())
			return;

		final Optional<Short> oPacketId = oHeader.map(PacketHeader::getPacketId);

		if(oPacketId.filter(pId->pId==PacketWork.PACKET_ID_REDUNDANCY_CONTROLLER_STATUS).isPresent()){
			parseStatus(oPacket);
			return;
		}
	}

	private final static int BITS_MASK_SW1_READY 			= 1;
	private final static int BITS_MASK_SW2_READY 			= 2;
	private final static int BITS_MASK_REDUNDANCY_READY 	= 12;
	private final static int BITS_MASK_SWITCHOVER_MODE 		= 240;
	private final static int BITS_MASK_STANDBY_POWER_MODE 	= 3840;
//	private final static int BITS_MASK_ALARM 				= 28672;

	private void parseStatus(Optional<Packet> oPacket) {
		oPacket
		.map(Packet::getPayloads)
		.map(List::stream)
		.flatMap(Stream::findAny)
		.map(Payload::getBuffer)
		.ifPresent(
				bs->{

					// First 4 bytes System Flags
					final ByteBuffer bb = ByteBuffer.wrap(bs);
					parseSystemFlags(bb);

					final boolean isAutomatic = Optional.ofNullable(cbSwitchoverMode).map(ChoiceBox::getSelectionModel).map(SelectionModel::getSelectedItem).map(item->item.equals(SwitchoverModes.AUTOMATIC)).orElse(true);
					// Units data
					RedundancyControllerUnitStatus
					.parse(bb)
					.forEach(
							uStatus->{

								final int id = uStatus.getId();
								final Optional<YesNo> oConnected = Optional.of(uStatus.getConnected()).filter(con->con.equals(YesNo.YES));
								setStatus("#bpPresentUnit" + id, uStatus.getConnected());
								setStatus("#bpSwitchhowerUnit" + id, oConnected.map(con->uStatus.getSwitchoverAlarm()).map(Alarms::toString).orElse(""));
								setStatus("#bpUnitReady" + id, oConnected.map(con->uStatus.getOperational()).map(YesNo::toString).orElse(""));
								setStatus("#bpRedundancyReady" + id, oConnected.map(con->uStatus.getAlarmName()).map(AlarmSeverityNames::toString).orElse(""));

								Boolean dis1 = true;
								Boolean dis2 = true;
								String button1Name;
								String button2Name;

								final UnitStatusNames statusName = uStatus.getStatusName();
								switch(statusName){
								case ONLINE:
									button1Name = id==1 ? BUTTON_1_NAME : null;
									button2Name = id==2 ? BUTTON_2_NAME : null;

									if(isAutomatic) break;

									dis1 = id==1 ? false : null;
									dis2 = id==2 ? false : null;
									break;
								case STANDBY:
									button1Name = id==1 ? BUTTON_3_NAME : null;
									button2Name = id==2 ? BUTTON_3_NAME : null;

									if(isAutomatic) break;

									dis1 = id==1 ? false : null;
									dis2 = id==2 ? false : null;
									break;
								case PROTECTION_A:
									button1Name = id==3 ? BUTTON_3_NAME : null;
									button2Name = id==3 ? BUTTON_2_NAME : null;

									if(isAutomatic) break;

									dis1 = id==3 ? false : null;
									dis2 = id==3 ? true : null;
									break;
								case PROTECTION_B:
									button1Name = id==3 ? BUTTON_1_NAME : null;
									button2Name = id==3 ? BUTTON_3_NAME : null;

									if(isAutomatic) break;

									dis1 = id==3 ? true : null;
									dis2 = id==3 ? false : null;
									break;
								default:
									button1Name = BUTTON_1_NAME;
									button2Name = BUTTON_2_NAME;
								}

								buttonsStatus(button1Name, dis1, button2Name, dis2);
							});
				});
	}

	private void buttonsStatus(String button1Name, Boolean button1Disable, String button2Name, Boolean button2Disable) {

		Platform.runLater(()->{
			
			Optional.ofNullable(button1Name).ifPresent(btn1::setText);
			Optional.ofNullable(button1Disable).ifPresent(btn1::setDisable);

			Optional.ofNullable(button2Name).ifPresent(btn2::setText);
			Optional.ofNullable(button2Disable).ifPresent(btn2::setDisable);
		});
	}

	private void setStatus(final String nodeName, Object o) {
		Optional
		.ofNullable(apUnitsStatus.lookup(nodeName))
		.filter(BorderPane.class::isInstance)
		.map(BorderPane.class::cast)
		.map(bp->bp.lookup("Text"))
		.map(Text.class::cast)
		.ifPresent(t->t.setText(o.toString()));
	}

	@SuppressWarnings("unchecked")
	private void parseSystemFlags(ByteBuffer bb) {

		Optional
		.of(bb.getInt())
		.map(splitFlags())
		.map(Map::entrySet)
		.map(Set::stream)
		.orElse(Stream.empty())
		.forEach(
				e->{
					final Node node = ControlPanelIrPcFx.this.lookup(e.getKey());
					Optional.ofNullable(node.getUserData()).ifPresent(ud->{
						try {

							final Method method = ((Class<?>)ud).getMethod("parse", Integer.class);
							final Object enumValue = method.invoke(null, e.getValue());

							Platform.runLater(()->{
								
								if(node instanceof BorderPane){
									Optional.ofNullable(node.lookup("Text")).map(Text.class::cast).ifPresent(t->t.setText(enumValue.toString()));

								}else if(node instanceof ChoiceBox){
									Optional.ofNullable(node).map(ChoiceBox.class::cast).map(ChoiceBox::getSelectionModel).ifPresent(
											sm->{
												ChangeListener<?> listener;
												if(enumValue instanceof SwitchoverModes)
													listener = switchoverModesListener;
												else
													listener = standbyModesListener;

												sm.selectedItemProperty().removeListener(listener);
												sm.select(enumValue);
												sm.selectedItemProperty().addListener(listener);
											});
								}
							});

						} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
							logger.catching(e1);
						}
					});
				});
	}

	private Function<Integer, Map<String, Integer>> splitFlags() {

		return flags->{

			Map<String, Integer> map = new HashMap<>();
			map.put("#bpSwitch1Ready", 	flags&BITS_MASK_SW1_READY);
			map.put("#bpSwitch2Ready", 	flags&BITS_MASK_SW2_READY);
			map.put("#bpRedundancyReady",flags&BITS_MASK_REDUNDANCY_READY);
			map.put("#cbStandbyMode", 	flags&BITS_MASK_STANDBY_POWER_MODE);
			map.put("#cbSwitchoverMode", flags&BITS_MASK_SWITCHOVER_MODE);
//			map.put("ALARM", 			flags&BITS_MASK_ALARM);

			return map;
		};
	}

	@Override
	public void run() {
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

		public PacketAbstract getPacket(){
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

		public PacketAbstract getPacket(){
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
	public enum Alarms{
		OK,
		ALARM;

		public static Alarms parse(Integer flag){
			return flag>0 ? ALARM : OK;
		}
	}
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
		};
}
