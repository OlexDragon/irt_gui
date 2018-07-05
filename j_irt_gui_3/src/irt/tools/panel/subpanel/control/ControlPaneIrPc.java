package irt.tools.panel.subpanel.control;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.interfaces.Refresh;
import irt.data.DeviceInfo.DeviceType;
import irt.data.packet.LinkHeader;
import irt.tools.fx.ControlPanelIrPcFx;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

public class ControlPaneIrPc extends JFXPanel implements Refresh {
	private static final long serialVersionUID = 7804070932251629214L;
	private final static Logger logger = LogManager.getLogger();

	public ControlPaneIrPc(Optional<DeviceType> deviceType, LinkHeader linkHeader) {

		Platform.runLater(()->{
			try{

				final ControlPanelIrPcFx controlPanelIrPcFx = new ControlPanelIrPcFx(Optional.ofNullable(linkHeader).map(LinkHeader::getAddr).orElse((byte) 0));
				Scene scene = new Scene(controlPanelIrPcFx);
				final String externalForm = ControlPanelIrPcFx.class.getResource("control_panel.css").toExternalForm();
				scene.getStylesheets().add(externalForm);
				setScene(scene);

			}catch (Exception e) {
				logger.catching(e);
			}
		});
	}

	@Override
	public void refresh() {
	}

	
}
