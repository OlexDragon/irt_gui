package irt.calculator.harmonics;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.calculator.harmonics.calculators.Calculator;
import irt.calculator.harmonics.calculators.Calculators;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

public class CalculatorHarmonicsController {
	private final Logger logger = LogManager.getLogger();

	private final static Preferences prefs = Preferences.userNodeForPackage(CalculatorHarmonicsApp.class);
    @FXML private VBox vBox;
	@FXML private TextArea taResult;

    @FXML void onCalculate() {
 
    	AtomicReference<Alert> referenceToWaitAlert = new AtomicReference<>();

    	// Show information message.
    	Platform.runLater(
    			()->{

    				Alert alert = new Alert(AlertType.INFORMATION, "There is a calculation. Wait a few seconds.", ButtonType.CANCEL);
    				referenceToWaitAlert.set(alert);
    				alert.showAndWait();
    			});

    	// Do the calculation
    	ThreadRunner.runThread(
    			()->{
    		    	try {

    		    		// get values of text fields
    		    		final Map<String, String> map = getAllTextFields()
    		    				.filter(tf->!tf.getText().trim().isEmpty())						// Filter empty text fields
    		    				.map(
    		    						tf->new SimpleEntry<>(tf.getId(), tf.getText()))
    		    				.collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));
 
    		    		final Calculator calculator = Calculators.getCalculator(map);
    		    		final String result = calculator.calculate();

    		    		Platform.runLater(
    		    				()->{
    		    					taResult.setText(result);
    		    					referenceToWaitAlert.get().hide();
    		    				});

    		    	} catch (NumberFormatException e) {
    		    		logger.catching(Level.INFO, e);

    					Platform.runLater(
    							()->{
    								referenceToWaitAlert.get().hide();

    								Alert alert = new Alert(AlertType.ERROR);
    								alert.setTitle("Error Dialog");
    								alert.setHeaderText("One of the text fields contains an error.");
    								alert.setContentText("Correct the error and try again.");

    								alert.showAndWait();
    							});
    				}
    			});
    }

    @FXML
    void onClear() {
    	getAllTextFields().forEach(textField->textField.setText(""));
    }

    @FXML public void initialize() {

    	// Find all TextFields
    	getAllTextFields().forEach(textField->Platform.runLater(

    			()->{

    				// Setup TextField text
    				final String key = getClass().getPackage() + "." + textField.getId();
					Optional.ofNullable(prefs.get(key, null)).ifPresent(textField::setText);

    				// Add Focus Listener
    				textField.focusedProperty()
    				.addListener(
    						(o,ov,nv)->{
 
    							if(nv)
    							// Do nothing on focus get
    								return;

    							// On focus lost save text of the textField to the System
    							final String text = textField.getText().trim();

    							if(text.isEmpty())
    								// If empty remove text from the System
    								prefs.remove(key);
    							else
    								// otherwise save text of the textField to the System
    								prefs.put(key, text);
    						});
    			}));
    }

	private Stream<TextField> getAllTextFields() {
		return vBox.lookupAll(".titled-pane").stream().map(n->(VBox)((TitledPane)n).getContent()).flatMap(box->box.lookupAll(".text-field").stream()).map(TextField.class::cast);
	}
}