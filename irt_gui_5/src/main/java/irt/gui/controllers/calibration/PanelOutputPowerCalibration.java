package irt.gui.controllers.calibration;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.IrtGuiProperties;
import irt.gui.controllers.calibration.process.CalibrationMap;
import irt.gui.controllers.calibration.process.ValueOutputPower;
import irt.gui.controllers.interfaces.CalibrationWindow;
import irt.gui.data.MyThreadFactory;
import irt.gui.data.listeners.NumericDoubleChecker;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;

public class PanelOutputPowerCalibration implements CalibrationWindow{
	private static final String IP_PRECISION = "ip-precision";
	public final static NumberFormat FORMAT = new DecimalFormat("#0.0##");

	private final Logger logger = LogManager.getLogger();

	private static final String KEY = "var-output-power";
	private static final String IN_POWER_LUT = "out-power-lut-";

	private static final Preferences prefs = Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);
	private final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(new MyThreadFactory());

	private Future<?> future;

	private static final double MAX_ANGLE = 5.0;//degrees

	private Runnable closeMethod;
	private final CalibrationMap map = new CalibrationMap();



    @FXML private BorderPane borderPane;
    @FXML private ScrollPane scrollPane;
    @FXML private TextArea  textAria;
    @FXML private TextField textFieldVariableName;
    @FXML private Label 	receivedValueLabel;
    @FXML private TextField textFieldAccuracy;

    @FXML private LineChart<Number, Number> lineChart;
    @FXML private NumberAxis xAxis;
    @FXML private NumberAxis yAxis;

    private final Series<Number, Number> series = new Series<>();
    private final Series<Number, Number> seriesCompact = new Series<>();

    @FXML public void initialize() {
    	try{

    		final String var = prefs.get(KEY, IN_POWER_LUT);
    		textFieldVariableName.setText(var);
    		textFieldVariableName.focusedProperty().addListener((o,ald, focous)->Optional.of(focous).filter(f->!f).ifPresent(f->prefs.put(KEY, textFieldVariableName.getText())));

    		new NumericDoubleChecker(textFieldAccuracy.textProperty()).setMaximum(MAX_ANGLE);
    		final String text = prefs.get(IP_PRECISION, "1");
			textFieldAccuracy.setText(text);

    		final ObservableList<Series<Number, Number>> data = lineChart.getData();
    		data.add(series);
    		data.add(seriesCompact);
    		series.setName("Output Power Calibration.");
    		seriesCompact.setName("Compact");
    		logger.error("{}", lineChart.getStyleClass());

    	}catch(Exception e){
    		logger.catching(e);
    	}
    }

    @FXML  void onSetVariableName() {
		final double angle = Double.parseDouble(textFieldAccuracy.getText());
		final CalibrationMap with = map.getWith(angle);
		textAria.setText(with.toString(textFieldVariableName.getText()));

		final ObservableList<Data<Number, Number>> data = seriesCompact.getData();
		data.clear();

		with
		.entrySet()
		.forEach(e->data.add(new Data<Number, Number>(e.getKey(), e.getValue())));
    }


    @FXML void onSetPrecision() {
    	prefs.putDouble(IP_PRECISION, Double.parseDouble(textFieldAccuracy.getText()));
    	onSetVariableName();

    	
    }

    @FXML void onMenuDefaultVariable(ActionEvent event) {
    	textFieldVariableName.setText(IN_POWER_LUT);
		prefs.remove(KEY);
    }

    private final double MIN_STEP = 0.001;
    private double step = MIN_STEP;

    @FXML void onKeyPressedAccuracy(KeyEvent event) {
  		logger.error(event.getCode());

    	if(future!=null && !future.isDone())
    		return;
 
   		double angle = Double.parseDouble(textFieldAccuracy.getText());

   		switch(event.getCode()){
   		case SHIFT:
   			step = MIN_STEP * 10;
   			return;
   		case CONTROL:
   			step = MIN_STEP * 100;
   			return;
   		case ALT:
   			step = MIN_STEP * 1000;
   			return;
    	case UP:
     		if(angle == MAX_ANGLE)
    			return;

    		angle += step;
    		if(angle>MAX_ANGLE)
    			angle = MAX_ANGLE;

    		break;

    	case DOWN:
     		if(angle == 0)
    			return;

    		angle -= step;
    		if(angle<0)
    			angle = 0;

      		break;
    	default:
    		return;
    	}

    	future = EXECUTOR.submit(()->{synchronized (this) { try { wait(100); } catch (Exception e) {}}});

		textFieldAccuracy.setText(FORMAT.format(angle));
    	onSetPrecision();
    }

    @FXML  void onKeyReleasedAccuracy(KeyEvent event) {
   		switch(event.getCode()){
   		case SHIFT:
   		case CONTROL:
   		case ALT:
   			step = MIN_STEP;
   			break;
		default:
   		}
    }

    @Override public void update(Observable o, Object arg) {
    	logger.entry(arg);

		if(arg instanceof ValueOutputPower){
			try{
				final ValueOutputPower valueOutputPower = (ValueOutputPower)arg;
				map.add(valueOutputPower);
				Platform.runLater(()->{
					try{

						//Text Aria
						textAria.setText(map.toString(textFieldVariableName.getText()));
						receivedValueLabel.setText(arg.toString());

						//Chart
						final Data<Number, Number> data = new Data<>( valueOutputPower.getKey(), valueOutputPower.getValue());
						final ObservableList<Data<Number, Number>> datas = series.getData();
						datas.add(data);

					}catch(Exception e){
						logger.catching(e);
					}
				});
			}catch(Exception e){

				logger.catching(e);
				try { closeMethod.run(); } catch (Exception e1) { logger.catching(e); }
				Platform.runLater(()->{
					receivedValueLabel.setText("Done");
					final ObservableList<Data<Number, Number>> data = series.getData();
					data
					.parallelStream()
					.filter(p->!map.containsKey(p.getXValue()))
					.forEach(p->p.extraValueProperty());

					xAxis.setAutoRanging(false);
					yAxis.setAutoRanging(false);
					Entry<Number, Number> entry = map.firstEntry();
					xAxis.setLowerBound(entry.getKey().intValue());
					yAxis.setLowerBound(entry.getValue().intValue());

					 entry = map.lastEntry();
					xAxis.setUpperBound(entry.getKey().intValue());
					yAxis.setUpperBound(entry.getValue().intValue());

					onSetPrecision();
				});
			}
		}
	}

	@Override public void setCloseMethod(Runnable closeMethod) {
		this.closeMethod = closeMethod;
	}
}
