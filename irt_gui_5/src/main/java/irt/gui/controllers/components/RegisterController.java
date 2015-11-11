package irt.gui.controllers.components;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.controllers.FieldsControllerAbstract;
import irt.gui.controllers.ScheduledServices;
import irt.gui.controllers.leftside.setup.SerialPortController;
import irt.gui.data.RegisterValue;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketErrors;
import irt.gui.data.packet.observable.device_debug.PotentiometerPacket;
import irt.gui.errors.PacketParsingException;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class RegisterController extends FieldsControllerAbstract {

	@FXML private Label		titleLabel;
	@FXML private Label		valueLabel;
	@FXML private TextField toSetTextField;
	@FXML private Slider	toSetSlider;

	private RegisterValue registerValue;
	private int actualValue;
	private int savedValue;

	public void initialize(RegisterValue registerValue, int minValue, int maxValue, boolean checkForChange) throws PacketParsingException {

		logger.trace("\n\t min:{}\n\t max:{}\n\t{}", minValue, maxValue, registerValue);

		this.registerValue = registerValue;

		toSetSlider.setMin(minValue);
		toSetSlider.setMax(maxValue);

		toSetSlider.valueProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				toSetTextField.setText(Integer.toString(newValue.intValue()));
			}
		});

		toSetTextField.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if(!newValue.matches("\\d*"))
					toSetTextField.setText(oldValue);
			}
		});

		packetSender.addPacketToSend(new PotentiometerPacket(registerValue));
		ScheduledServices.services.scheduleAtFixedRate(packetSender, 1, 3, TimeUnit.SECONDS);
		if(checkForChange)
			ScheduledServices.services.scheduleAtFixedRate(new ValueChangeAnalyzer(), 1, 5, TimeUnit.SECONDS);
	}

	@FXML public void onValueChange(){
		toSetTextField.setText(Long.toString(Math.round(toSetSlider.getValue())));
	}

	@FXML public void setValue(){
		try {
			setValue(toSetTextField.getText());
		} catch (NumberFormatException | PacketParsingException e) {
			logger.catching(e);
		}
	}

	private void setValue(String text) throws NumberFormatException, PacketParsingException {
		if(valueLabel.getText().equals(text))
			logger.debug("\n\t Value did not changed:\n\t{}", text);
		else{
			PotentiometerPacket packet = new PotentiometerPacket(new RegisterValue(registerValue.getIndex(), registerValue.getAddr(), Integer.parseInt(text)));
			packet.addObserver(this);
			SerialPortController.QUEUE.add(packet);
		}
	}

	public void resetValue() throws NumberFormatException, PacketParsingException{
		if(actualValue!=savedValue){
			String text = Integer.toString(savedValue);
			toSetTextField.setText(text);
			setValue(text);
		}
	}

	public void disable(boolean disable){
		toSetTextField	.setDisable(disable);
		toSetSlider		.setDisable(disable);
	}

	@Override
	protected void updateFields(LinkedPacket packet) throws PacketParsingException {
		logger.trace("\n\tENTRY: {}", packet);

		PotentiometerPacket p = new PotentiometerPacket(packet.getAnswer());
		List<Payload> payloads = p.getPayloads();
		PacketErrors packetError = p.getPacketHeader().getPacketErrors();

		if(packetError!=PacketErrors.NO_ERROR){
			final String text = packetError.toString();

			if(!valueLabel.getText().equals(text))
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						valueLabel.setText(text);
					}
				});

			logger.error("\n\tPacket has Error:\n\t sent packet{}\n\n\t received packet{}", packet, p);

		}else{

			if(payloads==null || payloads.isEmpty())
				throw new PacketParsingException("\n\t Packet parsing error:\n\t Payload is empty\n\t Sent packet: " + packet + "\n\t Resieved packet: " + p);

			Payload payload = payloads.get(0);
			actualValue = payload.getInt(2);
			String valueStr = Integer.toString(actualValue);

			if(!valueLabel.getText().equals(valueStr))
				Platform.runLater(new Runnable() {
				
					@Override
					public void run() {
						valueLabel.setText(valueStr);
					}
				});

			if(toSetTextField.getText().isEmpty()){
				toSetTextField.setText(valueStr);
				toSetSlider.setValue(actualValue);
			}
		}
	}

	public void setTitle(String title){
		titleLabel.setText(title);
	}

	public void saveRegister() throws PacketParsingException {
		if(actualValue!=savedValue){
			PotentiometerPacket packet = new PotentiometerPacket(new RegisterValue(registerValue.getIndex(), registerValue.getAddr()+3, 0));
			packet.addObserver(new Observer() {
			
				@Override
				public void update(Observable observable, Object object) {
					final Thread t = new Thread(new Runnable() {
						
						@Override
						public void run() {
							if(observable instanceof LinkedPacket){
								try {

									PotentiometerPacket packet = new PotentiometerPacket(((LinkedPacket)observable).getAnswer());
									logger.warn("\n\tTODO Have to add some code{}{}", observable, packet);

//									Platform.runLater(new Runnable() {
//										
//										@Override
//										public void run() {}
//									});

								} catch (PacketParsingException e) {
									logger.catching(e);
								}
							}
						}
					});
					int priority = t.getPriority();
					if(priority>Thread.MIN_PRIORITY)
						t.setPriority(--priority);
					t.setDaemon(true);
					t.start();
				}
			});
			SerialPortController.QUEUE.add(packet);
		}
	}

	//*********************************************   ValueChangeAnalyzer   ****************************************************************
	private final class ValueChangeAnalyzer implements Runnable {

		protected final Logger logger = LogManager.getLogger();

		private PotentiometerPacket packet;

		public ValueChangeAnalyzer() throws PacketParsingException {
			RegisterValue rv = new RegisterValue(registerValue.getIndex(), registerValue.getAddr()==0 ? 0x10+2 : 0x10+3); //0x10+2 --> RDAC:MEM2; 0x10+3 --> RDAC:MEM3
			packet = new PotentiometerPacket(rv);
			packet.addObserver(new Observer() {
				
				@Override
				public void update(Observable observable, Object object) {
					logger.entry(observable, object);
					final Thread t = new Thread(new Runnable() {
						@Override
						public void run() {

							if(observable instanceof LinkedPacket) {
								LinkedPacket lp = (LinkedPacket) observable;

								if( lp.getAnswer()!=null){

									try {

										PotentiometerPacket p = new PotentiometerPacket(lp.getAnswer());
										List<Payload> payloads = p.getPayloads();
										PacketErrors packetError = p.getPacketHeader().getPacketErrors();

										if(packetError!=PacketErrors.NO_ERROR){

											final String text = "Packet has Error: " + packetError;
											if(!toSetTextField.getText().equals(text))
												Platform.runLater(new Runnable() {
													
													@Override
													public void run() {
														toSetTextField.setTooltip(new Tooltip(text));
													}
												});
											logger.error("\n\tPacket has Error:\n\t sent packet{}\n\n\t received packet{}", packet, p);

										}else{

											if(payloads==null || payloads.isEmpty())
												throw new PacketParsingException("\n\t Packet parsing error:\n\t Payload is empty\n\t Sent packet: " + packet + "\n\t Resieved packet: " + p);

											setTooltip(payloads);
											setColor();
										}
									} catch (Exception e) {
										logger.catching(e);
									}
								}
								else
									logger.warn("No Answer");
							}
						}

						private void setTooltip(List<Payload> payloads) {
							Payload payload = payloads.get(0);
							int sv = payload.getInt(2);
							if(sv!=savedValue){
								savedValue = sv;
								valueLabel.setTooltip(new Tooltip(Integer.toString(savedValue)));
							}
						}

						private void setColor() {
							if(savedValue == actualValue)
								setColor(Color.BLACK);
							else
								setColor(Color.RED);
						}

						private void setColor(Paint color) {
							Paint paint = valueLabel.getTextFill();
							if(!paint.equals(color))
								valueLabel.setTextFill(color);
						}
					});
					int priority = t.getPriority();
					if(priority>Thread.MIN_PRIORITY)
						t.setPriority(--priority);
					t.setDaemon(true);
					t.start();
				}
			});
		}

		@Override
		public void run() {
			SerialPortController.QUEUE.add(packet);
		}
	}
}
