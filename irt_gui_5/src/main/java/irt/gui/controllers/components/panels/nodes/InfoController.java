package irt.gui.controllers.components;

import java.time.Duration;

import irt.gui.controllers.FieldsControllerAbstract;
import irt.gui.controllers.UpdateController;
import irt.gui.data.DeviceInfo;
import irt.gui.data.packet.enums.PacketErrors;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.observable.InfoPacket;
import irt.gui.errors.PacketParsingException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;

public class InfoController extends FieldsControllerAbstract {

	private static Integer DEVICE_TYPE;

	@FXML private TitledPane infoPanel;
	@FXML private Label snLabel;
	@FXML private Label pnLabel;
	@FXML private Label countLabel;
	@FXML private Label builtDateLabel;
	@FXML private Label versionLabel;
	@FXML private Label typeLabel;
	@FXML private Label addressLabel;

//	private RollingRandomAccessFileManager rollingRandomAccessFileManager;

	@FXML public void initialize() {
		try {

			addPacketToSend(new InfoPacket());
			doUpdate(true);

			UpdateController.addController(this);

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	@Override
	protected Duration getPeriod() {
		return Duration.ofSeconds(5);
	}

	@Override
	protected void updateFields(LinkedPacket packet) throws PacketParsingException {

		InfoPacket p = new InfoPacket(packet.getAnswer(), true);
		logger.trace(p);

		if (p.getPacketHeader().getPacketError() == PacketErrors.NO_ERROR) {

			DeviceInfo deviceInfo = new DeviceInfo(p);
			logger.trace(deviceInfo);

			Platform.runLater(()->{
				try{
					final String sn = deviceInfo.getSerialNumber().toString();

					if(!sn.equals(snLabel.getText()))
						changeAppenderSettings(sn);

					snLabel.setText(sn);

					infoPanel		.setText(deviceInfo.getSerialNumber() + " : " + deviceInfo.getUnitName());
					pnLabel			.setText( deviceInfo.getUnitPartNumber()	.toString());
					builtDateLabel	.setText( deviceInfo.getFirmwareBuildDate()	.toString());
					versionLabel	.setText( deviceInfo.getFirmwareVersion()	.toString());

					DEVICE_TYPE = deviceInfo.getType();
					typeLabel		.setText(DEVICE_TYPE+"."+deviceInfo.getRevision()+"."+deviceInfo.getSubtype());
					countLabel		.setText( Integer.toString(deviceInfo.getUptimeCounter()));
					addressLabel	.setText((packet.getLinkHeader().getAddr() & 0xFF) + "");
				}catch(Exception ex){
					logger.catching(ex);
				}
			});

		}else
			logger.warn("\n\tInfoPacket has ERROR:{}", p);

	}

	private void changeAppenderSettings(String serialNumber) {
		//TODO
//		try{
//
//			final Path fileName = Paths.get(System.getProperty("user.home"), "irt", serialNumber, serialNumber + ".log");
//			final Path filePattern = Paths.get(System.getProperty("user.home"), "irt", serialNumber, serialNumber + "-%d{MM-dd-yyyy}-%i.log.gz");
//
//		
//			final Logger dumper = (Logger) LogManager.getLogger("dumper");
//			final Map<String, Appender> appenders = dumper.getAppenders();
//			final RollingRandomAccessFileAppender appender = (RollingRandomAccessFileAppender) appenders.get("DumpFile");
//
//			final RollingFileManager manager = appender.getManager();
//
//			if(rollingRandomAccessFileManager==null)
//				Optional
//				.ofNullable(manager.getFileName())
//				.map(fn->(RollingRandomAccessFileManager)AbstractManager.getManager(fn, null, null))
//				.ifPresent(m->rollingRandomAccessFileManager = m);
//
//			final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
//			final Configuration config = ctx.getConfiguration();
//			Layout<?> layout = PatternLayout.createLayout("%n%-5p %d%n %M (%F:%L)%n%m%n", null, config, null, Charset.forName("UTF-8"), true, false, null, null);
//
//			final RollingRandomAccessFileAppender newAppender = RollingRandomAccessFileAppender
//																		.createAppender(fileName.toString(),
//																				filePattern.toString(),
//																				"true",
//																				appender.getName(),
//																				"false",
//																				Integer.toString(manager.getBufferSize()),
//																				rollingRandomAccessFileManager.getTriggeringPolicy(),
//																				rollingRandomAccessFileManager.getRolloverStrategy(),
//																				layout,
//																				appender.getFilter(),
//																				null,
//																				"false",
//																				null,
//																				config);
//			appender.stop();
//			dumper.removeAppender(appender);
//			dumper.addAppender(newAppender);
//			newAppender.start();
//
//		}catch(Exception ex){
//			logger.catching(ex);
//		}
 	}

	public static Integer getDeviceType() {
		return DEVICE_TYPE;
	}

	@Override
	public void doUpdate(boolean update) {
		super.doUpdate(update);
		snLabel.getParent().setDisable(!update);
	}
}
