package irt.tools;

import java.awt.event.HierarchyEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.JCheckBox;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiControllerAbstract;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.data.MyThreadFactory;
import irt.data.listener.PacketListener;
import irt.data.packet.ALCEnablePacket;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.packet.PacketImp;
import irt.data.packet.interfaces.LinkedPacket;
import irt.data.packet.interfaces.PacketWork;
import irt.tools.panel.ConverterPanel;
import irt.tools.panel.PicobucPanel;

public class ALCComboBox extends JCheckBox implements Runnable, PacketListener{
	private static final long serialVersionUID = 5927791917430153433L;

	protected final Logger logger = LogManager.getLogger();

	private final 	ComPortThreadQueue 			cptq 					= GuiControllerAbstract.getComPortThreadQueue();
	private	final 	ScheduledExecutorService 	service 	= Executors.newScheduledThreadPool(1, new MyThreadFactory());
	private 		ScheduledFuture<?> 			scheduleAtFixedRate;

	private final ALCEnablePacket packet;

	private final Byte unitAddress; 

		public ALCComboBox(String text, final Byte linkAddr) {
		super(text);

		addHierarchyListener(
				hierarchyEvent->
				Optional
				.of(hierarchyEvent)
				.filter(e->(e.getChangeFlags()&HierarchyEvent.PARENT_CHANGED)!=0)
				.map(HierarchyEvent::getChanged)
				.filter(c->c instanceof ConverterPanel || c instanceof PicobucPanel)
				.filter(c->c.getParent()==null)
				.ifPresent(
						c->{
							cptq.removePacketListener(ALCComboBox.this);
							service.shutdownNow();
						}));

		this.unitAddress = linkAddr;
		packet = new ALCEnablePacket(linkAddr, null); 

		addAncestorListener(new AncestorListener() {

			@Override
			public void ancestorAdded(AncestorEvent event) {
				cptq.addPacketListener(ALCComboBox.this);
				scheduleAtFixedRate = service.scheduleAtFixedRate(ALCComboBox.this, 1, 3000, TimeUnit.MILLISECONDS);
			}

			@Override
			public void ancestorRemoved(AncestorEvent event) {
				scheduleAtFixedRate.cancel(true);
				removeAncestorListener(this);
				cptq.removePacketListener(ALCComboBox.this);
			}
			@Override public void ancestorMoved(AncestorEvent event) { }
		});
		addItemListener(checkBoxItemListener);
	}

	@Override
	public void run() {
		try{

			logger.trace(packet);
			cptq.add(packet);

		}catch(Exception e){
			logger.catching(e);
		}
	}

	@Override
	public void onPacketRecived(Packet packet) {
		try{

			final Optional<Packet> o = Optional
			.ofNullable(packet);

			if(!o.isPresent())
				return;

			byte addr = o.filter(LinkedPacket.class::isInstance).map(LinkedPacket.class::cast).map(LinkedPacket::getLinkHeader).map(LinkHeader::getAddr).orElse((byte) 0);

			if(addr!=unitAddress)
				return;

			o.map(Packet::getHeader)
			.filter(h->h.getPacketType()==PacketImp.PACKET_TYPE_RESPONSE)
			.filter(h->h.getOption()==PacketImp.ERROR_NO_ERROR)
			.filter(h->h.getGroupId()==PacketImp.GROUP_ID_CONFIGURATION)
			.map(h->packet.getPayloads())
			.map(pls->pls.parallelStream())
			.ifPresent(
					stream->{
						stream
						.forEach(pl->{

							final byte code = pl.getParameterHeader().getCode();

							switch(code){
							case PacketImp.PARAMETER_CONFIG_BUC_APC_ENABLE:
								setSelected((pl.getByte()==1));
							}
						});
					});
		}catch(Exception e){
			logger.catching(e);
		}
	}

	final ItemListener checkBoxItemListener = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {

			PacketWork packet = new ALCEnablePacket(unitAddress, (byte) (e.getStateChange() == ItemEvent.DESELECTED ? 0 : 1));
			cptq.add(packet);
		}
	};
}
