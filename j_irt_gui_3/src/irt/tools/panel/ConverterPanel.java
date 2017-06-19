package irt.tools.panel;

import java.awt.HeadlessException;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import irt.data.DeviceInfo;
import irt.data.DeviceInfo.DeviceType;
import irt.data.DeviceInfo.Protocol;
import irt.tools.fx.AlarmPanelFx;
import irt.tools.fx.JavaFxWrapper;
import irt.tools.panel.subpanel.DACsPanel;
import irt.tools.panel.subpanel.PLL_HMC807LP6CE_Reg9;
import irt.tools.panel.subpanel.PLLsPanel;
import irt.tools.panel.subpanel.control.ControlPanelConverter;
import irt.tools.panel.subpanel.control.ControlPanelDownConverter;
import irt.tools.panel.subpanel.control.ControlPanelImpl;

@SuppressWarnings("serial")
public class ConverterPanel extends DevicePanel {

	public static final boolean LNB_OFF	= false;
	public static final boolean LNB_ON	= true;

	private boolean hasDcOutput;
	private boolean hasFreqSet;//Frequency Set(true) or Frequency range(false)

	public ConverterPanel(DeviceInfo deviceInfo, Protocol protocol, int maxHeight)	throws HeadlessException {
		super(deviceInfo, 0, 0, 0, 0, maxHeight);

		final JTabbedPane tabbedPane = getTabbedPane();

		JavaFxWrapper alarmPanel = new JavaFxWrapper(new AlarmPanelFx());
		alarmPanel.setUnitAddress((byte) 0);
//		alarmPanel.setBorder(null);
		tabbedPane.addTab("alarms", alarmPanel);

		hasDcOutput = 	deviceType
				.filter(
						dt->(dt==DeviceType.CONVERTER_L_TO_140 ||
						dt == DeviceType.CONVERTER_L_TO_70 ||
						dt == DeviceType.CONVERTER_C_TO_L))
				.map(dt->true)
				.orElse(false);

		hasFreqSet 	= 	deviceType
				.filter(
						dt->(dt == DeviceType.CONVERTER_L_TO_KU ||
						dt == DeviceType.CONVERTER_L_TO_C ||
						dt == DeviceType.CONVERTER_KU_TO_L||
						dt == DeviceType.CONVERTER_C_TO_L))
				.map(dt->true)
				.orElse(false);

		JPanel dacPanel = new DACsPanel(deviceType, null);
		tabbedPane.addTab("DACs", null, dacPanel, null);
		dacPanel.setLayout(null);

		JPanel registersPanel = deviceInfo.getDeviceType().filter(dt->dt==DeviceType.CONVERTER_L_TO_KU).map(dt->new PLL_HMC807LP6CE_Reg9(deviceType)).map(JPanel.class::cast).orElse(new PLLsPanel(deviceType));
		tabbedPane.addTab("PLLs", null, registersPanel, null);

//		JPanel registerPanel = new RegistersPanel();
//		getTabbedPane().addTab("Registers", null, registerPanel, null);

		DebagInfoPanel infoPanel = new DebagInfoPanel(deviceInfo.getDeviceType(), null, this);
		tabbedPane.addTab("Info", null, infoPanel, null);
	}

	@Override
	protected ControlPanelImpl getNewControlPanel() {
		ControlPanelConverter controlPanel = hasDcOutput ? new ControlPanelDownConverter(deviceType) : new ControlPanelConverter(deviceType, hasFreqSet);
		controlPanel.setLocation(10, 225);
		return controlPanel;
	}
}
