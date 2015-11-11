package irt.controller.control;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import irt.controller.MuteController;
import irt.controller.StoreConfigController;
import irt.controller.SwitchController;
import irt.controller.serial_port.value.setter.ConfigurationSetter;
import irt.controller.serial_port.value.setter.SetterAbstract;
import irt.data.IdValue;
import irt.data.IdValueForComboBox;
import irt.data.PacketWork;
import irt.data.Range;
import irt.data.RundomNumber;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.PacketImp;
import irt.data.value.ValueFrequency;
import irt.tools.panel.subpanel.monitor.MonitorPanelAbstract;

public class ControlController extends ControllerAbstract {
	private JButton btnMute;
	private JLabel lblMute;
	private SwitchController lnbController; 
	private MuteController muteController;
	private JButton btnStore;
	private JCheckBox chbxLNB;
	protected JComboBox<Object> comboBoxfreqSet;
	protected ItemListener itemListenerComboBox;

	/**
	 * Use for LO control
	 * @param hasFreqSet 
	 */
	public ControlController(int deviceType, String controllerName, LinkHeader linkHeader, MonitorPanelAbstract panel) {
		super(deviceType, controllerName, new ConfigurationSetter(linkHeader), panel, Style.CHECK_ALWAYS);
		if(comboBoxfreqSet==null)
			setSend(false);

		muteController = new MuteController(deviceType, linkHeader, btnMute, lblMute, Style.CHECK_ALWAYS);
		Thread t = new Thread(muteController, "ControlController.MuteController-"+new RundomNumber().toString());
		int priority = t.getPriority();
		if(priority>Thread.MIN_PRIORITY)
			t.setPriority(priority-1);
		t.setDaemon(true);
		t.start();

		if(chbxLNB!=null){
			lnbController = new SwitchController(deviceType, "LNB Controller", chbxLNB, new ConfigurationSetter(null, PacketImp.PARAMETER_CONFIG_BUC_ENABLE, PacketWork.PACKET_ID_CONFIGURATION_LNB));
			t = new Thread(lnbController, "ControlController.SwitchController-"+new RundomNumber().toString());
			priority = t.getPriority();
			if(priority>Thread.MIN_PRIORITY)
				t.setPriority(priority-1);
			t.setDaemon(true);
			t.start();
		}
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return new ValueChangeListener() {

			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				logger.entry("\n\t{}", valueChangeEvent);
				SetterAbstract pw = (SetterAbstract) getPacketWork();

				if(valueChangeEvent.getID()==pw.getPacketId()){
					Object source = valueChangeEvent.getSource();

					if(comboBoxfreqSet!=null){
						if(source instanceof Object[]){
							ComboBoxModel<Object> comboBoxModel = new DefaultComboBoxModel<>((Object[])source);
							comboBoxfreqSet.setModel(comboBoxModel);

							pw.setPacketId(PacketWork.PACKET_ID_CONFIGURATION_FREQUENCY);
							pw.setPacketParameterHeaderCode(PacketImp.PARAMETER_CONFIG_FCM_FREQUENCY);
							pw.getPacketThread().preparePacket();
							setSend(true, false);
						}else if(source instanceof Long){
							setSend(false);
							ValueFrequency vf = new ValueFrequency((Long)source, Long.MIN_VALUE, Long.MAX_VALUE);

							pw.setPacketType(PacketImp.PACKET_TYPE_COMMAND);

							comboBoxfreqSet.setSelectedItem(vf.toString());
							comboBoxfreqSet.addItemListener(itemListenerComboBox);

						}else if(source instanceof Range)
							comboBoxfreqSet.addItem(new ValueFrequency(((Range)source).getMinimum(), Long.MIN_VALUE, Long.MAX_VALUE).toString());
					}
				}
			}
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	protected boolean setComponent(Component component) {
		String name = component.getName();
		boolean isSet = true;

		if(name!=null)
			switch (name) {
			case "Button Mute":
				btnMute = (JButton)component;
				break;
			case "Label Mute":
				lblMute = (JLabel)component;
				break;
			case "Switch LNB":
				chbxLNB = (JCheckBox) component;
				break;
			case "LO Select":
				comboBoxfreqSet = (JComboBox<Object>)component;
				break;
			case "Store":
				btnStore = (JButton)component;
				btnStore.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						try{
							new StoreConfigController(deviceType, getPacketWork().getPacketThread().getLinkHeader(), getOwner(), Style.CHECK_ONCE);
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
		muteController.stop();
		muteController = null;
		btnMute = null;
		lblMute = null;

		if(lnbController!=null){
			lnbController.stop();
			lnbController = null;
		}
	}

	@Override
	protected void setListeners() {
		if(this instanceof ControlControllerPicobuc)
			itemListenerComboBox = new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent itemEvent) {
				if(itemEvent.getStateChange()==ItemEvent.SELECTED){
					((SetterAbstract)getPacketWork()).preparePacketToSend(new IdValue(PacketWork.PACKET_ID_CONFIGURATION_LO_BIAS_BOARD, (byte) ((IdValueForComboBox)comboBoxfreqSet.getSelectedItem()).getID()));
					setSend(true);
				}
			}
		};
		else
			itemListenerComboBox = new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent itemEvent) {
				if(itemEvent.getStateChange()==ItemEvent.SELECTED){
					((SetterAbstract)getPacketWork()).preparePacketToSend(new IdValue(PacketWork.PACKET_ID_CONFIGURATION_FREQUENCY,
							new ValueFrequency(comboBoxfreqSet.getSelectedItem().toString(),"0", ""+Long.MAX_VALUE)));
					setSend(true);
				}
			}
		};

	}
}
