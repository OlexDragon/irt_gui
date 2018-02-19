package irt.tools.fx;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import irt.controller.serial_port.ComPortThreadQueue;
import irt.controller.serial_port.MyComPort.Baudrate;
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
		group.selectedToggleProperty().addListener((t,o,n)->{
			Optional.ofNullable(ComPortThreadQueue.getSerialPort()).ifPresent(sp->{
				sp.setBaudrate(((Baudrate)n.getUserData()).getBaudrate());
			});
		});

		final int baudrate = ComPortThreadQueue.getSerialPort().getBaudrate();

		Arrays.stream(Baudrate.values()).forEach(v->{
			final RadioButton rb = new RadioButton(v.toString());
			rb.setUserData(v);
			rb.setToggleGroup(group);
			rb.setSelected(baudrate==v.getBaudrate());
			vBox.getChildren().add(rb);
		});
	}
}
