package irt.tools.fx;

import java.io.IOException;
import java.util.Arrays;

import irt.controller.serial_port.Baudrate;
import irt.controller.translation.Translation;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class BaudRateSelectorFx extends AnchorPane{

	@FXML private VBox vBox;
	@FXML private TitledPane titledPane;

	public BaudRateSelectorFx() {
		Thread currentThread = Thread.currentThread();
		currentThread.setName(getClass().getSimpleName() + "-" + currentThread.getId());

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("BaudRateSelector.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	}

	@FXML protected void initialize() {

		titledPane.setText(Translation.getValue(String.class, "baudrates", "Baudrates"));

		final ToggleGroup group = new ToggleGroup();
		group.selectedToggleProperty().addListener((t,o,n)->Baudrate.setDefaultBaudrate((Baudrate)n.getUserData()));

		Arrays.stream(Baudrate.values())
		.forEach(
				baudrate->{

					final RadioButton rb = new RadioButton(baudrate.toString());
					rb.setUserData(baudrate);
					rb.setToggleGroup(group);
					rb.setSelected(baudrate==Baudrate.getDefaultBaudrate());

					vBox.getChildren().add(rb);
				});
	}
}
