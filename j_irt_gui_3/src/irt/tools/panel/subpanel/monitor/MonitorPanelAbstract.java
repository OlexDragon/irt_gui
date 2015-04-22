package irt.tools.panel.subpanel.monitor;

import irt.controller.DefaultController;
import irt.controller.control.ControllerAbstract;
import irt.controller.control.ControllerAbstract.Style;
import irt.controller.interfaces.Refresh;
import irt.controller.serial_port.value.getter.MeasurementGetter;
import irt.controller.translation.Translation;
import irt.data.RundomNumber;
import irt.data.listener.PacketListener;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.packet.PacketHeader;
import irt.data.packet.Payload;

import java.awt.Color;
import java.awt.Font;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

@SuppressWarnings("serial")
public abstract class MonitorPanelAbstract extends JPanel implements Refresh  {

	protected final Logger logger = (Logger) LogManager.getLogger(getClass());

	protected ControllerAbstract controller;
	private LinkHeader linkHeader;
	private ValueChangeListener statusListener;
	protected TitledBorder titledBorder;
	protected String selectedLanguage;

	protected int deviceType;

//	public MonitorPanelAbstract(LinkHeader linkHeader){
//		this(linkHeader, "Monitor", 214, 210);
//	}

	protected MonitorPanelAbstract(final int deviceType, LinkHeader linkHeader, String title, int width, int height) {
		setName("MonitorPanel");
		this.linkHeader = linkHeader!=null ? linkHeader : new LinkHeader((byte)0, (byte)0, (short)0);
		this.deviceType = deviceType;

		selectedLanguage = Translation.getSelectedLanguage();

		addAncestorListener(new AncestorListener() {

			public void ancestorAdded(AncestorEvent arg0) {

				controller = getNewController();
				if (controller != null) {
					if (statusListener != null)
						controller.addStatusListener(statusListener);
					Thread t = new Thread(controller, MonitorPanelAbstract.this.getClass().getSimpleName() + "." + controller.getName() + "-" + new RundomNumber());
					int priority = t.getPriority();
					if (priority > Thread.MIN_PRIORITY)
						t.setPriority(priority - 1);
					t.setDaemon(true);
					t.start();
				}
			}
			public void ancestorMoved(AncestorEvent arg0) {}

			public void ancestorRemoved(AncestorEvent arg0) {
				if(controller!=null && controller.isRun())
					controller.stop();
			}
		});

		setOpaque(false);

		Font font = getTitledBorderFont();

		titledBorder = new TitledBorder(UIManager.getBorder("TitledBorder.border"), title, TitledBorder.LEADING, TitledBorder.TOP, font, Color.WHITE);
		setBorder(titledBorder);
		if(width==0)
			width = 200;
		if(height==0)
			height = 100;
		setSize(width, height);
		setLayout(null);

		addAncestorListener(new AncestorListener() {
			private List<DefaultController> defaultControllers;
			public void ancestorAdded(AncestorEvent event) {
				defaultControllers = getControllers();
			}
			public void ancestorMoved(AncestorEvent event) { }
			public void ancestorRemoved(AncestorEvent event) {
				if(defaultControllers!=null){
					for(Iterator<DefaultController> c=defaultControllers.iterator(); c.hasNext();){
						DefaultController next = c.next();
						next.stop();
					}
					defaultControllers = null;
				}
			}
		});
	}

	protected abstract List<DefaultController> getControllers();

	private Font getTitledBorderFont() {
		return Translation.getFont()
				.deriveFont(Translation.getValue(Integer.class, "titledBorder.font.size", 18))
				.deriveFont(Translation.getValue(Integer.class, "titledBorder.font.type", Font.BOLD));
	}

	protected abstract ControllerAbstract getNewController();

	public LinkHeader getLinkHeader() {
		return linkHeader;
	}

	public void addStatusListener(ValueChangeListener valueChangeListener) {
		statusListener = valueChangeListener;
		if(controller!=null)
			controller.addStatusListener(valueChangeListener);
	}

	public void refresh() {

		selectedLanguage = Translation.getSelectedLanguage();

		Font font = getTitledBorderFont();
		titledBorder.setTitleFont(font);
	}

	protected DefaultController startController(String controllerName, byte parameter, final short packetId) {
		logger.entry(controllerName, parameter, packetId);
		DefaultController defaultController = new DefaultController(
				deviceType,
				controllerName,
				new MeasurementGetter(getLinkHeader(), parameter, packetId), Style.CHECK_ALWAYS){

					@Override
					protected PacketListener getNewPacketListener() {
						return new PacketListener() {

							@Override
							public void packetRecived(final Packet packet) {
								new SwingWorker<Void, Void>(){

									@Override
									protected Void doInBackground() throws Exception {
										PacketHeader header = packet.getHeader();

										if (	getPacketWork().isAddressEquals(packet) &&
												header.getPacketType()==Packet.IRT_SLCP_PACKET_TYPE_RESPONSE &&
												header.getPacketId() == packetId)

											MonitorPanelAbstract.this.packetRecived(packet.getPayloads());
										return null;
									}
								}.execute();
							}
						};
					}
			
		};
		Thread t = new Thread(defaultController, getClass().getSimpleName()+"."+controllerName+"-"+new RundomNumber());
		int priority = t.getPriority();
		if(priority>Thread.MIN_PRIORITY)
			t.setPriority(priority-1);
		t.setDaemon(true);
		t.start();
		return defaultController;
	}

	protected String getOperator(byte flags) {
		String operator;
		switch(flags){
		case 2:
			operator = "<";
			break;
		case 3:
			operator = ">";
			break;
		default:
			operator = "";
		}
		return operator;
	}

	protected abstract void packetRecived(List<Payload> payloads);
}
