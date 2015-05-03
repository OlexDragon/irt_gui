package irt.controller;

import irt.controller.control.ControllerAbstract;
import irt.controller.serial_port.value.getter.GetterAbstract;
import irt.controller.serial_port.value.setter.ConfigurationSetter;
import irt.controller.serial_port.value.setter.SetterAbstract;
import irt.controller.translation.Translation;
import irt.data.DeviceInfo;
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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JLabel;

import org.apache.logging.log4j.LogManager;

public class MuteController extends ControllerAbstract {

	private JButton btnMute;
	private JLabel lblMute;
	
	private boolean isMute;
	private ActionListener actionListener;
	private final MouseListener mouseListener = new MouseListener() {
		
		@Override
		public void mouseReleased(MouseEvent e) {
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
		}
		
		@Override
		public void mouseExited(MouseEvent e) {
		}
		
		@Override
		public void mouseEntered(MouseEvent e) {
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			setMuteUnmute();
		}
	};

	public MuteController(int deviceType, LinkHeader linkHeader, JButton btnMute, JLabel lblMute, Style style) {
		super(deviceType,
				"Mute Controller",
				new ConfigurationSetter(
						linkHeader,
						linkHeader!=null && linkHeader.getAddr()!=0 && deviceType!=DeviceInfo.DEVICE_TYPE_L_TO_KU_OUTDOOR ? Packet.IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_MUTE : Packet.IRT_SLCP_DATA_FCM_CONFIG_MUTE_CONTROL,
								PacketWork.PACKET_ID_CONFIGURATION_MUTE,
								LogManager.getLogger()),
								null,
								style);
		this.btnMute = btnMute;
		this.btnMute.addActionListener(actionListener);
		this.lblMute = lblMute;
		this.lblMute.addMouseListener(mouseListener);
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return new ValueChangeListener() {

			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				if(valueChangeEvent.getID() == PacketWork.PACKET_ID_CONFIGURATION_MUTE){
					logger.trace("valueChangeEvent: {}", valueChangeEvent);
					new ControllerWorker(valueChangeEvent);
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
		lblMute.removeMouseListener(mouseListener);
		lblMute = null;
		actionListener = null;
	}

	@Override
	protected void setListeners() {
		actionListener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setMuteUnmute();
			}
		};
	}

	private void setMuteUnmute() {
		SetterAbstract as = (SetterAbstract) getPacketWork();
		PacketThread pt = as.getPacketThread();
		if(pt.getPacket()==null)
			return;
		as.preparePacketToSend(new IdValue(deviceType!=DeviceInfo.DEVICE_TYPE_L_TO_KU_OUTDOOR ? PacketWork.PACKET_ID_CONFIGURATION_MUTE : PacketWork.PACKET_ID_CONFIGURATION_MUTE_OUTDOOR, new Boolean(isMute=!isMute)));
		logger.trace("PacketThread: {}", pt);
		setSend(true);
		if(isMute)
			MuteController.this.lblMute.setText(Translation.getValue(String.class, "unmute", "UNMUTE"));
		else
			MuteController.this.lblMute.setText(Translation.getValue(String.class, "mute", "MUTE"));
	}

	//********************* class ControllerWorker *****************
	private class ControllerWorker extends Thread {

		private ValueChangeEvent valueChangeEvent;

		public ControllerWorker(ValueChangeEvent valueChangeEvent){
			this.valueChangeEvent = valueChangeEvent;
			int priority = getPriority();
			if(priority>Thread.MIN_PRIORITY)
				setPriority(priority-1);
			setDaemon(true);
			start();
		}

		@Override
		public void run() {
			try {
				GetterAbstract pw = (GetterAbstract) getPacketWork();
				long source;

				if (valueChangeEvent.getSource() instanceof Byte)
					source = (Byte) valueChangeEvent.getSource();
				else if(valueChangeEvent.getSource() instanceof Integer)
					source = (Integer) valueChangeEvent.getSource();
				else
					source = (Long) valueChangeEvent.getSource();

				String text;
				if (isMute = (source > 0)) {
					text = Translation.getValue(String.class, "unmute", "UNMUTE");
					lblMute.setText(text);
					btnMute.setToolTipText(text);
					if (style == Style.CHECK_ALWAYS)
						pw.getPacketThread().preparePacket(pw.getPacketParameterHeaderCode(), (Object) null);
				} else if (source == 0) {
					text = Translation.getValue(String.class, "mute", "MUTE");
					lblMute.setText(text);
					btnMute.setToolTipText(text);
					if (style == Style.CHECK_ALWAYS)
						pw.getPacketThread().preparePacket(pw.getPacketParameterHeaderCode(), (Object) null);
				} else {
					lblMute.setText("error" + source);
					pw.getPacketThread().preparePacket(pw.getPacketParameterHeaderCode(), (Object) null);
					setSend(true, false);
				}
			} catch (Exception ex) {
				logger.catching(ex);
			}
		}

	}
}
