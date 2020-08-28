package irt.controller;

import java.awt.Component;
import java.util.Optional;

import irt.controller.control.ControllerAbstract;
import irt.controller.interfaces.ToDo;
import irt.data.DeviceInfo.DeviceType;
import irt.data.ThreadWorker;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.PacketWork;

public class SetterController extends ControllerAbstract {

	private ToDo toDo;
/**
 * This thread run once
 * @param packetWork should be command 
 * @param style UnitController work style 
 */
	public SetterController(Optional<DeviceType> deviceType, String controllerName, PacketWork packetWork, ToDo toDo, Style style) {
		super(deviceType, controllerName, packetWork, null, style);
		this.toDo = toDo;

		new ThreadWorker(this, "SetterController");
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
			try{
				toDo.doIt(valueChangeEvent);
			}catch (Exception e) {
				logger.catching(e);
			}
		}

	}
}
