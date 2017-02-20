package irt.fx.control.buc;

import irt.serial.port.fx.SerialPortSelectorFx;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class LabelMeasurementInputPowerApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

    	VBox root = new VBox();
        final ObservableList<Node> vBoxChildrens = root.getChildren();

        SerialPortSelectorFx portFX = new SerialPortSelectorFx();
    	VBox.setVgrow(portFX, Priority.ALWAYS);
        vBoxChildrens.add(portFX);

        LabelMeasurementInputPowerFx inputPowerFx = new LabelMeasurementInputPowerFx();
    	VBox.setVgrow(inputPowerFx, Priority.ALWAYS);
        vBoxChildrens.add(inputPowerFx);
         
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        
        stage.setTitle("JavaFX and Maven");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
