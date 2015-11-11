package irt.gui.controllers.leftside.monitor;

import java.util.concurrent.TimeUnit;

import irt.gui.controllers.FieldsControllerAbstract;
import irt.gui.controllers.ScheduledServices;
import irt.gui.data.DeviceInfo;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketErrors;
import irt.gui.data.packet.observable.InfoPacket;
import irt.gui.errors.PacketParsingException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;

public class InfoController extends FieldsControllerAbstract {

	@FXML private TitledPane infoPanel;
	@FXML private Label snLabel;
	@FXML private Label pnLabel;
	@FXML private Label countLabel;
	@FXML private Label builtDateLabel;
	@FXML private Label versionLabel;
	@FXML private Label typeLabel;
	@FXML private Label subtypeLabel;

	@FXML public void initialize() {
		try {
			packetSender.addPacketToSend(new InfoPacket());
			packetSender.setSend(true);
			ScheduledServices.services.scheduleAtFixedRate(packetSender, 1, 10, TimeUnit.SECONDS);

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	@Override
	protected void updateFields(LinkedPacket packet) throws PacketParsingException {

		InfoPacket p = new InfoPacket(packet.getAnswer());
		logger.trace(p);

		if (p.getPacketHeader().getPacketErrors() == PacketErrors.NO_ERROR) {

			DeviceInfo deviceInfo = new DeviceInfo(p);
			logger.trace(deviceInfo);

			Platform.runLater(new Runnable() {
				
				@Override
				public void run() {
					infoPanel		.setText(deviceInfo.getSerialNumber() + " : " + deviceInfo.getUnitName());

					snLabel			.setText( deviceInfo.getSerialNumber()		.toString());
					pnLabel			.setText( deviceInfo.getUnitPartNumber()	.toString());
					builtDateLabel	.setText( deviceInfo.getFirmwareBuildDate()	.toString());
					versionLabel	.setText( deviceInfo.getFirmwareVersion()	.toString());

					typeLabel		.setText( Integer.toString(deviceInfo.getType()));
					subtypeLabel	.setText( Integer.toString(deviceInfo.getSubtype()));
					countLabel		.setText( Integer.toString(deviceInfo.getUptimeCounter()));
				}
			});

		}else
			logger.warn("\n\tInfoPacket has ERROR:{}", p);

	}
}
