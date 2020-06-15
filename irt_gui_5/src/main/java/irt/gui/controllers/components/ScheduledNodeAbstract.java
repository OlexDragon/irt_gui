package irt.gui.controllers.components;

import irt.gui.controllers.interfaces.ScheduledNode;
import javafx.application.Platform;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;

public abstract class ScheduledNodeAbstract extends StartStopAbstract implements ScheduledNode{

	public static final String NAME = "name";
	public static final String PERIOD = "period";

	protected 				String 				propertyName; 			public String getPropertyName() { return propertyName; }

	//******************************** class TooltipWorker   ********************************************
	class TooltipWorker implements Runnable{

		private Control node;
		private String message;
		private volatile boolean isRunning;

		public TooltipWorker(Control node) {
			this.node = node;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		@Override
		public void run() {
			if(isRunning)
				return;

			isRunning = true;

			final Tooltip tooltip = node.getTooltip();
			Platform.runLater(()->node.setTooltip(new Tooltip(message)));

			try { Thread.sleep(10000); } catch (Exception e) { }

			Platform.runLater(()->node.setTooltip(tooltip));

			isRunning = false;
		}
		
	}
}
