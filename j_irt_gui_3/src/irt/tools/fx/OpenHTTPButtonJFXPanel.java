
package irt.tools.fx;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;

import irt.data.network.NetworkAddress;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.Button;

public class OpenHTTPButtonJFXPanel extends JFXPanel {
	private static final long serialVersionUID = -5186685111758208307L;

	private final NetworkAddress networkAddress;

	private final OpenHTTPButtonFx root;
	
	public OpenHTTPButtonJFXPanel(NetworkAddress networkAddress) {

		this.networkAddress = networkAddress;
		root = new OpenHTTPButtonFx();

		Platform.runLater(()->{
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("fx.css").toExternalForm());
			setScene(scene);
		});
	}

	public class OpenHTTPButtonFx extends Button{

		public OpenHTTPButtonFx() {
			setText("Open HTTP");
			setOnAction(e->{

				final byte[] address = networkAddress.getAddress();
				final String addrStr = Optional.ofNullable(address)
												.filter(a->a.length==4)
												.map(a->IntStream.range(0, a.length))
												.orElse(IntStream.empty())
												.map(index->address[index]&0xFF)
												.mapToObj(Integer::toString)
												.collect(Collectors.joining("."));

				if (!addrStr.isEmpty() && Desktop.isDesktopSupported()) {
					try {

						Desktop.getDesktop().browse(new URI("http://" + addrStr));

					} catch (IOException | URISyntaxException e1) {
						LogManager.getLogger().catching(e1);
					}

				}else
					setDisable(true);
			});
		}
	}
}
