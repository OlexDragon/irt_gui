
package irt.tools.fx;

import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.tools.panel.subpanel.monitor.Monitor;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class JavaFxWrapper extends JFXPanel implements Monitor {
	private static final long serialVersionUID = 1157429339979438261L;
	private final Logger logger = LogManager.getLogger();

	private JavaFxPanel root;

	public JavaFxWrapper(JavaFxPanel javaFxPanel) {

		root = javaFxPanel;

		addHierarchyListener(new HierarchyListener() {
			public void hierarchyChanged(HierarchyEvent e) {
				if((e.getChangeFlags()&HierarchyEvent.PARENT_CHANGED)==HierarchyEvent.PARENT_CHANGED && e.getComponent().getParent()==null)
					root.shutdownNow();
			}
		});

		addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent event) {
				Platform.runLater(()->{
					root.start();
				});
			}
			public void ancestorRemoved(AncestorEvent event) {
				Platform.runLater(()->{
					root.stop();
				});
			}
			public void ancestorMoved(AncestorEvent event) { }
		});

		logger.trace("before Platform.runLater");
		Platform.runLater(()->{
			try{

				Scene scene = new Scene((Parent) root);
				setScene(scene);

			}catch (Exception e) {
		        logger.catching(e);
			}

		});
	}

	@Override
	public void refresh() {
		logger.error("refresh()");//TODO
	}

	public void setUnitAddress(byte unitAddress) {
		Platform.runLater(()->root.setUnitAddress(unitAddress));
	}

}
