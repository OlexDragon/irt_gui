package irt.controller.control;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JPanel;

import irt.controller.StoreConfigController;
import irt.controller.SwitchController;
import irt.controller.serial_port.value.setter.ConfigurationSetter;
import irt.data.DeviceInfo.DeviceType;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.tools.panel.subpanel.monitor.MonitorPanelAbstract;

public class ControlController extends ControllerAbstract {
//	private JButton btnMute;
//	private JLabel lblMute;
	private SwitchController lnbController; 
//	private MuteController muteController;
	private JButton btnStore;
//	protected JComboBox<Object> comboBoxfreqSet;
	protected ItemListener itemListenerComboBox;
	private final byte linkAddr;

	/**
	 * Use for LO control
	 * @param hasFreqSet 
	 */
	public ControlController(Optional<DeviceType> deviceType, String controllerName, LinkHeader linkHeader, MonitorPanelAbstract panel) {
		super(deviceType, controllerName, new ConfigurationSetter(linkHeader), panel, Style.CHECK_ALWAYS);
		run = false;
		linkAddr = linkHeader.getAddr();
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return new ValueChangeListener() {

			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				logger.entry("\n\t{}", valueChangeEvent);
			}
		};
	}

//	@SuppressWarnings("unchecked")
	@Override
	protected boolean setComponent(Component component) {
		String name = component.getName();
		boolean isSet = true;

		if(name!=null)
			switch (name) {
//			case "Button Mute":
//				btnMute = (JButton)component;
//				break;
//			case "Label Mute":
//				lblMute = (JLabel)component;
//				break;
//			case "LO Select":
//				comboBoxfreqSet = (JComboBox<Object>)component;
//				break;
			case "Store":
				btnStore = (JButton)component;
				btnStore.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {

						try{

							final JPanel owner = getOwner();
							new StoreConfigController(linkAddr, owner);

						}catch(Exception ex){
							logger.catching(ex);
						}
					}
				});
				break;
			default:
				isSet = false;
			}
		else
			isSet = false;

		return isSet;
	}

	@Override
	protected void clear() {
		super.clear();
//		muteController.stop();
//		muteController = null;
//		btnMute = null;
//		lblMute = null;

		if(lnbController!=null){
			lnbController.stop();
			lnbController = null;
		}
	}

	@Override protected void setListeners() {
	}
}
