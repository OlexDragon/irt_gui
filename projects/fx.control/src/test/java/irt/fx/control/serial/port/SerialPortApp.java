package irt.fx.control.serial.port;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;


public class SerialPortApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

    	final HBox serialPort = new SerialPortFX();
        
        Scene scene = new Scene(serialPort);
        
        stage.setTitle("Serial Port Controller");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
