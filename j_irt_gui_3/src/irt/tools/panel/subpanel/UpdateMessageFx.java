package irt.tools.panel.subpanel;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.file.FileScanner;
import irt.data.DeviceInfo;
import irt.data.MyThreadFactory;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;

public class UpdateMessageFx extends Dialog<Boolean>{
	private final Logger logger = LogManager.getLogger();

	final private TextField ipAddress;

	private String ipAddressStr;

	private Label lbProfile;

	private CheckBox cbProfile;

	public UpdateMessageFx(DeviceInfo deviceInfo) {

		new MyThreadFactory().newThread(()->{
			
			deviceInfo.getSerialNumber().map(sn->sn + ".bin").ifPresent(f->{
				
				try {

					final FileScanner fileScanner = new FileScanner( Paths.get("Z:/4alex/boards/profile/"), f);
					final List<Path> paths = fileScanner.get(10, TimeUnit.SECONDS);

					if(paths.size()!=1)
						return;

					final Path path = paths.get(0);
					Platform.runLater(()->{

						lbProfile.setTooltip(new Tooltip(path.toString()));
						lbProfile.setText(path.getFileName().toString());
						cbProfile.setSelected(true);
					});

				} catch (Exception e) {
					logger.catching(e);
				}
			});
		})
		.start();

		setTitle("IP Address");
		setHeaderText("Type a valid IP address.");

		ButtonType updateButtonType = new ButtonType("Update", ButtonData.OK_DONE);
		getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

		final Node lookupButton = getDialogPane().lookupButton(updateButtonType);
		lookupButton.setDisable(true);

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		//IP Address row #0
		grid.add(new Label("IP Address:"), 0, 0);
		ipAddress = new TextField();
		grid.add(ipAddress, 1, 0);

		ipAddress.setPromptText("192.168.0.1");
		ipAddress.textProperty().addListener(getListener(lookupButton));

		//Profile row #1
		cbProfile = new CheckBox("Profile:");
		grid.add(cbProfile, 0, 1);
		lbProfile = new Label();
		grid.add(lbProfile, 1, 1);

		//Program row #2
		CheckBox cbProgram = new CheckBox("Program:");
		grid.add(cbProgram, 0, 2);
		Label lProgram = new Label();
		grid.add(lProgram, 1, 2);

		getDialogPane().setContent(grid);
	}

	private ChangeListener<? super String> getListener(final Node lookupButton) {
		return (o, oV, nV)->{

			final List<String> addr = Optional.ofNullable(nV).filter(a->!a.isEmpty()).map(a->a.split("\\D")).map(Arrays::stream).orElse(Stream.empty()).filter(s->!s.isEmpty()).collect(Collectors.toList());

			if(addr.size()!=4){
				lookupButton.setDisable(true);
				return;
			}

			ipAddressStr = addr.stream().collect(Collectors.joining("."));
			lookupButton.setDisable(false);
		};
	}

	public void setIpAddress(String addrStr) {
		ipAddress.setText(addrStr);
	}

	public String getIpAddress() {
		return ipAddressStr;
	}
}
