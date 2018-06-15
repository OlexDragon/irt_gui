package irt.tools.panel.subpanel.control;

import java.util.Optional;

import irt.controller.interfaces.Refresh;
import irt.data.DeviceInfo.DeviceType;
import irt.data.packet.LinkHeader;
import irt.tools.fx.ControlPanelIrPcFx;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

public class ControlPaneIrPc extends JFXPanel implements Refresh {
	private static final long serialVersionUID = 7804070932251629214L;

	public ControlPaneIrPc(Optional<DeviceType> deviceType, LinkHeader linkHeader) {

		setOpaque(false);
		Platform.runLater(()->{
			final ControlPanelIrPcFx controlPanelIrPcFx = new ControlPanelIrPcFx(Optional.ofNullable(linkHeader).map(LinkHeader::getAddr).orElse((byte) 0));
			Scene scene = new Scene(controlPanelIrPcFx);
			scene.getStylesheets().add(getClass().getResource("../../../fx/control_panel.css").toExternalForm());
			setScene(scene);
		});
	}

	@Override
	public void refresh() {
	}

	
}
