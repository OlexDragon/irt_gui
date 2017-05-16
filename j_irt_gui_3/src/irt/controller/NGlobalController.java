package irt.controller;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import irt.controller.control.ControllerAbstract;
import irt.controller.serial_port.value.getter.GetterAbstract;
import irt.controller.serial_port.value.setter.SetterAbstract;
import irt.data.RegisterValue;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.interfaces.PacketThreadWorker;
import irt.data.packet.interfaces.PacketWork;
import irt.data.value.Value;

public class NGlobalController extends ControllerAbstract {

	private JCheckBox checkBox;
	private ActionListener actionListener;

	public NGlobalController(int deviceType, JCheckBox checkBox, PacketWork packetWork) {
		super(deviceType, "NGlobalController", packetWork, null, null);
		setListeners();
		this.checkBox = checkBox;
		checkBox.addActionListener(actionListener);
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return new ValueChangeListener() {

			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				int id = valueChangeEvent.getID();
				if(id==((GetterAbstract)getPacketWork()).getPacketId())
					new ControllerWorker(valueChangeEvent);
			}
		};
	}

	@Override
	protected void setListeners() {
		actionListener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					SetterAbstract as = (SetterAbstract) getPacketWork();
					PacketThreadWorker pt = as.getPacketThread();
					if (pt.getPacket() == null)
						return;
					RegisterValue rv = ((RegisterValue) pt.getValue());
					Value v = rv.getValue();

					if (v == null)
						rv.setValue(new Value(NGlobalController.this.checkBox.isSelected() ? 1 : 0, 0, 1, 0));
					else
						v.setValue(NGlobalController.this.checkBox.isSelected() ? 1 : 0);

					as.preparePacketToSend(rv);
					setSend(true);
				} catch (Exception ex) {
					logger.catching(ex);
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
		checkBox.removeActionListener(actionListener);
		checkBox = null;
		actionListener = null;
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
			Object source = valueChangeEvent.getSource();
			if(source instanceof RegisterValue){
				RegisterValue sv = (RegisterValue)source;
				checkBox.setSelected((sv.getValue().getValue()&1)==1);
			}
		}

	}
}
