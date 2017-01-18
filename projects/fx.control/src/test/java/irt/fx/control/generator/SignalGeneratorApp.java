package irt.fx.control.generator;

import irt.fx.control.prologix.PrologixFx;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class SignalGeneratorApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

    	VBox root = new VBox();
        final ObservableList<Node> vBoxChildrens = root.getChildren();

        PrologixFx prologix = new PrologixFx();
    	VBox.setVgrow(prologix, Priority.ALWAYS);
        vBoxChildrens.add(prologix);

        SignalGeneratorFx signalGeneratorFx = new SignalGeneratorFx();
    	VBox.setVgrow(signalGeneratorFx, Priority.ALWAYS);
        vBoxChildrens.add(signalGeneratorFx);
        signalGeneratorFx.setPrologix(prologix);
        
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
