package irt.tools.panel;

import irt.data.DeviceInfo;
import irt.tools.panel.subpanel.DACsPanel;
import irt.tools.panel.subpanel.PLLsPanel;
import irt.tools.panel.subpanel.control.ControlPanel;
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

	private int converterType;
	private boolean hasDcOutput;
	private boolean hasFreqSet;//Frequency Set(true) or Frequency range(false)

	public ConverterPanel(DeviceInfo di, int minWidth, int midWidth, int maxWidth, int minHeight, int maxHeight)	throws HeadlessException {
		super(null, "("+di.getSerialNumber()+") "+di.getUnitName(), minWidth, midWidth, maxWidth, minHeight, maxHeight);
		converterType = di.getType();
		hasDcOutput = converterType == DeviceInfo.DEVICE_TYPE_L_TO_140 || converterType == DeviceInfo.DEVICE_TYPE_L_TO_70;
		hasFreqSet 	= converterType == DeviceInfo.DEVICE_TYPE_L_TO_KU || converterType == DeviceInfo.DEVICE_TYPE_L_TO_C;

		JPanel dacPanel = new DACsPanel(null);
		getTabbedPane().addTab("DACs", null, dacPanel, null);
		dacPanel.setLayout(null);

		PLLsPanel registersPanel = new PLLsPanel();
		getTabbedPane().addTab("PLLs", null, registersPanel, null);

//		JPanel registerPanel = new RegistersPanel();
//		getTabbedPane().addTab("Registers", null, registerPanel, null);

		DebagInfoPanel infoPanel = new DebagInfoPanel(null, this);
		getTabbedPane().addTab("Info", null, infoPanel, null);
	}

	@Override
	protected MonitorPanelAbstract getNewMonitorPanel() {
		MonitorPanelConverter monitorPanel = new MonitorPanelConverter();
		monitorPanel.setLocation(10, 11);
		return monitorPanel;
	}

	@Override
	protected ControlPanel getNewControlPanel() {
		ControlPanelConverter controlPanel = hasDcOutput ? new ControlPanelDownConverter() : new ControlPanelConverter(hasFreqSet);
		controlPanel.setLocation(10, 225);
		return controlPanel;
	}
}
