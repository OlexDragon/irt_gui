package irt.fx.control.buc;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class ConverterApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

    	final ConverterFx converterFx = new ConverterFx();
        
        Scene scene = new Scene(converterFx);
        
        stage.setTitle("BUC Controller");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
