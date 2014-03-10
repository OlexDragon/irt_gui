package irt.tools.panel;

import irt.controller.GuiController;
import irt.data.DeviceInfo;
import irt.data.listener.PacketListener;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.tools.panel.head.Panel;
import irt.tools.panel.subpanel.DebugPanel;
import irt.tools.panel.subpanel.InfoPanel;
import irt.tools.panel.subpanel.control.ControlPanel;
import irt.tools.panel.subpanel.control.ControlPanelSSPA;
import irt.tools.panel.subpanel.control.ControlPanelUnit;
import irt.tools.panel.subpanel.monitor.MonitorPanel;
import irt.tools.panel.subpanel.monitor.MonitorPanelAbstract;
import irt.tools.panel.subpanel.monitor.MonitorPanelSSPA;

import java.awt.HeadlessException;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.prefs.Preferences;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

@SuppressWarnings("serial")
public class DevicePanel extends Panel implements Comparable<DevicePanel>{

	public static final DebugPanel DEBUG_PANEL = new DebugPanel();

	protected final Logger logger = (Logger) LogManager.getLogger(getClass());

	protected final String selectedTab = "selected_tab_"+getClass().getSimpleName();

	private JLabel clickedLabel;
	private InfoPanel infoPanel;
	private JTabbedPane tabbedPane;

	private LinkHeader linkHeader;

	protected final Preferences pref = GuiController.getPrefs();


	private MonitorPanelAbstract monitorPanel;
	public MonitorPanelAbstract getMonitorPanel() {
		return monitorPanel;
	}

	private ControlPanel controlPanel;
	private ValueChangeListener statusChangeListener;

	private int deviceType;

	public DevicePanel(LinkHeader linkHeader, DeviceInfo deviceInfo, int minWidth, int midWidth, int maxWidth, int minHeight, int maxHeight) throws HeadlessException {
		super( deviceInfo!=null ? "("+deviceInfo.getSerialNumber()+") "+deviceInfo.getUnitName() : null, minWidth, midWidth, maxWidth, minHeight, maxHeight);
		setBorder(null);
		setName("DevicePanel");
		lblAddress.setText(lblAddress.getText()+(linkHeader!=null ? (linkHeader.getAddr()&0xFF) : "N/A"));
		if(deviceInfo!=null)
			this.deviceType = deviceInfo.getType();
		addAncestorListener(new AncestorListener() {

			public void ancestorAdded(AncestorEvent event) {

				monitorPanel = getNewMonitorPanel();
				userPanel.add(monitorPanel);

				if(statusChangeListener!=null)
					monitorPanel.addStatusListener(statusChangeListener);
				controlPanel = getNewControlPanel();
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

		this.linkHeader = linkHeader!=null ? linkHeader : new LinkHeader((byte)0, (byte)0, (short) 0);
		DEBUG_PANEL.setLinkHeader(linkHeader);

		infoPanel = new InfoPanel(linkHeader);
		infoPanel.setLocation(10, 11);
		extraPanel.add(infoPanel);

		setTabbedPane(extraPanel);
		
	}

	public ControlPanel getControlPanel() {
		return controlPanel;
	}

	protected void setTabbedPane(JPanel extraPanel) {
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addContainerListener(new ContainerAdapter() {
			@Override
			public void componentAdded(ContainerEvent e) {

				new SwingWorker<Void, Void>() {

					@Override
					protected Void doInBackground() throws Exception {
						int select = pref.getInt(selectedTab, 0);
						int tabCount = tabbedPane.getTabCount();

						if (tabbedPane.getSelectedIndex() != select && tabCount != 0 && tabCount > select) {
							tabbedPane.setSelectedIndex(select);
							logger.trace("selectedTab={}, select={}, tabCount={}", selectedTab, select, tabCount);
						}
						return null;
					}

				}.execute();
			}
		});
		tabbedPane.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				int selectedIndex = tabbedPane.getSelectedIndex();
				pref.putInt(selectedTab, selectedIndex);

				logger.trace("selectedTab={}, selectedIndex={}", selectedTab, selectedIndex);
			}
		});
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tabbedPane.setOpaque(false);
		tabbedPane.setBounds(10, 118, 286, 296);
		extraPanel.add(tabbedPane);
	}

	protected MonitorPanelAbstract getNewMonitorPanel() {
		MonitorPanelAbstract monitorPanel = deviceType==DeviceInfo.DEVICE_TYPE_SSPA ? new MonitorPanelSSPA(linkHeader) : new MonitorPanel(linkHeader);
		monitorPanel.setLocation(10, 11);
		return monitorPanel;
	}

	protected ControlPanel getNewControlPanel(){
		ControlPanel controlPanel = deviceType==DeviceInfo.DEVICE_TYPE_SSPA ? new ControlPanelSSPA(linkHeader, 0) : new ControlPanelUnit(linkHeader);
		controlPanel.setLocation(10, 225);
		return controlPanel;
	}

	@Override
	public boolean equals(Object obj) {
		return obj!=null ? obj.hashCode()==hashCode() : false;
	}

	@Override
	public int hashCode() {
		return linkHeader.getAddr();
	}

	public JLabel getSource() {
		return clickedLabel;
	}

	public PacketListener getPacketListener() {
		return null;
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
	public int compareTo(DevicePanel o) {
		LinkHeader lh = o.getLinkHeader();
		return linkHeader.compareTo(lh);
	}

	public void showDebugPanel(boolean show) {
		if(show)
			tabbedPane.add("Debug", DEBUG_PANEL);
		else
			tabbedPane.remove(DEBUG_PANEL);
	}
}
