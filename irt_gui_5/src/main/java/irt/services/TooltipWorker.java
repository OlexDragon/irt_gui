package irt.services;

import javafx.application.Platform;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

//******************************** class TooltipWorker   ********************************************
/** Changes the tooltip to the specified value and after 10 seconds reverts to the previous value.  */
@RequiredArgsConstructor
public class TooltipWorker implements Runnable {

	private final Control node;

	private volatile boolean isRunning;
	@Setter private String message;

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
