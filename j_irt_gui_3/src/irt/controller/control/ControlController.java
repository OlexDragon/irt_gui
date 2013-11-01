package irt.controller.control;

import irt.controller.MuteController;
import irt.controller.StoreConfigController;
import irt.controller.SwitchController;
import irt.controller.serial_port.value.seter.ConfigurationSetter;
import irt.controller.serial_port.value.seter.SetterAbstract;
import irt.data.IdValue;
import irt.data.IdValueForComboBox;
import irt.data.PacketWork;
import irt.data.Range;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.value.ValueFrequency;
import irt.tools.panel.subpanel.control.ControlPanel;

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
	public ControlController(LinkHeader linkHeader, ControlPanel panel) {
		super(new ConfigurationSetter(linkHeader), panel, Style.CHECK_ALWAYS);
		if(comboBoxfreqSet==null)
			setSend(false);

		muteController = new MuteController(linkHeader, btnMute, lblMute, Style.CHECK_ALWAYS);
		Thread t = new Thread(muteController, "Mute Controller");
		t.setPriority(t.getPriority()-1);
		t.start();

		if(chbxLNB!=null){
			lnbController = new SwitchController(chbxLNB, new ConfigurationSetter(null, Packet.IRT_SLCP_DATA_FCM_CONFIG_BUC_ENABLE, PacketWork.PACKET_ID_CONFIGURATION__LNB));
			t = new Thread(lnbController, "LNB ON/OFF");
			t.setPriority(t.getPriority()-1);
			t.start();
		}
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return new ValueChangeListener() {

			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				SetterAbstract pw = (SetterAbstract) getPacketWork();

				if(valueChangeEvent.getID()==pw.getPacketId()){
					Object source = valueChangeEvent.getSource();

					if(comboBoxfreqSet!=null){
						if(source instanceof Object[]){
							ComboBoxModel<Object> comboBoxModel = new DefaultComboBoxModel<>((Object[])source);
							comboBoxfreqSet.setModel(comboBoxModel);

							pw.setPacketId(PacketWork.PACKET_ID_CONFIGURATION_FREQUENCY);
							pw.setPacketParameterHeaderCode(Packet.IRT_SLCP_DATA_FCM_CONFIG_FREQUENCY);
							pw.getPacketThread().preparePacket();
							setSend(true, false);
						}else if(source instanceof Long){
							setSend(false);
							ValueFrequency vf = new ValueFrequency((Long)source, Long.MIN_VALUE, Long.MAX_VALUE);

							pw.setPacketType(Packet.IRT_SLCP_PACKET_TYPE_COMMAND);

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
						new StoreConfigController(getPacketWork().getPacketThread().getLinkHeader(), getOwner(), Style.CHECK_ONCE);
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
		muteController.setRun(false);
		muteController = null;
		btnMute = null;
		lblMute = null;

		if(lnbController!=null){
			lnbController.setRun(false);
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
