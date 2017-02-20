
package irt.calibration.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import irt.calibration.enums.CalibrationType;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart.Data;
import javafx.stage.Stage;

public class ProgressChartFxTest extends ApplicationTest {
	private final Logger logger = LogManager.getLogger();

	private ProgressChartFx progressChartFx;

	@Override
	public void start(Stage stage) throws Exception {
		logger.entry("\n\n ************************ start(Stage stage) ******************************** ");
		try {

			progressChartFx = new ProgressChartFx();
			progressChartFx.setCalibrationType(CalibrationType.INPUT_PORER);

			stage.setScene(new Scene(progressChartFx));
			stage.show();

			/* Do not forget to put the GUI in front of windows. Otherwise, the robots may interact with another  window, the one in front of all the windows... */ 
			stage.toFront(); 

		} catch (Exception e) {
			logger.catching(e);
		}
		logger.traceExit("\n ************************ start(Stage stage) ********************************\n ");
	}

	@Test(expected=IllegalArgumentException.class)
	public void test() {

		try {

			progressChartFx.addDataPoit(new Data<Number, Number>(-10.5, 10));
			ObservableList<Data<Number, Number>> data = progressChartFx.getData();

			assertEquals(1, data .size());
			assertTrue((boolean) data.get(0).getExtraValue());

			progressChartFx.addDataPoit(new Data<Number, Number>(-7, 20));
			progressChartFx.addDataPoit(new Data<Number, Number>(-7, 21));
			progressChartFx.addDataPoit(new Data<Number, Number>(-7, 22));
			progressChartFx.addDataPoit(new Data<Number, Number>(-9.5, 0));
			logger.trace("{}", data);
			assertEquals(0, data.size());

			progressChartFx.addDataPoit(new Data<Number, Number>(-8.5, 5));

			logger.trace("{}", data);
			assertEquals(1, data.size());
			assertTrue((boolean) data.get(0).getExtraValue());

			progressChartFx.addDataPoit(new Data<Number, Number>(-7, 20));
			progressChartFx.addDataPoit(new Data<Number, Number>(-7, 21));
			progressChartFx.addDataPoit(new Data<Number, Number>(-7, 22));
			progressChartFx.addDataPoit(new Data<Number, Number>(-6, 25));
			progressChartFx.addDataPoit(new Data<Number, Number>(-5, 24));

//			sleep(10000);
			logger.trace("{}", data);
			assertEquals(6, data.size());
			assertFalse((boolean) data.get(data.size() - 1).getExtraValue());

		}catch (Exception e) { throw new RuntimeException(e); }

		progressChartFx.addDataPoit(new Data<Number, Number>(-4, 23));
	}
}
