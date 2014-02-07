package irt.controller;

import irt.controller.control.ControllerAbstract;
import irt.controller.serial_port.value.seter.ConfigurationSetter;
import irt.controller.serial_port.value.seter.SetterAbstract;
import irt.data.IdValue;
import irt.data.IdValueForComboBox;
import irt.data.Listeners;
import irt.data.PacketThread;
import irt.data.PacketWork;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

public class LoController extends ControllerAbstract {

	JComboBox<String> cbLoSelect;
	private ItemListener itemListener;

	public LoController(LinkHeader linkHeader, JComboBox<String> cbLoSelect, Style stile) {
		super("LoController", new ConfigurationSetter(linkHeader), null, stile);

		this.cbLoSelect = cbLoSelect;
		cbLoSelect.addItemListener(itemListener);
		cbLoSelect.addPopupMenuListener(Listeners.popupMenuListener);
	}

	@Override protected void setListeners() {
		itemListener = new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent itemEvent) {
				if(itemEvent.getStateChange()==ItemEvent.SELECTED){
					((SetterAbstract)getPacketWork()).preparePacketToSend(new IdValue(PacketWork.PACKET_ID_CONFIGURATION_LO_BIAS_BOARD, (byte) LoController.this.cbLoSelect.getSelectedIndex()));
					setSend(true);
				}
			}
		};
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return new ValueChangeListener() {

			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
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
		cbLoSelect.removeItemListener(itemListener);
		cbLoSelect.removePopupMenuListener(Listeners.popupMenuListener);
		cbLoSelect = null;
		itemListener = null;
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

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			SetterAbstract pw = (SetterAbstract) getPacketWork();
			PacketThread pt = pw.getPacketThread();

			if(valueChangeEvent.getID()==pw.getPacketId()){
				Object source = valueChangeEvent.getSource();

				if(source instanceof ComboBoxModel){
					cbLoSelect.setModel((ComboBoxModel<String>) source);

					pw.setPacketId(PacketWork.PACKET_ID_CONFIGURATION_LO_BIAS_BOARD);
					pw.setPacketParameterHeaderCode(Packet.IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_LO_SET);
					pt.preparePacket();
				}else{
					cbLoSelect.setSelectedItem(new IdValueForComboBox((byte) source, null));
					setSend(false);
				}
			}
		}

	}
}
