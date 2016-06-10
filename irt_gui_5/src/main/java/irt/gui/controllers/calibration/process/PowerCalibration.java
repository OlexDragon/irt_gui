
package irt.gui.controllers.calibration.process;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.controllers.calibration.PanelBUC;
import irt.gui.controllers.calibration.PanelPowerMeter;
import irt.gui.controllers.calibration.PanelSignalGenerator;
import irt.gui.controllers.calibration.enums.Calibration;
import irt.gui.controllers.calibration.tools.Tool;
import irt.gui.controllers.interfaces.CalibrationProcess;
import irt.gui.controllers.interfaces.CalibrationWindow;
import irt.gui.data.MyThreadFactory;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class PowerCalibration implements CalibrationProcess {

	private final Logger logger = LogManager.getLogger();

	private final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(new MyThreadFactory());

	private boolean completed;
	private SequentialProcess task;

	private boolean input;
	private boolean output;

	private Runnable stopAction;

	private List<Calibration> calibrations;

	@Override public boolean isCompleted() {
		return completed;
	}

	@Override public boolean inProgress() {
		return task!=null;
	}

	@Override public boolean setMode(List<Calibration> calibrations) {

		this.calibrations = calibrations;

		return Optional
				.ofNullable(calibrations)
				.filter(cs->!cs.isEmpty())
				.map(cs -> cs
						.parallelStream()
						.map(c -> {

							boolean error = false;

							switch (c) {
							case INPUT_POWER:
								input = true;
								break;
							case OUTPUT_POWER:
								output = true;
								break;
							default:
								error = true;
							}

							return error;
						}).allMatch(b -> b == false))//no error
				.orElse(false);
	}

	@Override public void start(List<Tool> tools, double step, int steps) {

		Objects.requireNonNull(tools);
		Objects.requireNonNull(calibrations);

		if(task!=null || calibrations.isEmpty() || step < 0.1 || steps < 10)
			return;

		task = new SequentialProcess(calibrations, step, steps);
		EXECUTOR.execute(task);

		tools
		.parallelStream()
		.forEach(t->{
			if(t instanceof PanelSignalGenerator)
				task.setSignalGenerator(t);
			else if(output && t instanceof PanelPowerMeter)
				task.setPowerMeter(t);
			else if(t instanceof PanelBUC)
				task.setBuc(t);
		});

		showStages();
	}

	private void showStages() {
		if(input)
			showInputPowerStage();

		if(output)
			showOutputPowerStage();
	}

	private void showOutputPowerStage() {
		showStage("/fxml/calibration/PanelOutputPowerCalibration.fxml", "Output Power");
	}

	private void showInputPowerStage() {
		showStage("/fxml/calibration/PanelInputPowerCalibration.fxml", "Input Power");
	}

	private void showStage(final String fxmlPath, final String title) {
		final URL resource = getClass().getResource(fxmlPath);
		FXMLLoader loader = new FXMLLoader(resource);  

		Platform.runLater(()->{
			try {

				Parent root = (Parent) loader.load();

				CalibrationWindow calibrationWindow = (CalibrationWindow)loader.getController();
				task.addObserver(calibrationWindow);
				calibrationWindow.setCloseMethod(()->stop());

				Scene scene = new Scene(root);
				Stage stage = new Stage();
				stage.setScene(scene);
				stage.setTitle(title);
				stage.show();
				stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, e->stop());

			} catch (IOException e) {
				logger.catching(e);
			}
		});
	}

	@Override
	public synchronized void stop() {

		Optional
		.ofNullable(task)
		.ifPresent(t->{
			task = null;
			t.deleteObservers();
			t.stop();
		});

		Optional
		.ofNullable(stopAction)
		.ifPresent(a->{
			stopAction = null;
			a.run();
		});
	}

	@Override
	public void setStopAction(Runnable stopAction) {
		this.stopAction = stopAction;
	}
}
