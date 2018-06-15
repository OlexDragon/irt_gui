
package irt.tools.button;

import java.awt.Cursor;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.swing.ImageIcon;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiControllerAbstract;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.data.MyThreadFactory;
import irt.data.RegisterValue;
import irt.data.listener.PacketListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.PacketAbstract;
import irt.data.packet.PacketImp;
import irt.data.packet.ParameterHeader;
import irt.data.packet.Payload;
import irt.data.packet.denice_debag.CallibrationModePacket;
import irt.data.packet.denice_debag.DeviceDebugPacket;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.interfaces.PacketWork;
import irt.data.value.Value;
import irt.irt_gui.IrtGui;
import irt.tools.CheckBox.SwitchBox;
import irt.tools.panel.ConverterPanel;
import irt.tools.panel.PicobucPanel;

public class Switch extends SwitchBox implements Runnable, PacketListener {
	private final Logger logger = LogManager.getLogger();

	private ScheduledFuture<?> scheduledFuture;
	private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(new MyThreadFactory());

	private PacketAbstract packetToGet;

	private byte linkAddr;
	private short packetId;
	private Byte parameterId;

	private RegisterValue registerValue;

	final ActionListener actionListener = e->{

		if(packetId==PacketWork.PACKET_ID_DEVICE_DEBUG_CALIBRATION_MODE){
			final CallibrationModePacket packet = new CallibrationModePacket(linkAddr, isSelected() ? 1 : 0);
			GuiControllerAbstract.getComPortThreadQueue().add(packet);
			return;
		}

		if(registerValue == null) return;

		registerValue.setValue(new Value(isSelected() ? 3 : 2, 0, 3, 0));
			DeviceDebugPacket packetToSet = new DeviceDebugPacket(linkAddr, registerValue, packetId, parameterId);
			GuiControllerAbstract.getComPortThreadQueue().add(packetToSet);
	};

	public Switch(PacketAbstract packet) {
		super(Optional.ofNullable(IrtGui.class.getResource("/irt/irt_gui/images/switch_off.png")).map(r->new ImageIcon(r).getImage()).orElse(null),
				Optional.ofNullable(IrtGui.class.getResource("/irt/irt_gui/images/switch_on.png")).map(r->new ImageIcon(r).getImage()).orElse(null));

		this.packetToGet = packet;
		setCursor(new Cursor(Cursor.HAND_CURSOR));

		linkAddr = Optional.ofNullable(packet.getLinkHeader()).map(LinkHeader::getAddr).orElse((byte) 0);
		packetId = packet.getHeader().getPacketId();

		Optional
		.ofNullable(packet.getValue())
		.filter(RegisterValue.class::isInstance)
		.map(RegisterValue.class::cast)
		.ifPresent(rv->{

			final Optional<Byte> oParameterId = packet.getPayloads().parallelStream().findAny().map(Payload::getParameterHeader).map(ParameterHeader::getCode);

			if(!oParameterId.isPresent())
				return;

			parameterId = oParameterId.get();
			registerValue = rv;

		});
		

		addHierarchyListener(
				hierarchyEvent->
				Optional
				.of(hierarchyEvent)
				.filter(e->(e.getChangeFlags()&HierarchyEvent.PARENT_CHANGED)!=0)
				.map(HierarchyEvent::getChanged)
				.filter(c->c instanceof ConverterPanel || c instanceof PicobucPanel)
				.filter(c->c.getParent()==null)
				.ifPresent(c->{
					GuiControllerAbstract.getComPortThreadQueue().removePacketListener(Switch.this);
					service.shutdownNow();
				}));

		addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent event) {
				if(scheduledFuture==null || scheduledFuture.isCancelled())
					scheduledFuture = service.scheduleAtFixedRate(Switch.this, 1, 3, TimeUnit.SECONDS);
				GuiControllerAbstract.getComPortThreadQueue().addPacketListener(Switch.this);
			}
			public void ancestorRemoved(AncestorEvent event) {
				GuiControllerAbstract.getComPortThreadQueue().removePacketListener(Switch.this);
				if(scheduledFuture!=null && !scheduledFuture.isCancelled())
					scheduledFuture.cancel(true);
			}
			public void ancestorMoved(AncestorEvent event) { }
		});

		addActionListener(actionListener);
	}

	private static final long serialVersionUID = 8018058982771868859L;

	@Override
	public void onPacketRecived(Packet packet) {

		Optional
		.ofNullable(packet)
		.map(p->p.getHeader())
		.filter(h->h.getPacketId()==packetToGet.getHeader().getPacketId())
		.filter(h->h.getPacketType()==PacketImp.PACKET_TYPE_RESPONSE)
		.ifPresent(h->{

			if(h.getOption()!=PacketImp.ERROR_NO_ERROR){
				logger.warn("packet has error: {}", packet);
				return;
			}

			Boolean isSelected;
			if(h.getPacketId()==PacketWork.PACKET_ID_DEVICE_DEBUG_CALIBRATION_MODE){

				isSelected = Optional
								.ofNullable(packet.getPayloads())
								.map(List::stream)
								.orElse(Stream.empty())
								.map(pl->pl.getInt(0))
								.map(i->i>0)
								.findAny()
								.orElse(false);
						
			}else{

				isSelected = packet
									.getPayloads()
									.parallelStream()
									.map(pl->pl.getRegisterValue())
									.map(rv->rv.getValue())
									.map(v->v.getValue())
									.map(l->(l&1)>0)
									.findAny()
									.orElse(false);
			}

			if(isSelected()!=isSelected){
				removeActionListener(actionListener);
				setSelected(isSelected);
				addActionListener(actionListener);
			}
		});
	}

	private int delay;
	@Override
	public void run() {

		final ComPortThreadQueue queue = GuiControllerAbstract.getComPortThreadQueue();
		final int size = queue.size();

		if(size>ComPortThreadQueue.QUEUE_SIZE_TO_DELAY && delay<=0)
			delay = ComPortThreadQueue.DELAY_TIMES;

		if(delay<=0)
			GuiControllerAbstract.getComPortThreadQueue().add(packetToGet);
		else
			delay--;
	}

}
