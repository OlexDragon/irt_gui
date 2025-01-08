package irt.tools.fx;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import irt.controller.serial_port.Baudrate;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.controller.translation.Translation;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class BaudRateSelectorFx extends AnchorPane{

	private static VBox staticVBox;
	private static ToggleGroup group;
	private static ChangeListener<? super Toggle> listener;

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

		group = new ToggleGroup();
		listener = (t,o,n)->ComPortThreadQueue.getSerialPort().setBaudrate((Baudrate)n.getUserData());
		group.selectedToggleProperty().addListener(listener);

		Arrays.stream(Baudrate.values())
		.forEach(
				baudrate->{

					final RadioButton rb = new RadioButton(baudrate.toString());
					rb.setUserData(baudrate);
					rb.setToggleGroup(group);
					rb.setSelected(baudrate==Baudrate.getDefaultBaudrate());

					vBox.getChildren().add(rb);
				});
		staticVBox = vBox;
	}

	public static void selectBaudrate(Baudrate baudrate) {
		Platform.runLater(
				()->{
					Optional.ofNullable(staticVBox).map(VBox::getChildren)
					.ifPresent(
							c->{
								for(Node n : c) {
									if(n.getUserData().equals(baudrate)) {

										group.selectedToggleProperty().removeListener(listener);
										final RadioButton radioButton = (RadioButton)n;
										radioButton.setSelected(true);
										group.selectedToggleProperty().addListener(listener);
										break;
									}
								}
							});
				});
		
	}
}
