
package irt.calibration.components;

import java.io.IOException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.calibration.enums.CalibrationType;
import irt.data.IrtGuiProperties;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.layout.AnchorPane;

public class ProgressChartFx extends AnchorPane {
	private final Logger logger = LogManager.getLogger();

    @FXML private LineChart<Number, Number> lineChart;
	@FXML private NumberAxis 	xAxis;
    @FXML private NumberAxis 	yAxis;

    private final Series<Number, Number> seriesRaw = new Series<>();
    private final Series<Number, Number> seriesCompact = new Series<>();
    																		public ObservableList<Data<Number, Number>> getData() {
    																			return FXCollections.unmodifiableObservableList(seriesCompact.getData());
    																		}

	private CalibrationType calibrationType;
    private int startSize = 5;
    									public int getStartLength() { return startSize; }
    									public void setStartLength(int startLength) { this.startSize = startLength; }

	public ProgressChartFx() {

    	FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/components/progress_chart.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

	@FXML private void initialize(){
		
		seriesCompact.setName(IrtGuiProperties.BUNDLE.getString("result"));
		final ObservableList<Series<Number, Number>> data = lineChart.getData();
		data.add(seriesRaw);
		data.add(seriesCompact);
	}

	public void setCalibrationType(CalibrationType calibrationType) {
		this.calibrationType = calibrationType;

		lineChart.setTitle(calibrationType.getTitle());

		xAxis.setLabel(calibrationType.getValueName());

		seriesRaw.setName(calibrationType.getTitle() + " " + IrtGuiProperties.BUNDLE.getString("calibration"));
	}

	public boolean addDataPoit(Data<Number, Number> point){

		final ObservableList<Data<Number, Number>> dataRow 		= seriesRaw		.getData();

		boolean test = true;
		if(dataRow.size()>0){
			final BiPredicate<Number, Number> 	predicate 	= calibrationType.getPredicate();
			final Data<Number, Number> 			data 		= dataRow.get(dataRow.size()-1);
												test 		= predicate.test(point.getYValue(), data.getYValue());
		}

		point.setExtraValue(test);

		accept(point);

		FutureTask<Boolean> ft = new FutureTask<>(()->dataRow.add(point));
		Platform.runLater(ft);

		try { ft.get(1, TimeUnit.SECONDS); } catch (Exception e) { logger.catching(e); }

		return test;
	}

	private void accept(final Data<Number, Number> point) {
		logger.entry(point);
//		final String format = String.format("x=%s; y=%s; accepted=%s", point.getXValue(), point.getYValue(), point.getExtraValue());
//		Platform.runLater(()->lblCurrentValue.setText(format));

		final boolean extraValue = (boolean) point.getExtraValue();
		final ObservableList<Data<Number, Number>> 				 data = seriesCompact.getData();
		final boolean 								isLessThenMinSize = data.size()<startSize;

		if(!extraValue && isLessThenMinSize){
			Platform.runLater(()->data.clear());
		}

		if(!(data.isEmpty() || ((boolean)data.get(data.size()-1).getExtraValue())))
			throw new IllegalArgumentException();

		if(extraValue || !isLessThenMinSize)
			Platform.runLater(()->data.add(new Data<Number, Number>(point.getXValue(), point.getYValue(), point.getExtraValue())));
	}
}
