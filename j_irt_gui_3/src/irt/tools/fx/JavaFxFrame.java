
package irt.tools.fx;

import java.awt.HeadlessException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
	final private JFXPanel fxPanel;
	private Scene scene;

	/**
	 * @wbp.parser.constructor
	 */
	public JavaFxFrame(Parent root) throws HeadlessException {
		this(root, null);
	}

	public JavaFxFrame(Parent root, JMenu menu) throws HeadlessException {

		fxPanel = new JFXPanel();
		getContentPane().add(fxPanel);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setVisible(true);
		
		Optional.ofNullable(menu).ifPresent(
				m->{
					JMenuBar menuBar = new JMenuBar();
					setJMenuBar(menuBar);
		
					this.menu = menu;
					menuBar.add(menu);
				});

		Platform.runLater(
				()->{
					scene = new Scene(root);
					fxPanel.setScene(scene);
				});

		Optional.ofNullable(root).filter(JavaFxPanel.class::isInstance).map(JavaFxPanel.class::cast).ifPresent(JavaFxPanel::start);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				Platform.runLater(()->scene.getWindow().hide());
			}
		});
	}

	public JMenu getMenu() {
		return menu;
	}
}
