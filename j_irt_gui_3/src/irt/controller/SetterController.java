package irt.controller;

import irt.controller.control.ControllerAbstract;
import irt.controller.interfaces.ToDo;
import irt.data.PacketWork;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;

import java.awt.Component;

public class SetterController extends ControllerAbstract {

	private ToDo toDo;
/**
 * This thread run once
 * @param packetWork should be command 
 * @param style Controller work style 
 */
	public SetterController(PacketWork packetWork, ToDo toDo, Style style) {
		super(packetWork, null, style);
		this.toDo = toDo;

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
				toDo.doIt(valueChangeEvent);
			}
		};
	}

	@Override protected void setListeners() {}
	@Override protected boolean setComponent(Component component) { return false; }

}
