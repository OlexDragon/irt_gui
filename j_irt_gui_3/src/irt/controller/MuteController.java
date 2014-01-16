package irt.controller;

import irt.controller.control.ControllerAbstract;
import irt.controller.serial_port.value.getter.GetterAbstract;
import irt.controller.serial_port.value.seter.ConfigurationSetter;
import irt.controller.serial_port.value.seter.SetterAbstract;
import irt.controller.translation.Translation;
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

public class MuteController extends ControllerAbstract {

	private JButton btnMute;
	private JLabel lblMute;
	
	private boolean isMute;
	private ActionListener actionListener;

	public MuteController(LinkHeader linkHeader, JButton btnMute, JLabel lblMute, Style style) {
		super("Mute Controller", new ConfigurationSetter(linkHeader, linkHeader!=null ? Packet.IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_MUTE : Packet.IRT_SLCP_DATA_FCM_CONFIG_MUTE_CONTROL, PacketWork.PACKET_ID_CONFIGURATION_BAIAS_25W_MUTE), null, style);
		this.btnMute = btnMute;
		this.btnMute.addActionListener(actionListener);
		this.lblMute = lblMute;
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return new ValueChangeListener() {

			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				if(valueChangeEvent.getID() == PacketWork.PACKET_ID_CONFIGURATION_BAIAS_25W_MUTE)
					new ControllerWorker(valueChangeEvent);
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
					MuteController.this.lblMute.setText(Translation.getValue(String.class, "unmute", "UNMUTE"));
				else
					MuteController.this.lblMute.setText(Translation.getValue(String.class, "mute", "MUTE"));
			}
		};
	}

	//********************* class ControllerWorker *****************
	private class ControllerWorker extends Thread {

		private ValueChangeEvent valueChangeEvent;

		public ControllerWorker(ValueChangeEvent valueChangeEvent){
			setDaemon(true);
			this.valueChangeEvent = valueChangeEvent;
			int priority = getPriority();
			if(priority>Thread.MIN_PRIORITY)
				setPriority(priority-1);
			start();
		}

		@Override
		public void run() {
			GetterAbstract pw = (GetterAbstract)getPacketWork();
			long source;

			if(valueChangeEvent.getSource() instanceof Byte)
				source = (Byte)valueChangeEvent.getSource();
			else
				source = (Long)valueChangeEvent.getSource();

			String text;
			if(isMute=(source>0)){
				text = Translation.getValue(String.class, "unmute", "UNMUTE");
				lblMute.setText(text);
				btnMute.setToolTipText(text);
				if(style==Style.CHECK_ALWAYS)
					pw.getPacketThread().preparePacket(pw.getPacketParameterHeaderCode(), (Object)null);
			}else if(source==0){
				text = Translation.getValue(String.class, "mute", "MUTE");
				lblMute.setText(text);
				btnMute.setToolTipText(text);
				if(style==Style.CHECK_ALWAYS)
					pw.getPacketThread().preparePacket(pw.getPacketParameterHeaderCode(), (Object)null);
			}else{
				lblMute.setText("error"+source);
				pw.getPacketThread().preparePacket(pw.getPacketParameterHeaderCode(), (Object)null);
				setSend(true, false);
			}
		}

	}
}
