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
import irt.gui.controllers.calibration.process.ValueInputPower;
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

public class PanelInputPowerCalibration implements CalibrationWindow{
	private static final String IP_PRECISION = "ip-precision";
	public final static NumberFormat FORMAT = new DecimalFormat("#0.0##");

	private final Logger logger = LogManager.getLogger();

	private static final String KEY = "var-input-power";
	private static final String IN_POWER_LUT = "in-power-lut-";

	private static final Preferences prefs = Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);
	private final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(new MyThreadFactory());

	private Future<?> future;

	private static final double MAX_ACCURACY = 0.5;//dBm

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

    		new NumericDoubleChecker(textFieldAccuracy.textProperty()).setMaximum(MAX_ACCURACY);
    		final String text = prefs.get(IP_PRECISION, "1");
			textFieldAccuracy.setText(text);

    		final ObservableList<Series<Number, Number>> data = lineChart.getData();
    		data.add(series);
    		data.add(seriesCompact);
    		series.setName("Input Power Calibration.");
    		seriesCompact.setName("Compact");

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

    private final double STEP = 0.1;
    private double step = STEP;
 
    @FXML void onKeyPressedAccuracy(KeyEvent event) {

    	if(future!=null && !future.isDone())
    		return;
 
   		double accuracy = Double.parseDouble(textFieldAccuracy.getText());

   		switch(event.getCode()){
   		case CONTROL:
   			step = STEP / 10;
   			return;
    	case UP:
     		if(accuracy == MAX_ACCURACY)
    			return;

     		accuracy += step;
    		if(accuracy>MAX_ACCURACY)
    			accuracy = MAX_ACCURACY;

    		break;

    	case DOWN:
     		if(accuracy == 0)
    			return;

     		accuracy -= step;
    		if(accuracy<0)
    			accuracy = 0;

      		break;
    	default:
    		return;
    	}

    	future = EXECUTOR.submit(()->{synchronized (this) { try { wait(100); } catch (Exception e) {}}});

		textFieldAccuracy.setText(FORMAT.format(accuracy));
    	onSetPrecision();
    }

    @FXML  void onKeyReleasedAccuracy(KeyEvent event) {
   		switch(event.getCode()){
   		case SHIFT:
   		case CONTROL:
   		case ALT:
   			step = STEP;
   			break;
		default:
   		}
    }

    @Override public void update(Observable o, Object arg) {

		if(arg instanceof ValueInputPower){
			try{
				final ValueInputPower valueInputPower = (ValueInputPower)arg;
				map.add(valueInputPower);
				Platform.runLater(()->{
					try{

						//Text Aria
						textAria.setText(map.toString(textFieldVariableName.getText()));
						receivedValueLabel.setText(arg.toString());

						//Chart
						final Data<Number, Number> data = new Data<>( valueInputPower.getKey(), valueInputPower.getValue());
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
