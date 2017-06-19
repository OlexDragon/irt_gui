package irt.tools.panel;

import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;
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

import irt.controller.GuiController;
import irt.controller.interfaces.ControlPanel;
import irt.data.DeviceInfo;
import irt.data.DeviceInfo.DeviceType;
import irt.data.listener.PacketListener;
import irt.data.packet.LinkHeader;
import irt.tools.fx.MonitorPanelSwingWithFx;
import irt.tools.panel.head.Panel;
import irt.tools.panel.subpanel.DebugPanel;
import irt.tools.panel.subpanel.InfoPanel;
import irt.tools.panel.subpanel.control.ControlDownlinkRedundancySystem;
import irt.tools.panel.subpanel.control.ControlPanelSSPA;
import irt.tools.panel.subpanel.control.ControlPanelUnit;
import irt.tools.panel.subpanel.monitor.Monitor;
import irt.tools.panel.subpanel.monitor.MonitorPanelAbstract;

@SuppressWarnings("serial")
public class DevicePanel extends Panel implements Comparable<DevicePanel>{

	public static final DebugPanel DEBUG_PANEL = new DebugPanel(Optional.empty());

	protected final Logger logger = (Logger) LogManager.getLogger(getClass());

	protected final String selectedTab = "selected_tab_"+getClass().getSimpleName();

	private JPanel controlPanel;
	private JLabel clickedLabel;
	private InfoPanel infoPanel;
	private JTabbedPane tabbedPane;

	protected final Preferences pref = GuiController.getPrefs();

	private MonitorPanelSwingWithFx monitorPanel;
												public Monitor getMonitorPanel() {
													return monitorPanel;
												}

	protected Optional<DeviceType> deviceType;

	private LinkHeader linkHeader;

	protected DeviceInfo deviceInfo;

	public DevicePanel(DeviceInfo deviceInfo, int minWidth, int midWidth, int maxWidth, int minHeight, int maxHeight) throws HeadlessException {
		super( deviceInfo!=null ? "("+deviceInfo.getSerialNumber()+") "+deviceInfo.getUnitName() : null, minWidth, midWidth, maxWidth, minHeight, maxHeight);
		setBorder(null);
		setName("DevicePanel");
		this.deviceInfo = deviceInfo;
		
		final Optional<LinkHeader> oLinkHeader = Optional.ofNullable(deviceInfo.getLinkHeader());
		lblAddress.setText(lblAddress.getText()+oLinkHeader.map(LinkHeader::getIntAddr).map(Object::toString).orElse("N/A"));
		if(deviceInfo!=null)
			this.deviceType = deviceInfo.getDeviceType();
		addAncestorListener(new AncestorListener() {

			public void ancestorAdded(AncestorEvent event) {

				monitorPanel = new MonitorPanelSwingWithFx();
				monitorPanel.setLocation(10, 11);
				monitorPanel.setSize(215, 210);
				oLinkHeader.ifPresent(lh->monitorPanel.setUnitAddress(lh.getAddr()));
				userPanel.add((Component) monitorPanel);

				controlPanel = getNewControlPanel();
				userPanel.add(controlPanel);

				if(controlPanel instanceof ControlPanel){
					JSlider slider = ((ControlPanel)controlPanel).getSlider();
					slider.setOpaque(false);
					slider.setOrientation(SwingConstants.VERTICAL);
					slider.setBounds(230, 11, 33, 400);
					userPanel.add(slider);
					userPanel.revalidate();
				}
			}
			public void ancestorMoved(AncestorEvent event) {
			}
			public void ancestorRemoved(AncestorEvent event) {
				userPanel.removeAll();
			}
		});

		linkHeader = oLinkHeader.orElse(new LinkHeader((byte)0, (byte)0, (short) 0));
		DEBUG_PANEL.setLinkHeader(linkHeader);

		infoPanel = new InfoPanel(deviceInfo);
		infoPanel.setLocation(10, 11);
		extraPanel.add(infoPanel);

		setTabbedPane(extraPanel);
		
	}

	public JPanel getControlPanel() {
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

	protected JPanel getNewControlPanel(){
		logger.error("deviceType: {}", deviceType);

		MonitorPanelAbstract controlPanel = deviceType.map(dt->{

															switch(dt){
															case SSPA:
																return new ControlPanelSSPA(deviceType, linkHeader, 0);
															case DLRS:
																return new ControlDownlinkRedundancySystem(deviceType, linkHeader);
															default:
																return new ControlPanelUnit(deviceType, linkHeader);
															}
											}).orElse(new ControlPanelUnit(deviceType, linkHeader));

		controlPanel.setLocation(10, 225);
		return controlPanel;
	}

	@Override
	public boolean equals(Object obj) {
		return obj!=null ? obj.hashCode()==hashCode() : false;
	}

	@Override
	public int hashCode() {
		return Optional.ofNullable(linkHeader).map(LinkHeader::getAddr).orElse((byte) 0);
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
