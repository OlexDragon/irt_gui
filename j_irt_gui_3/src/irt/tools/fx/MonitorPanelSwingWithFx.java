
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
import javafx.scene.Scene;

public class MonitorPanelSwingWithFx extends JFXPanel implements Monitor {
	private static final long serialVersionUID = 1157429339979438261L;
	private final Logger logger = LogManager.getLogger();

	private MonitorPanelFx root;

	public MonitorPanelSwingWithFx() {
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

		Platform.runLater(()->{
			try{

				root = new MonitorPanelFx();
				Scene scene = new Scene(root);
				setScene(scene);

			}catch (Exception e) {
		        logger.catching(e);
			}
		});
	}

	@Override
	public void refresh() {
		logger.traceEntry();
		//TODO
	}

	public void setUnitAddress(byte unitAddress) {
		logger.entry(unitAddress);
		Platform.runLater(()->root.setUnitAddress(unitAddress));
	}

}
