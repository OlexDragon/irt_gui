package irt.fx.control.buc;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

public class ConverterFx extends AnchorPane{

	public static final String CONV_ADDR = "convAddr";

	public ConverterFx() {

    	FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/converter.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
        	fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	}

    @FXML private void initialize() {}
}
