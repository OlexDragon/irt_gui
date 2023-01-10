package irt.tools.button;

import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import irt.controller.GuiControllerAbstract;
import irt.controller.translation.Translation;
import irt.data.ThreadWorker;
import irt.data.listener.PacketListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketID;
import irt.data.packet.configuration.MuteControlPacket;
import irt.data.packet.configuration.MuteControlPacket.MuteCommands;
import irt.data.packet.configuration.MuteControlPacket.MuteStatus;
import irt.data.packet.interfaces.LinkedPacket;
import irt.data.packet.interfaces.Packet;
import irt.irt_gui.IrtGui;
import irt.tools.panel.ConverterPanel;
import irt.tools.panel.PicobucPanel;

public class MuteButton extends ImageButton implements Runnable, PacketListener {
	private static final long serialVersionUID = 4101471002534919184L;

	private ScheduledFuture<?> scheduledFuture;
	private ScheduledExecutorService service;

	private Byte linkAddr;
											public byte getLinkAddr() {
												return linkAddr;
											}
											public void setLinkAddr(byte linkAddr) {

												this.linkAddr = linkAddr;
												getPacket.setAddr(linkAddr);
												setPacket.setAddr(linkAddr);
											}

	private MuteControlPacket getPacket = new MuteControlPacket(linkAddr, null);
	private MuteControlPacket setPacket = new MuteControlPacket(linkAddr, MuteCommands.MUTE);
	private JLabel lblMute;

	final ActionListener actionListener = e->GuiControllerAbstract.getComPortThreadQueue().add(setPacket);

	public MuteButton() {
		this(new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/power-red.png")).getImage());
	}

	public MuteButton(Image image) {
		super(image);

		String muteText = Translation.getValue(String.class, "mute", "MUTE");
		setToolTipText(muteText);

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

				if(!Optional.ofNullable(service).filter(s->!s.isShutdown()).isPresent())
					service =  Executors.newSingleThreadScheduledExecutor(new ThreadWorker("MuteButton"));

				GuiControllerAbstract.getComPortThreadQueue().addPacketListener(MuteButton.this);

				if(scheduledFuture==null || scheduledFuture.isDone())
					scheduledFuture = service.scheduleAtFixedRate(MuteButton.this, 1, 5, TimeUnit.SECONDS);
			}
			public void ancestorMoved(AncestorEvent event) { }
			public void ancestorRemoved(AncestorEvent event) {
				stop();
			}
		});

		addActionListener(actionListener);
	}

	private void stop() {
		GuiControllerAbstract.getComPortThreadQueue().removePacketListener(MuteButton.this);
		Optional.of(scheduledFuture).filter(s->!s.isDone()).ifPresent(s->s.cancel(true));
		Optional.of(service).filter(s->!s.isShutdown()).ifPresent(ScheduledExecutorService::shutdownNow);
	}

	@Override
	public void run() {

		if(linkAddr==null)
			return;

		GuiControllerAbstract.getComPortThreadQueue().add(getPacket);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onPacketReceived(Packet packet) {

		new ThreadWorker(()->{

			final Optional<Packet> oPacket = Optional
			.ofNullable(packet);

			oPacket
			.map(Packet::getHeader)
			.filter(h->PacketID.CONFIGURATION_MUTE.match(h.getPacketId()))
			.filter(h->h.getError()==PacketImp.ERROR_NO_ERROR)
			.filter(h->MuteControlPacket.GROUP_ID.match(h.getGroupId()))
			.filter(h->h.getPacketType()==PacketImp.PACKET_TYPE_RESPONSE)
			.ifPresent(h->{

				final Byte addr = oPacket

						.filter(LinkedPacket.class::isInstance)
						.map(LinkedPacket.class::cast)
						.map(LinkedPacket::getLinkHeader)
						.map(LinkHeader::getAddr)
						.orElse((byte) 0);
				if(addr==linkAddr){
					
					MuteControlPacket tmp = new MuteControlPacket(packet);
					((Optional<MuteStatus>) tmp.getValue())
					.ifPresent(ms->{

						final MuteCommands command = ms.getCommand();
						setPacket.setValue(command);

						String muteText = Translation.getValue(String.class, command.name(), "MUTE");
						final String toolTipText = getToolTipText();

						if(!toolTipText.equals(muteText)){
							setToolTipText(muteText);
							Optional.ofNullable(lblMute).ifPresent(ml->ml.setText(muteText));
						}
					});
				}
			});
		}, "MuteButton.onPacketReceived");
	}

	public void setMuteLabel(JLabel lblMute) {
		this.lblMute = lblMute;
		lblMute.addMouseListener(new MouseListener() {
			
			@Override public void mouseReleased	(MouseEvent e) { }
			@Override public void mousePressed	(MouseEvent e) { }
			@Override public void mouseExited	(MouseEvent e) { }
			@Override public void mouseEntered	(MouseEvent e) { }
			
			@Override
			public void mouseClicked(MouseEvent e) {
				actionListener.actionPerformed(null);
			}
		});
	}

}
