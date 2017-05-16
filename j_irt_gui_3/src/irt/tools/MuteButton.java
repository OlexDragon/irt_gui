package irt.tools;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.JLabel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiControllerAbstract;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.controller.translation.Translation;
import irt.data.MyThreadFactory;
import irt.data.listener.PacketListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.MuteControlPacket;
import irt.data.packet.Packet;
import irt.data.packet.PacketImp;
import irt.data.packet.interfaces.LinkedPacket;
import irt.data.packet.interfaces.PacketWork;
import irt.tools.button.ImageButton;

public class MuteButton extends ImageButton implements Runnable, PacketListener{
	private static final long serialVersionUID = -2275767848687769406L;

	protected final Logger logger = LogManager.getLogger();

	private final 	ComPortThreadQueue 			cptq 					= GuiControllerAbstract.getComPortThreadQueue();
	private	final 	ScheduledExecutorService 	service 	= Executors.newScheduledThreadPool(1, new MyThreadFactory());
	private 		ScheduledFuture<?> 			scheduleAtFixedRate;
	private final 	List<JLabel> 				labels 					= new ArrayList<>(); 

	private final 		PacketWork 	packet;
	private volatile	MuteStatus 	muteStatus;
	private 			Byte 		unitAddress;

	public MuteButton(Byte linkAddr, Image image) {
		super(image);
		addHierarchyListener(new HierarchyListener() {
			public void hierarchyChanged(HierarchyEvent e) {
				if((e.getChangeFlags()&HierarchyEvent.PARENT_CHANGED)==HierarchyEvent.PARENT_CHANGED && e.getComponent().getParent()==null)
					service.shutdownNow();
			}
		});

		this.unitAddress = linkAddr;
		packet = new MuteControlPacket(linkAddr, null);

		addAncestorListener(new AncestorListener() {

			@Override
			public void ancestorAdded(AncestorEvent event) {
				cptq.addPacketListener(MuteButton.this);
				scheduleAtFixedRate = service.scheduleAtFixedRate(MuteButton.this, 1, 30000, TimeUnit.MILLISECONDS);
			}

			@Override
			public void ancestorRemoved(AncestorEvent event) {
				scheduleAtFixedRate.cancel(true);
				removeAncestorListener(this);
				cptq.removePacketListener(MuteButton.this);
			}
			@Override public void ancestorMoved(AncestorEvent event) { }
		});
		addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try{
					sendCommand();
				}catch(Exception ex){
					logger.catching(ex);
				}
			}
		});
	}

	@Override
	public void run() {
		try{
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
			.ifPresent(stream->{
				stream
				.forEach(pl->{

					final byte code = pl.getParameterHeader().getCode();

					switch(code){
					case PacketImp.PARAMETER_ID_CONFIGURATION_MUTE:
						final byte index = pl.getByte();

						MuteStatus muteStatus = MuteStatus.values()[index];
						if(this.muteStatus==null || this.muteStatus!=muteStatus){
							this.muteStatus = muteStatus;

							final String name = muteStatus.name();
							final String text = Translation.getValue(String.class, MuteStatus.values()[muteStatus.ordinal()^1].name(), muteStatus==MuteStatus.MUTED ? "Unmute" : "Mute");

							setName(name);
							setToolTipText(text);

							for(JLabel l:labels){
								l.setName(name);
								l.setText(text);
							}
						}
					}
				});
			});

		}catch(Exception e){
			logger.catching(e);
		}
	}

	public void addLabel(JLabel label){
		labels.add(label);
		label.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				try{
					sendCommand();
				}catch(Exception ex){
					logger.catching(ex);
				}
			}
		});
	}
	private void sendCommand() {
		if(muteStatus!=null){
			final MuteControlPacket packet = new MuteControlPacket(unitAddress, (byte)(muteStatus.ordinal()^1));
			cptq.add(packet);
		}
	}
	public enum MuteStatus{
		MUTED,
		UNMUTED
	}
}
