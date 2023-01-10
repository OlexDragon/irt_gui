package irt.tools.panel.subpanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.HierarchyEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import irt.controller.GuiControllerAbstract;
import irt.data.ThreadWorker;
import irt.data.DeviceType;
import irt.data.StringData;
import irt.data.listener.PacketListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketID;
import irt.data.packet.Payload;
import irt.data.packet.denice_debag.DeviceDebugInfoPacket;
import irt.data.packet.interfaces.Packet;
import irt.tools.fx.HelpPaneFx;
import irt.tools.fx.JavaFxFrame;
import irt.tools.panel.ConverterPanel;
import irt.tools.panel.PicobucPanel;

@SuppressWarnings("serial")
public class DebagInfoPanel extends JPanel implements Runnable, PacketListener {

	private JTextArea textArea;
	private JComboBox<String> cbParameterCode;
	private static JComboBox<Integer> cbParameter;

	private ScheduledFuture<?> scheduleAtFixedRate;
	private ScheduledExecutorService service;
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
				.ifPresent(c->stop()));

		addAncestorListener(new AncestorListener() {

			public void ancestorAdded(AncestorEvent arg0) {
				start();
			}

			public void ancestorRemoved(AncestorEvent arg0) {
				stop();
			}
			public void ancestorMoved(AncestorEvent arg0) { }
		});

		byte linkAddr = Optional.ofNullable(linkHeader).map(LinkHeader::getAddr).orElse((byte)0);
		packetToSend = new DeviceDebugInfoPacket(linkAddr, (byte) 1);
		setLayout(new BorderLayout(0, 0));

		textArea = new JTextArea();
		textArea.setToolTipText("<html>CTRL & click - send request packet<br>SHIFT & click - Help Panel</html>");
		textArea.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {

				if(e.isShiftDown()) {

					JavaFxFrame frame = HelpPaneFx.getHelpFrame(linkAddr);
					frame.setVisible(true);
					return;
				}

				if(e.isControlDown())
					reset();
			}
		});
		JScrollPane scrollPane = new JScrollPane(textArea);
		add(scrollPane, BorderLayout.CENTER);

		panel = new JPanel();
		add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		cbParameterCode = new JComboBox<String>();
		cbParameterCode.addItem("device information: parts, firmware and etc.");
		cbParameterCode.addItem("dump of registers for specified device index ");
		panel.add(cbParameterCode, BorderLayout.CENTER);

		cbParameterCode
		.addItemListener(

				itemEvent->{
					if(itemEvent.getStateChange()==ItemEvent.SELECTED){
						cbParameterCode.setToolTipText(cbParameterCode.getSelectedItem().toString());
						packetToSend.setParameterCode((byte)(cbParameterCode.getSelectedIndex()+1));
						reset();
				}
		});

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
				reset();
			}
		});
	}

	private synchronized void start() {

		if(Optional.ofNullable(scheduleAtFixedRate).filter(s->!s.isDone()).isPresent())
			return;

		startService();

		GuiControllerAbstract.getComPortThreadQueue().addPacketListener(DebagInfoPanel.this);
		scheduleAtFixedRate = service.scheduleAtFixedRate(DebagInfoPanel.this, 0, 3, TimeUnit.SECONDS);
	}

	private synchronized void stop() {
		GuiControllerAbstract.getComPortThreadQueue().removePacketListener(DebagInfoPanel.this);
		Optional.ofNullable(scheduleAtFixedRate).filter(s->!s.isDone()).ifPresent(s->s.cancel(true));
		Optional.ofNullable(service).filter(s->!s.isShutdown()).ifPresent(ScheduledExecutorService::shutdownNow);
	}

	private void reset() {

		startService();

		Optional.ofNullable(scheduleAtFixedRate).filter(s->!s.isDone()).ifPresent(s->s.cancel(true));
		scheduleAtFixedRate = service.scheduleAtFixedRate(DebagInfoPanel.this, 1, 3, TimeUnit.SECONDS);
	}

	private void startService() {
		if(!Optional.ofNullable(service).filter(s->!s.isShutdown()).isPresent())
			service = Executors.newSingleThreadScheduledExecutor(new ThreadWorker("DebagInfoPanel.service"));
	}


	@Override
	public void onPacketReceived(Packet packet) {

		new ThreadWorker(()->{

			Optional
			.ofNullable(packet)
			.map(p->p.getHeader())
			.filter(h->PacketID.DEVICE_DEBUG_INFO.match(h.getPacketId()))
			.ifPresent(h->{

				String text;
				if(h.getPacketType()!=PacketImp.PACKET_TYPE_RESPONSE){

					setBorder(new LineBorder(Color.PINK));
					text = "No Communication";

				}else if(h.getError()!=PacketImp.ERROR_NO_ERROR){

					setBorder(new LineBorder(Color.RED));
					text = "ERROR: " + h.getErrorStr();

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
		}, "DebagInfoPanel.onPacketReceived()");
	}

	@Override
	public void run() {
		GuiControllerAbstract.getComPortThreadQueue().add(packetToSend);
	}

	public static void setIndexes(int[] dumpIndexes, int[] deviceIndexes) {
		if(cbParameter!=null)
		ThreadWorker.runThread(
				()->{
					final Integer[] concat = IntStream.concat(Arrays.stream(dumpIndexes), Arrays.stream(deviceIndexes)).distinct().sorted().mapToObj(Integer::new).toArray(Integer[]::new);
					DefaultComboBoxModel<Integer> model = new DefaultComboBoxModel<>( concat );
					cbParameter.setModel( model );
				}, "setIndexes");
	}
}
