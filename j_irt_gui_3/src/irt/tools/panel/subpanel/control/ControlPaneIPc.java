package irt.tools.panel.subpanel.control;

import java.util.Optional;

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.interfaces.Refresh;
import irt.data.DeviceType;
import irt.data.packet.LinkHeader;
import irt.tools.fx.AlarmPanelFx;
import irt.tools.fx.module.ControlPanelIrPcFx;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

public class ControlPaneIPc extends JFXPanel implements Refresh {
	private static final long serialVersionUID = 7804070932251629214L;
	private final static Logger logger = LogManager.getLogger();

	public ControlPaneIPc(Optional<DeviceType> deviceType, LinkHeader linkHeader) {

		ControlPanelIrPcFx controlPanelIrPcFx = new ControlPanelIrPcFx(Optional.ofNullable(linkHeader).map(LinkHeader::getAddr).orElse((byte) 0));

		Platform.runLater(()->{
			try{
				Scene scene = new Scene(controlPanelIrPcFx);
				final String externalForm = AlarmPanelFx.class.getResource("control_panel.css").toExternalForm();
				scene.getStylesheets().add(externalForm);
				setScene(scene);

			}catch (Exception e) {
				logger.catching(e);
			}
		});

		addAncestorListener(new AncestorListener() {
			
			@Override
			public void ancestorRemoved(AncestorEvent event) {
				controlPanelIrPcFx.stop();
			}
			
			@Override public void ancestorMoved(AncestorEvent event) { }
			
			@Override
			public void ancestorAdded(AncestorEvent event) {
				controlPanelIrPcFx.start();
			}
		});
	}

	@Override
	public void refresh() {
	}

	
}
