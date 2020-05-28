package irt.calculator.harmonics;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class CalculatorHarmonicsApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        String fxmlFile = "/fxml/CalculatorHarmonics.fxml";

        FXMLLoader loader = new FXMLLoader();
        Parent rootNode = (Parent) loader.load(getClass().getResourceAsStream(fxmlFile));


        Scene scene = new Scene(rootNode, 400, 500);
        scene.getStylesheets().add("/styles/styles.css");

        stage.setTitle("Harmonics Calculator");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}