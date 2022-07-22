package irt.tools.panel;

import java.awt.Color;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.DumpControllerFull;
import irt.controller.Dumper;
import irt.controller.GuiController;
import irt.controller.interfaces.ControlPanel;
import irt.data.DeviceInfo;
import irt.data.DeviceType;
import irt.data.packet.LinkHeader;
import irt.tools.fx.MonitorPanelSwingWithFx;
import irt.tools.fx.control.ControlPanelSwingWithFx;
import irt.tools.fx.interfaces.StopInterface;
import irt.tools.panel.head.Panel;
import irt.tools.panel.subpanel.DebugPanel;
import irt.tools.panel.subpanel.InfoPanel;
import irt.tools.panel.subpanel.control.ControlDownlinkRedundancySystem;
import irt.tools.panel.subpanel.control.ControlPaneIPc;
import irt.tools.panel.subpanel.control.ControlPanelHPB;
import irt.tools.panel.subpanel.control.ControlPanelPicobuc;
import irt.tools.panel.subpanel.control.ControlPanelSSPA;
import irt.tools.panel.subpanel.control.ControlPanelUnit;
import irt.tools.panel.subpanel.monitor.Monitor;

@SuppressWarnings("serial")
public class DevicePanel extends Panel implements Comparable<Component>{

	public final static DebugPanel DEBUG_PANEL = new DebugPanel(Optional.empty());

	protected final static Logger logger = LogManager.getLogger();

	protected final String selectedTab = "selected_tab_"+getClass().getSimpleName();

	private JComponent controlPanel;
	private JLabel clickedLabel;
	private InfoPanel infoPanel;
	private static JTabbedPane tabbedPane;

	protected final Preferences pref = GuiController.getPrefs();

	private MonitorPanelSwingWithFx monitorPanel;
												public Monitor getMonitorPanel() {
													return monitorPanel;
												}

	private Optional<DeviceType> deviceType = Optional.empty();

	private LinkHeader linkHeader;

	protected DeviceInfo deviceInfo;

	public DevicePanel(DeviceInfo deviceInfo, int minWidth, int midWidth, int maxWidth, int minHeight, int maxHeight) throws HeadlessException {
		super(
				deviceInfo!=null ? deviceInfo.getLinkHeader().getAddr() : (byte) 0,
				deviceInfo!=null ? deviceInfo.getSerialNumber().orElse("N/A") + " : " + deviceInfo.getUnitName().orElse("N/A") : "N/A",
				minWidth,
				midWidth,
				maxWidth,
				minHeight,
				maxHeight);


		setBorder(null);
		setName("DevicePanel");
		this.deviceInfo = deviceInfo;
		
		final Optional<LinkHeader> oLinkHeader = Optional.ofNullable(deviceInfo).map(DeviceInfo::getLinkHeader).filter(lh->lh!=null);
		lblAddress.setText(lblAddress.getText()+oLinkHeader.map(LinkHeader::getIntAddr).map(Object::toString).orElse("N/A"));
		if(deviceInfo!=null)
			this.deviceType = deviceInfo.getDeviceType();

		addAncestorListener(new AncestorListener() {

			private Dumper dumpController;
			public void ancestorAdded(AncestorEvent event) {

				if(deviceType.map(dt->!dt.equals(DeviceType.IR_PC)).orElse(true)){
					monitorPanel = new MonitorPanelSwingWithFx(deviceType);
					monitorPanel.setLocation(10, 11);
					monitorPanel.setSize(215, 210);
					oLinkHeader.ifPresent(lh->monitorPanel.setUnitAddress(lh.getAddr()));
					userPanel.add((Component) monitorPanel);
//				logger.error("*** 2 ***");

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
				}else{
					controlPanel = getNewControlPanel();
					controlPanel.setSize(260, 410);
					controlPanel.setLocation(5, 5);
					final Color background = userPanel.getBackground();
					controlPanel.setBackground(background);
					userPanel.add(controlPanel);					
				}

					dumpController = new DumpControllerFull(deviceInfo);
			}
			public void ancestorRemoved(AncestorEvent event) {

				Optional.ofNullable(controlPanel).filter(StopInterface.class::isInstance).map(StopInterface.class::cast).ifPresent(StopInterface::stop);

				userPanel.removeAll();
				Optional.ofNullable(dumpController).ifPresent(dc->{
					try {
						dc.stop();
					} catch (Throwable e) {
						logger.catching(e);
					}
				});
			}
			public void ancestorMoved(AncestorEvent event) { }
		});

		linkHeader = oLinkHeader.orElse(new LinkHeader((byte)0, (byte)0, (short) 0));
		DEBUG_PANEL.setLinkHeader(linkHeader);

		infoPanel = new InfoPanel(deviceInfo);
		infoPanel.setLocation(10, 11);
		extraPanel.add(infoPanel);

		setTabbedPane(extraPanel);
		
	}

	public JComponent getControlPanel() {
		return controlPanel;
	}

	protected void setTabbedPane(JPanel extraPanel) {
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addContainerListener(new ContainerAdapter() {
			@Override
			public void componentAdded(ContainerEvent e) {

				SwingUtilities.invokeLater(()->{
					int select = pref.getInt(selectedTab, 0);
					int tabCount = tabbedPane.getTabCount();

					if (tabbedPane.getSelectedIndex() != select && tabCount != 0 && tabCount > select) {
						tabbedPane.setSelectedIndex(select);
						logger.trace("selectedTab={}, select={}, tabCount={}", selectedTab, select, tabCount);
					}
				});
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

	protected JComponent getNewControlPanel(){

		JComponent controlPanel = deviceType.map(
									dt->{

										switch(dt){
										case C_SSPA:
											return new ControlPanelSSPA(deviceType, linkHeader, 0);
										case DLRS:
										case DLRS2:
											return new ControlDownlinkRedundancySystem(deviceType, linkHeader);
										case IR_PC:
											return new ControlPaneIPc(deviceType, linkHeader);
										case LNB_REDUNDANCY_1x2:
											return new ControlPanelSwingWithFx(linkHeader);
										case HPB_L_TO_C:
										case HPB_L_TO_KU:
										case HPB_SSPA:
											return new ControlPanelHPB(linkHeader.getAddr());
										default:
											return new ControlPanelPicobuc(deviceType, linkHeader);
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

	public static JTabbedPane getTabbedPane() {
		return tabbedPane;
	}

	public LinkHeader getLinkHeader() {
		return linkHeader;
	}

	public InfoPanel getInfoPanel() {
		return infoPanel;
	}


	@Override
	public int compareTo(Component o) {
		return Optional.of(o).filter(DevicePanel.class::isInstance).map(DevicePanel.class::cast).map(DevicePanel::getLinkHeader).map(linkHeader::compareTo).orElse(1);
	}
}
