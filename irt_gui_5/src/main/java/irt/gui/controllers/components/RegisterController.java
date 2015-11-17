package irt.gui.controllers.components;

import java.time.Duration;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import irt.gui.controllers.FieldsControllerAbstract;
import irt.gui.data.RegisterValue;
import irt.gui.data.listeners.NumericChecker;
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

	private RegisterValue registerValue;
	private int actualValue;
	private int savedValue;

	@Override
	protected Duration getPeriod() {
		return Duration.ofSeconds(3);
	}

	public void initialize(RegisterValue registerValue, int minValue, int maxValue, boolean checkForChange) throws PacketParsingException {

		logger.trace("\n\t min:{}\n\t max:{}\n\t{}", minValue, maxValue, registerValue);

		this.registerValue = registerValue;

		toSetSlider.setMin(minValue);
		toSetSlider.setMax(maxValue);

		toSetSlider.valueProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				Platform.runLater(new Runnable() {
					
					@Override
					public void run() {
						toSetTextField.setText(Integer.toString(newValue.intValue()));
					}
				});
			}
		});

		toSetTextField.textProperty().addListener(new NumericChecker());

		addLinkedPacket(new PotentiometerPacket(registerValue));
		if(checkForChange){
			RegisterValue rv = new RegisterValue(registerValue.getIndex(), registerValue.getAddr()==0 ? 0x10+2 : 0x10+3); //0x10+2 --> RDAC:MEM2; 0x10+3 --> RDAC:MEM3
			addLinkedPacket(new PotentiometerPacket(rv));
		}
	}

	public void resetValue() throws NumberFormatException, PacketParsingException{
		if(actualValue!=savedValue){
			String text = Integer.toString(savedValue);
			setValue(text);
			Platform.runLater(new Runnable() {
				
				@Override
				public void run() {
					toSetTextField.setText(text);
				}
			});
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

	public void disable(boolean disable){
		toSetTextField	.setDisable(disable);
		toSetSlider		.setDisable(disable);
	}

	@Override
	protected void updateFields(LinkedPacket packet) throws PacketParsingException {
		logger.trace("\n\tENTRY: {}", packet);

		PotentiometerPacket p = new PotentiometerPacket(packet.getAnswer());
		PacketErrors packetError = p.getPacketHeader().getPacketErrors();

		if(packetError==PacketErrors.NO_ERROR){

			final List<Payload> payloads= p.getPayloads();
			for(Payload pl:payloads)
				switch(pl.getParameterHeader().getParameterHeaderCode()){
				case DD_READ_WRITE:
					if(pl.getInt(1)==registerValue.getAddr()){
						setValue(pl);
					}else
						checkValue(pl);
					break;
				default:
				}
		}else{
			logger.warn("\n\tPacket has Error:\n\t sent packet{}\n\n\t received packet{}", packet, p);

			final String text = packetError.toString();

			if(!valueLabel.getText().equals(text))
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						valueLabel.setText(text);
					}
				});
		}
	}

	private void checkValue(Payload pl) {
		setTooltip(pl);
		setColor();
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
			Platform.runLater(new Runnable() {
				
				@Override
				public void run() {
					valueLabel.setTextFill(color);
				}
			});
	}

	private void setTooltip(Payload pl) {
		int sv = pl.getInt(2);
		if(sv!=savedValue){
			savedValue = sv;
			Platform.runLater(new Runnable() {
				
				@Override
				public void run() {
					valueLabel.setTooltip(new Tooltip(Integer.toString(savedValue)));
				}
			});
		}
	}

	private void setValue(Payload pl) {
		actualValue = pl.getInt(2);
		String valueStr = Integer.toString(actualValue);

		if(!valueLabel.getText().equals(valueStr))
			Platform.runLater(new Runnable() {
			
				@Override
				public void run() {
					valueLabel.setText(valueStr);
				}
			});

		if(toSetTextField.getText().isEmpty()){
			Platform.runLater(new Runnable() {
				
				@Override
				public void run() {
					toSetTextField.setText(valueStr);
					toSetSlider.setValue(actualValue);
				}
			});
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
}
