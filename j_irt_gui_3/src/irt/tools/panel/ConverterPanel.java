package irt.tools.panel;

import irt.controller.GuiControllerAbstract.Protocol;
import irt.data.DeviceInfo;
import irt.tools.panel.subpanel.DACsPanel;
import irt.tools.panel.subpanel.PLL_HMC807LP6CE_Reg9;
import irt.tools.panel.subpanel.PLLsPanel;
import irt.tools.panel.subpanel.control.ControlPanelImpl;
import irt.tools.panel.subpanel.control.ControlPanelConverter;
import irt.tools.panel.subpanel.control.ControlPanelDownConverter;
import irt.tools.panel.subpanel.monitor.MonitorPanelAbstract;
import irt.tools.panel.subpanel.monitor.MonitorPanelConverter;

import java.awt.HeadlessException;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ConverterPanel extends DevicePanel {

	public static final boolean LNB_OFF	= false;
	public static final boolean LNB_ON	= true;

	private boolean hasDcOutput;
	private boolean hasFreqSet;//Frequency Set(true) or Frequency range(false)

	public ConverterPanel(DeviceInfo deviceInfo, Protocol protocol, int maxHeight)	throws HeadlessException {
		super(null, deviceInfo, 0, 0, 0, 0, maxHeight);

		hasDcOutput = 	deviceType == DeviceInfo.DEVICE_TYPE_L_TO_140 ||
						deviceType == DeviceInfo.DEVICE_TYPE_L_TO_70 ||
						deviceType == DeviceInfo.DEVICE_TYPE_C_TO_L;

		hasFreqSet 	= 	deviceType == DeviceInfo.DEVICE_TYPE_L_TO_KU||
						deviceType == DeviceInfo.DEVICE_TYPE_L_TO_C	||
						deviceType == DeviceInfo.DEVICE_TYPE_KU_TO_L||
						deviceType == DeviceInfo.DEVICE_TYPE_C_TO_L;

		JPanel dacPanel = new DACsPanel(deviceType, null);
		getTabbedPane().addTab("DACs", null, dacPanel, null);
		dacPanel.setLayout(null);

		JPanel registersPanel = protocol.getDeviceType()== DeviceInfo.DEVICE_TYPE_L_TO_KU ? new PLL_HMC807LP6CE_Reg9(deviceType) : new PLLsPanel(deviceType);
		getTabbedPane().addTab("PLLs", null, registersPanel, null);

//		JPanel registerPanel = new RegistersPanel();
//		getTabbedPane().addTab("Registers", null, registerPanel, null);

		DebagInfoPanel infoPanel = new DebagInfoPanel(deviceInfo.getType(), null, this);
		getTabbedPane().addTab("Info", null, infoPanel, null);
	}

	@Override
	protected MonitorPanelAbstract getNewMonitorPanel() {
		MonitorPanelConverter monitorPanel = new MonitorPanelConverter(deviceType);
		monitorPanel.setLocation(10, 11);
		return monitorPanel;
	}

	@Override
	protected ControlPanelImpl getNewControlPanel() {
		ControlPanelConverter controlPanel = hasDcOutput ? new ControlPanelDownConverter(deviceType) : new ControlPanelConverter(deviceType, hasFreqSet);
		controlPanel.setLocation(10, 225);
		return controlPanel;
	}
}
