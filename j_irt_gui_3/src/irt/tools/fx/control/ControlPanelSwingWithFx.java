
package irt.tools.fx.control;

import java.awt.event.HierarchyEvent;
import java.util.Optional;

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.packet.LinkHeader;
import irt.tools.fx.AlarmPanelFx;
import irt.tools.panel.ConverterPanel;
import irt.tools.panel.PicobucPanel;
import irt.tools.panel.subpanel.monitor.Monitor;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

public class ControlPanelSwingWithFx extends JFXPanel implements Monitor {
	private final static long serialVersionUID = 1157429339979438261L;
	private final static Logger logger = LogManager.getLogger();

	private ControlPaneFx root;

	public ControlPanelSwingWithFx(LinkHeader linkHeader) {

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

		setSize(215, 180);
		Platform.runLater(()->{

			try {

				root = new ControlPaneFx(linkHeader.getAddr());

				Scene scene = new Scene(root);
				setScene(scene);
				final String externalForm = AlarmPanelFx.class.getResource("control_panel.css").toExternalForm();
				scene.getStylesheets().add(externalForm);

			}catch(Exception e) {
				logger.catching(e);
			}
		});
	}

	@Override
	public void refresh() {
//		LogManager.getLogger().error("refresh()");
	}

	public void setUnitAddress(byte unitAddress) {
		Platform.runLater(()->root.setUnitAddress(unitAddress));
	}
}
