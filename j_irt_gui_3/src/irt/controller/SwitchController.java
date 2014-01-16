package irt.controller;

import irt.controller.control.ControllerAbstract;
import irt.controller.serial_port.value.getter.GetterAbstract;
import irt.controller.serial_port.value.seter.SetterAbstract;
import irt.data.IdValue;
import irt.data.PacketThread;
import irt.data.PacketWork;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

public class SwitchController extends ControllerAbstract {

	private JCheckBox checkBox;
	private ActionListener actionListener;

	public SwitchController(String controllerName, JCheckBox checkBox, PacketWork packetWork) {
		super(controllerName, packetWork, null, null);
		this.checkBox = checkBox;
	}

	@Override
	protected void setListeners() {
		actionListener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				SetterAbstract as = (SetterAbstract) getPacketWork();
				PacketThread pt = as.getPacketThread();
				if(pt.getPacket()!=null){
					Object value = pt.getValue();

					if(value!=null){
						if(value instanceof Integer)
							as.preparePacketToSend(new IdValue(as.getPacketParameterHeaderCode(), ((Integer)value) == 0 ? 1 : 0));
						else
							as.preparePacketToSend(new IdValue(as.getPacketParameterHeaderCode(), ((Byte)value) == 0 ? (byte)1 : (byte)0));

						setSend(true);
					}
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
		checkBox.removeActionListener(actionListener);
		checkBox = null;
		actionListener = null;
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
			Object source = valueChangeEvent.getSource();
			int id = valueChangeEvent.getID();

			GetterAbstract as = (GetterAbstract)getPacketWork();
			if(id==as.getPacketId()){

				PacketThread pt = as.getPacketThread();
				pt.setValue(source);
				if(source instanceof Byte){
					checkBox.setSelected(((Byte)source)>0);
				}else
					checkBox.setSelected(((Integer)source)>0);

				if(checkBox.getActionListeners().length==0){
					checkBox.addActionListener(actionListener);
					checkBox.setEnabled(true);
				}
			}
		}

	}
}
