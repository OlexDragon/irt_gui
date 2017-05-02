
package irt.tools.fx;

import java.awt.HeadlessException;
import java.util.function.Supplier;

import javax.swing.JFrame;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javax.swing.JMenuBar;
import javax.swing.JMenu;

public class JavaFxFrame extends JFrame {
	private static final long serialVersionUID = 8449021038878505206L;
	private JMenu menu;

	public JavaFxFrame(Supplier<Parent> root) throws HeadlessException {
		final JFXPanel fxPanel = new JFXPanel();
		getContentPane().add(fxPanel);
		setVisible(true);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		menu = new JMenu("Menu");
		menuBar.add(menu);

		Platform.runLater(()->{
	        Scene scene = new Scene(root.get());
	        fxPanel.setScene(scene);
		});
	}

	public JMenu getMenu() {
		return menu;
	}

}
