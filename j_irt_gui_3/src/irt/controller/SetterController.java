package irt.controller;

import irt.controller.control.ControllerAbstract;
import irt.controller.interfaces.ToDo;
import irt.data.PacketWork;
import irt.data.RundomNumber;
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
	public SetterController(int deviceType, String controllerName, PacketWork packetWork, ToDo toDo, Style style) {
		super(deviceType, controllerName, packetWork, null, style);
		this.toDo = toDo;

		Thread t = new Thread(this, "SetterController-"+new RundomNumber().toString());
		int priority = t.getPriority();
		if(priority>Thread.MIN_PRIORITY)
			t.setPriority(priority-1);
		t.setDaemon(true);
		t.start();
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

	@Override protected void setListeners() {}
	@Override protected boolean setComponent(Component component) { return false; }


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
			toDo.doIt(valueChangeEvent);
		}

	}
}
