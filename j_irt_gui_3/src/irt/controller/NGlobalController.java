package irt.controller;

import irt.controller.control.ControllerAbstract;
import irt.controller.serial_port.value.getter.GetterAbstract;
import irt.controller.serial_port.value.seter.SetterAbstract;
import irt.data.PacketThread;
import irt.data.PacketWork;
import irt.data.RegisterValue;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.value.Value;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

public class NGlobalController extends ControllerAbstract {

	private JCheckBox checkBox;
	private ActionListener actionListener;

	public NGlobalController(JCheckBox checkBox, PacketWork packetWork) {
		super(packetWork, null, null);
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
				if(id==((GetterAbstract)getPacketWork()).getPacketId()){

					Object source = valueChangeEvent.getSource();
					if(source instanceof RegisterValue){
						RegisterValue sv = (RegisterValue)source;
						checkBox.setSelected((sv.getValue().getValue()&1)==1);
					}
				}
			}
		};
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
				RegisterValue rv = ((RegisterValue)pt.getValue());
				Value v = rv.getValue();

				if(v==null)
					rv.setValue(new Value(NGlobalController.this.checkBox.isSelected() ? 1 : 0, 0, 1, 0));
				else
					v.setValue(NGlobalController.this.checkBox.isSelected() ? 1 : 0);

				as.preparePacketToSend(rv);
				setSend(true);
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
}
