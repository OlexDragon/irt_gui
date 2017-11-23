
package irt.tools.fx;

import java.awt.HeadlessException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class JavaFxFrame extends JFrame {
	private static final long serialVersionUID = 8449021038878505206L;
	private JMenu menu;

	public JavaFxFrame(Parent root) throws HeadlessException {
		final JFXPanel fxPanel = new JFXPanel();
		getContentPane().add(fxPanel);
		setVisible(true);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		menu = new JMenu("Menu");
		menuBar.add(menu);

		Platform.runLater(()->{
	        Scene scene = new Scene(root);
	        fxPanel.setScene(scene);
		});

		if(root instanceof JavaFxPanel)
			((JavaFxPanel)root).start();
	}

	public JMenu getMenu() {
		return menu;
	}

}
