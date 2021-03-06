package irt.controller;

import irt.controller.control.ControllerAbstract;
import irt.data.PacketWork;
import irt.data.RegisterValue;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;

import java.awt.Component;

import javax.swing.JTextField;

public class DeviceDebagSaveController extends ControllerAbstract {

	private JTextField txtField;

	public DeviceDebagSaveController(JTextField txtField, PacketWork packetWork, Style stile) {
		super(packetWork, stile);
		this.txtField = txtField;

		Thread t = new Thread(this);
		int priority = t.getPriority();
		if(priority>Thread.MIN_PRIORITY)
			t.setPriority(priority-1);
		t.start();
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return new ValueChangeListener() {
			
			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				setRun(false);
				String str = txtField.getText();
				if(valueChangeEvent.getSource() instanceof RegisterValue)
					txtField.setText("Saved");
				else
					txtField.setText("Error");

				synchronized (this) { try { wait(500); } catch (InterruptedException e) { } }	
				txtField.setText(str);
			}
		};
	}

	@Override protected void setListeners() { }
	@Override protected boolean setComponent(Component component) { return false; }
}
