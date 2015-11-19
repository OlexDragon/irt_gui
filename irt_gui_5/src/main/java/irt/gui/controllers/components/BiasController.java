
package irt.gui.controllers.components;

import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import irt.gui.IrtCuiProperties;
import irt.gui.controllers.FieldsControllerAbstract;
import irt.gui.controllers.ScheduledServices;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.observable.device_debug.CallibrationModePacket;
import irt.gui.data.packet.observable.device_debug.CallibrationModePacket.CalibrationMode;
import irt.gui.errors.PacketParsingException;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class BiasController extends FieldsControllerAbstract {

	@FXML private VBox biasVBox;
	@FXML private Button calibModeButton;
    @FXML private Button saveButton;
    @FXML private Button resetButton;
    @FXML private RegisterController value1Controller;
    @FXML private RegisterController value2Controller;
    @FXML private RegisterController value3Controller;
    @FXML private RegisterController value4Controller;
    @FXML private RegisterController value5Controller;
    @FXML private RegisterController value6Controller;
    @FXML private RegisterController value7Controller;
	private RegisterController[] controllers;

    private CalibrationMode callibrationMode;
	private ScheduledFuture<?> scheduleAtFixedRate;

	@Override
	protected Duration getPeriod() {
		return Duration.ofSeconds(3);
	}

	public void initialize( String name) throws PacketParsingException {

		controllers = new RegisterController[]{value1Controller,value2Controller,value3Controller,value4Controller,value5Controller,value6Controller,value7Controller};

		setName(name);
		for(int i=0; i<controllers.length; i++)
			controllers[i].initialize( name+i);

		addLinkedPacket(new CallibrationModePacket((CalibrationMode)null));
		 scheduleAtFixedRate = ScheduledServices.services.scheduleAtFixedRate(new CSSWorker(), 500, 100, TimeUnit.MILLISECONDS);
	}

	@FXML public void changeCallibrationMode(){

		if(callibrationMode!=null){
			CallibrationModePacket packet;
			try {

				packet = new CallibrationModePacket(callibrationMode==CalibrationMode.ON ? CalibrationMode.OFF : CalibrationMode.ON);
				packet.addObserver(this);
				SerialPortController.QUEUE.add(packet);

			} catch (Exception e) {
				logger.catching(e);
			}
		}
	}

	@FXML public void saveValues(Event e){
		if(saveButton!=null){
			for(RegisterController c:controllers)
				try {
					c.saveRegister();
				} catch (Exception e1) {
					logger.catching(e1);
				}
		}
	}

	@FXML public void resetValues(Event e){
		if(saveButton!=null){
			for(RegisterController c:controllers)
				try {
					c.resetValue();
				} catch (Exception e1) {
					logger.catching(e1);
				}
		}
	}

	@Override
	protected void updateFields(LinkedPacket packet) throws PacketParsingException {
		logger.entry(packet);

		CallibrationModePacket p = new CallibrationModePacket(packet.getAnswer());
		callibrationMode = p.getCallibrationMode();
		final String text = "Callibration Mode is " + callibrationMode;

		if(!calibModeButton.getText().equals(text))
			Platform.runLater(()->calibModeButton.setText(text));

		disable(callibrationMode==CalibrationMode.OFF);
	}

	@Override
	public void doUpdate(boolean receive) {
		logger.entry(receive);

		super.doUpdate(receive);
		if(controllers!=null)
			for(RegisterController vc:controllers)
				vc.doUpdate(receive);
	}

	public void disable(boolean disable){
		saveButton	.setDisable(disable);
		resetButton	.setDisable(disable);
		if(controllers!=null)
			for(RegisterController vc:controllers)
				vc.disable(disable);
	}

    public class CSSWorker implements Runnable {

		private static final String RESOURCE = "Resource:";

		@Override
		public void run() {

			try {

				final Integer deviceID = InfoController.getDeviceType();

				if (deviceID != null) {
					scheduleAtFixedRate.cancel(true);

					final String cssFilePath = String.format(IrtCuiProperties.PANEL_PROPERTIES, BiasController.this.getName(), "css." + deviceID);
					final Optional<String> optionalCssPath = Optional.ofNullable(IrtCuiProperties.getProperty(cssFilePath));

					setCss(optionalCssPath);
				}
			} catch (Exception e) {
				logger.catching(e);
			}
		}

		private void setCss(Optional<String> optionalCssPath) {

			final Optional<String[]> pathArray = optionalCssPath

					.filter(
							s -> !s.isEmpty())

					.map(
							s -> s.contains(",")
							? s.split(",")
									: new String[] { s });

			// if path does not exists do nothing
			if (pathArray.isPresent())

				Arrays.stream(pathArray.get())

				.map(
						p -> p.startsWith(RESOURCE)
						? getClass().getResource(p.substring(RESOURCE.length()))
								: newUrl(p))

				.forEach(
						u -> Platform.runLater(() -> biasVBox.getStylesheets().add(u.toString())));
		}

		private URL newUrl(String p) {
			URL url = null;

			try {
				url = new URL(p);
			} catch (Exception e) {
				logger.catching(e);
			}

			return url;
		}
    }
}
