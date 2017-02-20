package irt.calibration;

import java.io.IOException;
import java.util.prefs.Preferences;

import irt.calibration.components.ProgressChartFx;
import irt.calibration.enums.CalibrationType;
import irt.data.IrtGuiProperties;
import irt.services.listeners.NumericDoubleChecker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

public class CalibrationFx extends BorderPane {

	private static final double MAX_ACCURACY = 0.5;
	private static final String prefKey = "cal_type";

	private static final Preferences prefs = Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);

    @FXML private ChoiceBox<CalibrationType> chbCalibrationType;
	@FXML private TextField 	tfTableName;
	@FXML private TextField 	tfAccuracy;
	@FXML private Label 			lblCurrentValue;
	@FXML private ProgressChartFx 	progressChart;

	public CalibrationFx() {

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/calibration.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	@FXML public void initialize() {
		chbCalibrationType.getItems().addAll(CalibrationType.values());
		final int index = prefs.getInt(prefKey, -1);
		chbCalibrationType.getSelectionModel().select(index);

		tfTableName.focusedProperty().addListener(e->{ if(tfTableName.isFocused()) tfTableName.selectAll(); });

		new NumericDoubleChecker(tfAccuracy.textProperty()).setMaximum(MAX_ACCURACY);
		tfAccuracy.setText(Double.toString(MAX_ACCURACY));

		onEscape();
	}

    @FXML
    void onSelect() {
    	final SingleSelectionModel<CalibrationType> selectionModel = chbCalibrationType.getSelectionModel();
		final int index = selectionModel.getSelectedIndex();
    	prefs.putInt(prefKey, index);

    	progressChart.setCalibrationType(selectionModel.getSelectedItem());
    }

	@FXML void onEscape() {
		// txtTableName.setText(calibrationType.getTableName());
	}
}
