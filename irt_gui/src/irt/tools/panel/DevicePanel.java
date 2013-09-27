package irt.tools.panel;

import irt.controller.monitor.MonitorController;
import irt.data.PacketWork;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.PacketListener;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.tools.label.LED;
import irt.tools.label.VarticalLabel;
import irt.tools.panel.head.Panel;
import irt.tools.panel.subpanel.InfoPanel;
import irt.tools.panel.subpanel.control.ControlPanel;
import irt.tools.panel.subpanel.control.ControlPanelUnit;
import irt.tools.panel.subpanel.monitor.MonitorPanel;
import irt.tools.panel.subpanel.monitor.MonitorPanelAbstract;

import java.awt.Color;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

@SuppressWarnings("serial")
public class DevicePanel extends Panel {

	private JLabel clickedLabel;

	private LED led;
	private InfoPanel infoPanel;
	private JTabbedPane tabbedPane;

	private LinkHeader linkHeader;

	private MonitorPanelAbstract monitorPanel;
	private ValueChangeListener statusChangeListener;

	public DevicePanel(LinkHeader linkHeader, String verticalLabelText, int minWidth, int midWidth, int maxWidth, int minHeight, int maxHeight) throws HeadlessException {
		super( verticalLabelText, minWidth, midWidth, maxWidth, minHeight, maxHeight);
		setName("DevicePanel");
		addAncestorListener(new AncestorListener() {

			public void ancestorAdded(AncestorEvent event) {

				monitorPanel = getNewMonitorPanel();
				userPanel.add(monitorPanel);
				monitorPanel.addStatusListener(new ValueChangeListener() {
					
					@Override
					public void valueChanged(ValueChangeEvent valueChangeEvent) {
						if(PacketWork.PACKET_ID_MEASUREMENT_STATUS==valueChangeEvent.getID()){
							Object source = valueChangeEvent.getSource();
							int status;
							if(source instanceof Long)
								status = (int) ((Long) source&(MonitorController.LOCK|MonitorController.MUTE));
							else
								status = (int)source&(MonitorController.LOCK|MonitorController.MUTE);
							switch(status){
							case MonitorController.LOCK:
								led.setLedColor(Color.GREEN);
								verticalLabel.setBackground(Color.GREEN);
								break;
							case MonitorController.MUTE|MonitorController.LOCK:
								led.setLedColor(Color.YELLOW);
								verticalLabel.setBackground(Color.YELLOW);
								break;
							default:
								led.setLedColor(Color.RED);
								verticalLabel.setBackground(Color.RED);
							}
						}
					}
				});

				if(statusChangeListener!=null)
					monitorPanel.addStatusListener(statusChangeListener);
				ControlPanel controlPanel = getNewControlPanel();
				userPanel.add(controlPanel);

				JSlider slider = controlPanel.getSlider();
				slider.setOpaque(false);
				slider.setOrientation(SwingConstants.VERTICAL);
				slider.setBounds(230, 11, 33, 400);
				userPanel.add(slider);
				userPanel.revalidate();
			}
			public void ancestorMoved(AncestorEvent event) {
			}
			public void ancestorRemoved(AncestorEvent event) {
				userPanel.removeAll();
			}
		});

		this.linkHeader = linkHeader;

		led = new LED(new Color(0, 153, 255), null);
		led.setName("Status Led");
		led.setBounds(0, 0, MIN_WIDTH, MIN_HEIGHT);
		led.setOn(true);
		add(led);
		led.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent mouseEvent) {
				if(isMinSize())
					setMidSize();
				else
					setMinSize();
				getParent().getParent().getParent().repaint();
			}
		});

		infoPanel = new InfoPanel(linkHeader);
		infoPanel.setLocation(10, 11);
		extraPanel.add(infoPanel);

		setTabbedPane(extraPanel);
		
	}

	protected void setTabbedPane(JPanel extraPanel) {
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setOpaque(false);
		tabbedPane.setBounds(10, 118, 286, 296);
		extraPanel.add(tabbedPane);
	}

	protected MonitorPanelAbstract getNewMonitorPanel() {
		MonitorPanelAbstract monitorPanel = new MonitorPanel(linkHeader);
		monitorPanel.setLocation(10, 11);
		return monitorPanel;
	}

	protected ControlPanel getNewControlPanel(){
		ControlPanel controlPanel = new ControlPanelUnit(linkHeader);
		controlPanel.setLocation(10, 225);
		return controlPanel;
	}

	public void setLabelForeground(Color labelForeground) {
		verticalLabel.setForeground(labelForeground);
	}

	public void setLabelBackground(Color labelBackground) {
		verticalLabel.setBackground(labelBackground);
	}

	public void setLabelFont(Font font) {
		verticalLabel.setFont(font);
	}

	@Override
	public boolean equals(Object obj) {
		return obj!=null ? obj.hashCode()==hashCode() : false;
	}

	@Override
	public int hashCode() {
		return linkHeader!=null ? linkHeader.getAddr() : -1;
	}

	public JLabel getSource() {
		return clickedLabel;
	}

	public PacketListener getPacketListener() {
		return null;
	}

	public VarticalLabel getVarticalLabel() {
		return verticalLabel;
	}

	protected JTabbedPane getTabbedPane() {
		return tabbedPane;
	}

	public LinkHeader getLinkHeader() {
		return linkHeader;
	}

	public void addStatusChangeListener(ValueChangeListener valueChangeListener){
		if(monitorPanel==null)
			statusChangeListener = valueChangeListener;
		else
			monitorPanel.addStatusListener(valueChangeListener);
	}

	public InfoPanel getInfoPanel() {
		return infoPanel;
	}

	@Override
	public void refresh() {
		super.refresh();
		monitorPanel.refresh();
	}
}
