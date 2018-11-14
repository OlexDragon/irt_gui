
package irt.tools.fx;

import java.awt.HeadlessException;
import java.util.Optional;

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

	public JavaFxFrame(Parent root, JMenu menu) throws HeadlessException {
		final JFXPanel fxPanel = new JFXPanel();
		getContentPane().add(fxPanel);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setVisible(true);
		
		Optional.ofNullable(menu).ifPresent(m->{
			JMenuBar menuBar = new JMenuBar();
			setJMenuBar(menuBar);
		
			this.menu = menu;
			menuBar.add(menu);
		});

		Platform.runLater(()->{
	        Scene scene = new Scene(root);
	        fxPanel.setScene(scene);
		});

		Optional.ofNullable(root).filter(JavaFxPanel.class::isInstance).map(JavaFxPanel.class::cast).ifPresent(JavaFxPanel::start);
	}

	public JMenu getMenu() {
		return menu;
	}

}
