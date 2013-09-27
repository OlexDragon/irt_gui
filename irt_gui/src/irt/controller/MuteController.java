package irt.controller;

import irt.controller.control.ControllerAbstract;
import irt.controller.serial_port.value.Getter.GetterAbstract;
import irt.controller.serial_port.value.seter.ConfigurationSetter;
import irt.controller.serial_port.value.seter.SetterAbstract;
import irt.data.IdValue;
import irt.data.PacketThread;
import irt.data.PacketWork;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;

import resources.Translation;

public class MuteController extends ControllerAbstract {

	private JButton btnMute;
	private JLabel lblMute;
	
	private boolean isMute;
	private ActionListener actionListener;

	public MuteController(LinkHeader linkHeader, JButton btnMute, JLabel lblMute, Style style) {
		super(new ConfigurationSetter(linkHeader, linkHeader!=null ? Packet.IRT_SLCP_PARAMETER_25W_BAIS_CONFIGURATION_MUTE : Packet.IRT_SLCP_PARAMETER_FCM_CONFIG_MUTE_CONTROL, PacketWork.PACKET_ID_CONFIGURATION_BAIAS_25W_MUTE), style);
		this.btnMute = btnMute;
		this.btnMute.addActionListener(actionListener);
		this.lblMute = lblMute;
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return new ValueChangeListener() {

			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				if(valueChangeEvent.getID() == PacketWork.PACKET_ID_CONFIGURATION_BAIAS_25W_MUTE){

					GetterAbstract pw = (GetterAbstract)getPacketWork();
					long source;

					if(valueChangeEvent.getSource() instanceof Byte)
						source = (Byte)valueChangeEvent.getSource();
					else
						source = (Long)valueChangeEvent.getSource();

					String muteText;
					if(isMute=(source>0)){
						muteText = Translation.getValue(String.class, "unmute", "UNMUTE");
						lblMute.setText(muteText);
						btnMute.setToolTipText(muteText);
						if(style==Style.CHECK_ALWAYS)
							pw.getPacketThread().preparePacket(pw.getPacketParameterHeaderCode(), (Object)null);
					}else if(source==0){
						muteText = Translation.getValue(String.class, "mute", "MUTE");
						lblMute.setText(muteText);
						btnMute.setToolTipText(muteText);
						if(style==Style.CHECK_ALWAYS)
							pw.getPacketThread().preparePacket(pw.getPacketParameterHeaderCode(), (Object)null);
					}else{
						lblMute.setText("error"+source);
						pw.getPacketThread().preparePacket(pw.getPacketParameterHeaderCode(), (Object)null);
						setSend(true, false);
					}
				}
			}
		};
	}

	@Override
	protected boolean setComponent(Component component) {
		return false;
	}

	@Override
	protected void clear() {
		super.clear();
		btnMute.removeActionListener(actionListener);
		btnMute = null;
		lblMute = null;
		actionListener = null;
	}

	@Override
	protected void setListeners() {
		actionListener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				SetterAbstract as = (SetterAbstract) getPacketWork();
				PacketThread pt = as.getPacketThread();
				if(pt.getPacket()==null)
					return;
				as.preparePacketToSend(new IdValue(PacketWork.PACKET_ID_CONFIGURATION_BAIAS_25W_MUTE, new Boolean(isMute=!isMute)));
				setSend(true);
				if(isMute)
					MuteController.this.lblMute.setText("UNMUTE");
				else
					MuteController.this.lblMute.setText("MUTE");
			}
		};
	}
}
