package irt.fx.control.serial.port;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.WindowEvent;

/** @author Oleksandr
 * 
 * Close serial port on window hiding
 */
public class SceneHideListener implements ChangeListener<Scene>{
	Logger logger = LogManager.getLogger();

	private EventHandler<? super WindowEvent> eventHandler;

	public SceneHideListener(EventHandler<? super WindowEvent> eventHandler) {
		this.eventHandler = eventHandler;
	}

	@Override
	public void changed(ObservableValue<? extends Scene> observable, Scene oldScene, Scene newScene) {

		Optional
		.ofNullable(newScene)

		.ifPresent(scene->//combo box is added to the scene
						scene
						.windowProperty()

						.addListener((o, oldWind, newWind)->//scene is added to the window

											{
												Optional
												.ofNullable(newWind)

												.ifPresent(wind->//on window hiding close serial port
																wind.addEventHandler(WindowEvent.WINDOW_HIDING, eventHandler) );

												logger.info("Scene added to the window");
											}
						)
				);

		logger.info("Component added to the scen.");
	}

}
