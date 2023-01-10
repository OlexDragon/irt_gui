
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
import irt.data.ThreadWorker;
import irt.data.listener.PacketListener;
import irt.data.packet.DeviceDebugPacketIds;
import irt.data.packet.LinkHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketSuper;
import irt.data.packet.PacketID;
import irt.data.packet.denice_debag.CallibrationModePacket;
import irt.data.packet.denice_debag.DeviceDebugPacket;
import irt.data.packet.interfaces.Packet;
import irt.data.value.Value;
import irt.irt_gui.IrtGui;
import irt.tools.CheckBox.SwitchBox;
import irt.tools.panel.ConverterPanel;
import irt.tools.panel.PicobucPanel;

public class Switch extends SwitchBox implements Runnable, PacketListener {
	private static final long serialVersionUID = 8018058982771868859L;
	private final Logger logger = LogManager.getLogger();

	private ScheduledFuture<?> scheduledFuture;
	private ScheduledExecutorService service;

	private PacketSuper packetToGet;

	private byte linkAddr;
	private PacketID packetId;

	final ActionListener actionListener = e->{

		if(PacketID.DEVICE_DEBUG_CALIBRATION_MODE.equals(packetId)){
			final CallibrationModePacket packet = new CallibrationModePacket(linkAddr, isSelected());
			GuiControllerAbstract.getComPortThreadQueue().add(packet);
			return;
		}

		DeviceDebugPacketIds
		.valueOf(packetId)
		.ifPresent(deviceDebugPacketId->{
			
			DeviceDebugPacket packetToSet = new DeviceDebugPacket(linkAddr, new Value(isSelected() ? 1 : 0, 0, 3, 0), deviceDebugPacketId);
			GuiControllerAbstract.getComPortThreadQueue().add(packetToSet);
		});
	};

	public Switch(PacketSuper packet) {
		super(Optional.ofNullable(IrtGui.class.getResource("/irt/irt_gui/images/switch_off.png")).map(ImageIcon::new).map(ImageIcon::getImage).orElse(null),
				Optional.ofNullable(IrtGui.class.getResource("/irt/irt_gui/images/switch_on.png")).map(ImageIcon::new).map(ImageIcon::getImage).orElse(null));

		this.packetToGet = packet;
		setCursor(new Cursor(Cursor.HAND_CURSOR));

		linkAddr = Optional.ofNullable(packet.getLinkHeader()).map(LinkHeader::getAddr).orElse((byte) 0);
		PacketID[] values = PacketID.values();

		final int intId = packet.getHeader().getPacketId()&0xFF;
		packetId = Optional.of(intId).filter(i->i<values.length).map(i->values[i]).orElse(PacketID.UNNECESSARY);
		

		addHierarchyListener(
				hierarchyEvent->
				Optional
				.of(hierarchyEvent)
				.filter(e->(e.getChangeFlags()&HierarchyEvent.PARENT_CHANGED)!=0)
				.map(HierarchyEvent::getChanged)
				.filter(c->c instanceof ConverterPanel || c instanceof PicobucPanel)
				.filter(c->c.getParent()==null)
				.ifPresent(c->stop()));

		addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent event) {
				if(Optional.ofNullable(scheduledFuture).filter(s->!s.isDone()).isPresent())
					return;

				if(!Optional.ofNullable(service).filter(s->!s.isShutdown()).isPresent())
					service = Executors.newSingleThreadScheduledExecutor(new ThreadWorker("Switch"));

				GuiControllerAbstract.getComPortThreadQueue().addPacketListener(Switch.this);
				scheduledFuture = service.scheduleAtFixedRate(Switch.this, 1, 10, TimeUnit.SECONDS);
			}
			public void ancestorRemoved(AncestorEvent event) {
				stop();
			}
			public void ancestorMoved(AncestorEvent event) { }
		});

		addActionListener(actionListener);
	}

	private void stop() {
		GuiControllerAbstract.getComPortThreadQueue().removePacketListener(Switch.this);
		Optional.ofNullable(scheduledFuture).filter(s->!s.isDone()).ifPresent(s->s.cancel(true));
		Optional.ofNullable(service).filter(s->!s.isShutdown()).ifPresent(ScheduledExecutorService::shutdownNow);
	}

	@Override
	public void onPacketReceived(Packet packet) {

		new ThreadWorker(()->{

			Optional
			.ofNullable(packet)
			.map(p->p.getHeader())
			.filter(h->h.getPacketId()==packetToGet.getHeader().getPacketId())
			.filter(h->h.getPacketType()==PacketImp.PACKET_TYPE_RESPONSE)
			.ifPresent(h->{

				if(h.getError()!=PacketImp.ERROR_NO_ERROR){
					if(h.getError()==PacketImp.ERROR_FUNCTION_NOT_IMPLEMENTED) {
						stop();
						setEnabled(false);
					}
					logger.warn("packet has error: {}", packet);
					getParent().remove(this);
					return;
				}

				Boolean isSelected;
				if(PacketID.DEVICE_DEBUG_CALIBRATION_MODE.match(h.getPacketId())){

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
//				logger.error("{} : {} : {}", isSelected, Switch.this.getName(), packet);
			});
		}, "Switch.onPacketReceived()");
	}

	private int delay;
	@Override
	public void run() {

//		logger.error("{} : {}", getName(), packetToGet);
		final ComPortThreadQueue queue = GuiControllerAbstract.getComPortThreadQueue();
		final int size = queue.size();

		if(size>ComPortThreadQueue.QUEUE_SIZE_TO_DELAY && delay<=0)
			delay = ComPortThreadQueue.DELAY_TIMES;

		if(delay<=ComPortThreadQueue.QUEUE_SIZE_TO_RESUME)
			GuiControllerAbstract.getComPortThreadQueue().add(packetToGet);
		else
			delay--;
	}

}
