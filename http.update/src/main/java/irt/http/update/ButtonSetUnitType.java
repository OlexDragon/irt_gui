package irt.http.update;

import static irt.http.update.HttpUpdateApp.PROPERTIES;
import static irt.http.update.HttpUpdateApp.UNIT_TYPES;
import static irt.http.update.HttpUpdateApp.UNIT_TYPE_START_WITH;
import static irt.http.update.HttpUpdateApp.PROPERTIES_FILE_PATH_KEY;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;

public class ButtonSetUnitType extends Button{
	private final static Logger logger = LogManager.getLogger();

	private final TextListener textListener = new TextListener();

	public ButtonSetUnitType(String id, Runnable action) {

		setText("Set Unit Type");

		Tooltip tooltip = new Tooltip("Unknown unit type: " + id);
		setTooltip(tooltip);

		setOnAction(
				a->{
					TextInputDialog dialog = new TextInputDialog("Unit Type");
					dialog.setTitle("Unit Type Setup Dialog");
					dialog.setHeaderText(null);
					dialog.setContentText("Please enter the Unit Type:");

					TextField textField = (TextField)dialog.getDialogPane().lookup(".text-field");
					textField.textProperty().addListener(textListener);
					

					dialog.showAndWait()
					.ifPresent(
							value->{
								ThreadBuilder.startThread(
										()->{

											String key = UNIT_TYPE_START_WITH + id;
											PROPERTIES.put(key, value.toLowerCase());

											try(final FileOutputStream os = new FileOutputStream(PROPERTIES.getProperty(PROPERTIES_FILE_PATH_KEY));){
											
												PROPERTIES.store(os, " Last change: " + key);

											} catch (IOException e) {
												logger.catching(e);
											}

											action.run();
										});
							});
				});
	}

	public class TextListener implements ChangeListener<String>{

		@Override
		public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

			TextField textField = (TextField) ((ReadOnlyProperty<?>)observable).getBean();

			// Return if selected text is deleted
			IndexRange indexRange = textField.getSelection();
			if(indexRange.getStart()==newValue.length())
				return;

			Optional.of(newValue).filter(v->!v.isEmpty())
			.map(String::toLowerCase)
			.flatMap(v->UNIT_TYPES.parallelStream().filter(t->t.startsWith(v)).findAny())
			.ifPresent(
					unitType->{
						observable.removeListener(this);
						textField.setText(unitType);
						observable.addListener(this);
						Platform.runLater(
								()->{
									textField.positionCaret(newValue.length());
									textField.selectEnd();
								});
					});
		}
	}
}
