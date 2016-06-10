package irt.gui.controllers.calibration;

import irt.gui.controllers.interfaces.FieldController;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

public class PanelCallibration implements FieldController{

	@FXML private AnchorPane anchorPaneCallibrationl;
	@FXML private PanelTools toolsController;
	@FXML private PanelTasks tasksController;

	@FXML protected void initialize() {
		anchorPaneCallibrationl.setUserData(this);
		tasksController.setTools(toolsController);
	}

	@Override
	public void doUpdate(boolean doUpdate) {
		toolsController.doUpdate(doUpdate);
	}
}
