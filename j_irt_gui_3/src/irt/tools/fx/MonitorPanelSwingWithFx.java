
package irt.tools.fx;

import java.awt.event.HierarchyEvent;
import java.util.Optional;

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.tools.panel.ConverterPanel;
import irt.tools.panel.PicobucPanel;
import irt.tools.panel.subpanel.monitor.Monitor;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

public class MonitorPanelSwingWithFx extends JFXPanel implements Monitor {
	private static final long serialVersionUID = 1157429339979438261L;
	private final Logger logger = LogManager.getLogger();

	private MonitorPanelFx root;

	public MonitorPanelSwingWithFx() {
		logger.traceEntry();

		addHierarchyListener(
				hierarchyEvent->
				Optional
				.of(hierarchyEvent)
				.filter(e->(e.getChangeFlags()&HierarchyEvent.PARENT_CHANGED)!=0)
				.map(HierarchyEvent::getChanged)
				.filter(c->c instanceof ConverterPanel || c instanceof PicobucPanel)
				.filter(c->c.getParent()==null)
				.ifPresent(
						c->{
							Platform.runLater(()->setVisible(false));
							root.shutdownNow();
						}));

//		logger.error("--- 1 ---");
		addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent event) {
				Platform.runLater(()->{
//					logger.error("Start {}", root.getClass().getSimpleName());
					root.start();
				});
			}
			public void ancestorRemoved(AncestorEvent event) {
				Platform.runLater(()->{
//					logger.error("stop {}", root.getClass().getSimpleName());
					root.stop();
				});
			}
			public void ancestorMoved(AncestorEvent event) { }
		});

//		logger.error("--- 2 --- keep javafx alive: {}");
		Platform.runLater(()->{
			try{

//				logger.error("*** Yee ***");
				root = new MonitorPanelFx();
				Scene scene = new Scene(root);

//				final String externalForm = getClass().getResource("monitor_panel.css").toExternalForm();
//				scene.getStylesheets().add(externalForm);

				setScene(scene);

			}catch (Exception e) {
		        logger.catching(e);
			}
		});
//		logger.error("--- 3 ---");
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
