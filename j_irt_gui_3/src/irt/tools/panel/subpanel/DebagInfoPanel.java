package irt.tools.panel.subpanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.HierarchyEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import irt.controller.GuiControllerAbstract;
import irt.data.DeviceInfo.DeviceType;
import irt.data.MyThreadFactory;
import irt.data.StringData;
import irt.data.listener.PacketListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.Payload;
import irt.data.packet.PacketWork.PacketIDs;
import irt.data.packet.denice_debag.DeviceDebugInfoPacket;
import irt.data.packet.interfaces.Packet;
import irt.tools.panel.ConverterPanel;
import irt.tools.panel.PicobucPanel;

@SuppressWarnings("serial")
public class DebagInfoPanel extends JPanel implements Runnable, PacketListener {
	private JTextArea textArea;
	private JComboBox<String> cbParameterCode;
	private JComboBox<Integer> cbParameter;

	private ScheduledFuture<?> scheduleAtFixedRate;
	private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(new MyThreadFactory());
	private DeviceDebugInfoPacket packetToSend;
	private Timer timer;

	public DebagInfoPanel(final Optional<DeviceType> deviceType, LinkHeader linkHeader, JPanel panel) {

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
							GuiControllerAbstract.getComPortThreadQueue().removePacketListener(DebagInfoPanel.this);
							service.shutdownNow();
						}));

		addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent arg0) {
				GuiControllerAbstract.getComPortThreadQueue().addPacketListener(DebagInfoPanel.this);

				if(!service.isShutdown() && (scheduleAtFixedRate==null || scheduleAtFixedRate.isCancelled()))
					scheduleAtFixedRate = service.scheduleAtFixedRate(DebagInfoPanel.this, 1, 10, TimeUnit.SECONDS);

			}
			public void ancestorRemoved(AncestorEvent arg0) {
				GuiControllerAbstract.getComPortThreadQueue().removePacketListener(DebagInfoPanel.this);

				if(scheduleAtFixedRate!=null && !scheduleAtFixedRate.isCancelled())
					scheduleAtFixedRate.cancel(true);
			}
			public void ancestorMoved(AncestorEvent arg0) { }
		});

		packetToSend = new DeviceDebugInfoPacket(Optional.ofNullable(linkHeader).map(LinkHeader::getAddr).orElse((byte)0), (byte) 0);
		setLayout(new BorderLayout(0, 0));

		textArea = new JTextArea();
		textArea.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2)
					restart();
			}
		});
		JScrollPane scrollPane = new JScrollPane(textArea);
		add(scrollPane, BorderLayout.CENTER);

		panel = new JPanel();
		add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		cbParameterCode = new JComboBox<String>();
		cbParameterCode.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				if(itemEvent.getStateChange()==ItemEvent.SELECTED){
					cbParameterCode.setToolTipText(cbParameterCode.getSelectedItem().toString());
					packetToSend.setParameterCode((byte)(cbParameterCode.getSelectedIndex()+1));
					restart();
				}
			}
		});
		cbParameterCode.addItem("device information: parts, firmware and etc.");
		cbParameterCode.addItem("dump of registers for specified device index ");
		panel.add(cbParameterCode, BorderLayout.CENTER);

		cbParameter = new JComboBox<Integer>();
		cbParameter.setMaximumRowCount(9);
		cbParameter.setPreferredSize(new Dimension(50, 20));
		cbParameter.setEditable(true);
		for(int i=0; i<7; i++)
			cbParameter.addItem(i);
		cbParameter.addItem(10);
		cbParameter.addItem(100);
		panel.add(cbParameter, BorderLayout.EAST);

		cbParameter.addItemListener(itemEvent->{

			if(itemEvent.getStateChange()==ItemEvent.SELECTED){
				packetToSend.setValue(cbParameter.getSelectedItem());
				restart();
			}
		});
	}

	public void restart() {

		if(scheduleAtFixedRate!=null && !scheduleAtFixedRate.isCancelled())
			scheduleAtFixedRate.cancel(true);

		scheduleAtFixedRate = service.scheduleAtFixedRate(DebagInfoPanel.this, 0, 10, TimeUnit.SECONDS);
	}


	@Override
	public void onPacketRecived(Packet packet) {

		new MyThreadFactory(()->{

			Optional
			.ofNullable(packet)
			.map(p->p.getHeader())
			.filter(h->PacketIDs.DEVICE_DEBUG_INFO.match(h.getPacketId()))
			.ifPresent(h->{

				String text;
				if(h.getPacketType()!=PacketImp.PACKET_TYPE_RESPONSE){

					setBorder(new LineBorder(Color.PINK));
					text = "No Communication";

				}else if(h.getOption()!=PacketImp.ERROR_NO_ERROR){

					setBorder(new LineBorder(Color.RED));
					text = "ERROR: " + h.getOptionStr();

				}else{

					setBorder(new LineBorder(Color.YELLOW));
					text = Optional.ofNullable(packet.getPayloads()).map(pls->pls.parallelStream()).orElse(Stream.empty()).findAny().map(Payload::getStringData).map(StringData::toString).orElse("No data.");
				}

				if(!text.equals(textArea.getText()))
					textArea.setText(text);

				if(timer!=null && timer.isRunning())
					return;

				timer = new Timer(1000, e->{setBorder(null); timer.stop();});
				timer.start();
			});
		});
	}

	@Override
	public void run() {
		GuiControllerAbstract.getComPortThreadQueue().add(packetToSend);
	}
}
