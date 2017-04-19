
package irt.tools.fx;

import java.awt.HeadlessException;
import java.util.function.Supplier;

import javax.swing.JFrame;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class JavaFxFrame extends JFrame {
	private static final long serialVersionUID = 8449021038878505206L;

	public JavaFxFrame(Supplier<Parent> root) throws HeadlessException {
		final JFXPanel fxPanel = new JFXPanel();
		add(fxPanel);
		setVisible(true);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		Platform.runLater(()->{
	        Scene scene = new Scene(root.get());
	        fxPanel.setScene(scene);
		});
	}

}
