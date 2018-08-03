
package irt.tools.fx;

import java.awt.event.HierarchyEvent;
import java.util.Optional;

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import irt.tools.panel.ConverterPanel;
import irt.tools.panel.PicobucPanel;
import irt.tools.panel.subpanel.monitor.Monitor;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

public class MonitorPanelSwingWithFx extends JFXPanel implements Monitor {
	private final static long serialVersionUID = 1157429339979438261L;

	private MonitorPanelFx root;

	public MonitorPanelSwingWithFx() {

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

		addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent event) {
				Platform.runLater(()->root.start());
			}
			public void ancestorRemoved(AncestorEvent event) {
				Platform.runLater(()->root.stop());
			}
			public void ancestorMoved(AncestorEvent event) { }
		});

		Platform.runLater(()->{

				root = new MonitorPanelFx();
				Scene scene = new Scene(root);
				setScene(scene);
		});
	}

	@Override
	public void refresh() {
		//TODO
	}

	public void setUnitAddress(byte unitAddress) {
		Platform.runLater(()->root.setUnitAddress(unitAddress));
	}

}
