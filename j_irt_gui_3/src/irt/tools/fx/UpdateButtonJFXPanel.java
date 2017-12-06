
package irt.tools.fx;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import irt.data.DeviceInfo;
import irt.data.network.NetworkAddress;
import irt.tools.panel.subpanel.UpdateMessageFx;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.Button;

public class UpdateButtonJFXPanel extends JFXPanel {
	private static final long serialVersionUID = -5186685111758208307L;

	private final DeviceInfo deviceInfo;
	private final NetworkAddress networkAddress;

	private final UpdateButtonFx root;
	
	public UpdateButtonJFXPanel(DeviceInfo deviceInfo, NetworkAddress networkAddress) {

		this.deviceInfo = deviceInfo;
		this.networkAddress = networkAddress;
		root = new UpdateButtonFx();

		Platform.runLater(()->{
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("fx.css").toExternalForm());
			setScene(scene);
		});
	}

	public class UpdateButtonFx extends Button{

		public UpdateButtonFx() {
			setText("Update");
			setOnAction(e->{

				final byte[] address = networkAddress.getAddress();
				final String addrStr = Optional.ofNullable(address)
												.filter(a->a.length==4)
												.map(a->IntStream.range(0, a.length))
												.orElse(IntStream.empty())
												.map(index->address[index]&0xFF)
												.mapToObj(Integer::toString)
												.collect(Collectors.joining("."));

				UpdateMessageFx d = new UpdateMessageFx(deviceInfo);
				d.setIpAddress(addrStr);
				
				d.showAndWait();
			});
		}
	}
}
