package irt.tools.button;

import java.awt.Image;
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
import irt.data.MyThreadFactory;
import irt.data.listener.PacketListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.MuteControlPacket;
import irt.data.packet.MuteControlPacket.MuteCommands;
import irt.data.packet.MuteControlPacket.MuteStatus;
import irt.data.packet.Packet;
import irt.data.packet.PacketImp;
import irt.data.packet.interfaces.LinkedPacket;
import irt.irt_gui.IrtGui;
import irt.tools.panel.ConverterPanel;
import irt.tools.panel.PicobucPanel;

public class MuteButton extends ImageButton implements Runnable, PacketListener {
	private static final long serialVersionUID = 4101471002534919184L;

	private ScheduledFuture<?> scheduledFuture;
	private ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(new MyThreadFactory());

	private byte linkAddr = (byte) 254;
											public byte getLinkAddr() {
												return linkAddr;
											}
											public void setLinkAddr(byte linkAddr) {
												this.linkAddr = linkAddr;
												getPacket.setAddr(linkAddr);
												setPacket.setAddr(linkAddr);
											}

	private MuteControlPacket getPacket = new MuteControlPacket(linkAddr, null);
	private MuteControlPacket setPacket = new MuteControlPacket(linkAddr, (byte)0);
	private JLabel lblMute;

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
				.ifPresent(
						c->{
							GuiControllerAbstract.getComPortThreadQueue().removePacketListener(MuteButton.this);
							service.shutdownNow();
						}));

		addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent event) {
				GuiControllerAbstract.getComPortThreadQueue().addPacketListener(MuteButton.this);

				if(scheduledFuture==null || scheduledFuture.isCancelled())
					scheduledFuture = service.scheduleAtFixedRate(MuteButton.this, 1, 5, TimeUnit.SECONDS);
			}
			public void ancestorMoved(AncestorEvent event) { }
			public void ancestorRemoved(AncestorEvent event) {

				if(scheduledFuture!=null && !scheduledFuture.isCancelled())
						scheduledFuture.cancel(true);

				GuiControllerAbstract.getComPortThreadQueue().removePacketListener(MuteButton.this);
			}
		});

		addActionListener(e->GuiControllerAbstract.getComPortThreadQueue().add(setPacket));
	}

	@Override
	public void run() {
		GuiControllerAbstract.getComPortThreadQueue().add(getPacket);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onPacketRecived(Packet packet) {
		Optional
		.ofNullable(packet)
		.map(Packet::getHeader)
		.filter(h->h.getPacketId()==MuteControlPacket.PACKET_ID)
		.filter(h->h.getOption()==PacketImp.ERROR_NO_ERROR)
		.filter(h->h.getGroupId()==MuteControlPacket.GROUP_ID)
		.filter(h->h.getPacketType()==PacketImp.PACKET_TYPE_RESPONSE)
		.map(h->(LinkedPacket)packet)
		.filter(p->Optional.ofNullable(p.getLinkHeader()).map(LinkHeader::getAddr).orElse((byte) 0)==linkAddr)
		.ifPresent(p->{
			MuteControlPacket tmp = new MuteControlPacket(p);
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
		});
	}

	public void setMuteLabel(JLabel lblMute) {
		this.lblMute = lblMute;
		lblMute.addMouseListener(new MouseListener() {
			
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
				GuiControllerAbstract.getComPortThreadQueue().add(setPacket);
			}
		});
	}

}
