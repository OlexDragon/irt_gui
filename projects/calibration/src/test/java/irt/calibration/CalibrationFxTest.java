
package irt.calibration;

import static org.junit.Assert.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import irt.calibration.enums.CalibrationType;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CalibrationFxTest extends ApplicationTest {
	private final Logger logger = LogManager.getLogger();
	private CalibrationFx calibrationFx;

	@Override
	public void start(Stage stage) throws Exception {
		logger.entry("\n\n ************************ start(Stage stage) ******************************** ");
		try {

			calibrationFx = new CalibrationFx();

			stage.setScene(new Scene(calibrationFx));
			stage.show();

			/* Do not forget to put the GUI in front of windows. Otherwise, the robots may interact with another  window, the one in front of all the windows... */ 
			stage.toFront(); 

		} catch (Exception e) {
			logger.catching(e);
		}
		logger.traceExit("\n ************************ start(Stage stage) ********************************\n ");
	}

	@Test
	public void test() {
		ChoiceBox<CalibrationType> chb = lookup("#chbCalibrationType").query();
		assertEquals(chb.getItems().size(), CalibrationType.values().length);
	}

	@Test public void onEscapeTest(){

		TextField tf = lookup("#tfTableName").query();

		if(!tf.isFocused())
			clickOn(tf);

//		verifyThat(tf, hasText(CalibrationType.INPUT_PORER.getTableName()));
//
//		tf.setText("is written");
////		write("is written");
//		verifyThat(tf, hasText("is written"));
//
//		type(KeyCode.ESCAPE);
//		verifyThat(tf, hasText(CalibrationType.INPUT_PORER.getTableName()));
	}
}
