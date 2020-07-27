package irt.gui.controllers.components;

import java.net.URL;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import irt.gui.IrtGuiProperties;
import irt.gui.controllers.interfaces.FxmlNode;
import irt.gui.controllers.interfaces.SliderListener;
import irt.gui.data.MyThreadFactory;
import irt.gui.data.RegisterValue;
import irt.gui.data.listeners.NumericChecker;
import irt.gui.data.listeners.TextChangeListener;
import irt.gui.data.listeners.TextFieldFocusListener;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.observable.device_debug.DACPacket;
import irt.gui.data.packet.observable.device_debug.DACPacket.DACs;
import irt.gui.data.value.Value;
import irt.gui.errors.PacketParsingException;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;

public class TextFieldDAC extends ScheduledNodeAbstract implements SliderListener, FxmlNode{

	public static final long MAX_VALUE = IrtGuiProperties.getLong("gui.converter.DAC.value.max");
	public static final String NAME_PROPERTIES = "gui.menu.converter.items.DAC.%d.name";	
	public static final String FXML_PROPERTIES = "gui.menu.converter.items.DAC.%d.fxml";	

	private static final ExecutorService SERVICES = Executors.newSingleThreadExecutor(new MyThreadFactory());

	private DACs dac;
	private Value value = new Value(0L, 0, MAX_VALUE, 0);		public Value getValue() { return value; }
	private DACPacket commandPacket;

	public TextFieldDAC(DACPacket.DACs dac) {
		this.dac = dac;
		setPeriod(dac.getPeriod());
	}

	@FXML private TextField textField;
    @FXML  private URL location; public URL getLocation() { return location; }


	@FXML protected void initialize() {

		setPropertyName(String.format(FXML_PROPERTIES, dac.ordinal()+1));

		textField.setTooltip(new Tooltip(IrtGuiProperties.getProperty(String.format(NAME_PROPERTIES, dac.ordinal()+1))));
		textField.setUserData(this);
		final StringProperty textProperty = textField.textProperty();
		new TextChangeListener(this, textField, value);

		final NumericChecker numericChecker = new NumericChecker(textProperty);
		numericChecker.setMaximum(MAX_VALUE);

		new TextFieldFocusListener(textField);

		try {

			addPacket(new DACPacket(dac, null));
			commandPacket = new DACPacket(dac, 0);
			commandPacket.addObserver(this);

		} catch (PacketParsingException e) {
			logger.catching(e);
		}
	}


	@FXML public void onActionTextField() {
		value.setValue(textField.getText());
		commandPacket.setValue(value.getValue().intValue());
		SerialPortController.getQueue().add(commandPacket, true);
	}

	@FXML private void onActionRemove(){
		final ObservableList<Node> nodes = ((Pane)textField.getParent()).getChildren();
		nodes.remove(textField);
	}

	@Override public void update(Observable o, Object arg) {
		final byte[] answer = ((LinkedPacket)o).getAnswer();
		if(answer!=null)
			SERVICES.execute(()->{
				try {

					final DACPacket packet = new DACPacket(answer, true);
					final List<Payload> payloads = packet.getPayloads();
					if(payloads.size()==0) {
						logger.warn("Packet does not have payload. " + packet);
						return;
					}
					final RegisterValue rv = payloads.get(0).getRegisterValue();
					setValue(value, rv.getValue());

					final String text = value.toString();
					if(text!=null && !text.equals(textField.getText()))
						Platform.runLater(()->textField.setText(text));

				} catch (Exception e) {
					logger.catching(e);
				}
			});
	}

	public void setValue(Value value, Integer rv) {
		Optional
		.ofNullable(value.getValue())
		.map(Long::intValue)
		.filter(i->i==rv)
		.orElseGet(()->{
			value.setValue(rv);
			return null;
		});
	}

	@Override public void setKeyStartWith(String name) throws PacketParsingException, ClassNotFoundException, InstantiationException, IllegalAccessException { throw new UnsupportedOperationException("Auto-generated method stub"); }

	@Override public void addFocusListener(ChangeListener<Boolean> focusListener) {
		textField.focusedProperty().addListener(focusListener);
	}

	@Override public void setSliderValue(Slider slider, ChangeListener<Number> sliderChangeListener, String cssClass, Observer valueObserver, NumericChecker stepNumericChecker) {

		final ObservableList<String> styleClass = textField.getStyleClass();

		if(!styleClass.contains(cssClass))
			styleClass.add(cssClass);

		Platform.runLater(()->{
			slider.setMinorTickCount(0);
			slider.setMajorTickUnit(MAX_VALUE/100);
		});

		//set slider values
		final DoubleProperty valueProperty = slider.valueProperty();
		{
			//set slider min value
			final Optional<Value> ofNullable = Optional.ofNullable(value);

			ofNullable
			.map(Value::getMinValue)
			.filter(min->Double.compare(slider.getMin(), min)!=0)
			.ifPresent(min->Platform.runLater(()->slider.setMin(min)));

			//set slider max value
			ofNullable
			.map(Value::getMaxValue)
			.filter(max->Double.compare(slider.getMax(), max)!=0)
			.ifPresent(max->{
				//set limit for text field
				stepNumericChecker.setMaximum(max);

				Platform.runLater(()->slider.setMax(max));
			});

			//set slider value
			ofNullable
			.map(Value::getRelativeValue)
			.filter(v->Double.compare(slider.getValue(), v)!=0)
			.ifPresent(
					v->
					Platform.runLater(
							()->{
								valueProperty.removeListener(sliderChangeListener);
								slider.setValue(v);
								valueProperty.addListener(sliderChangeListener);
							}));
		}

		value.addObserver(valueObserver);
	}

	@Override public int getMultiplier() {
		return 1;
	}

	@Override
	public void setText(double value) {
		textField.setText(Integer.toString(((int)value)));
	}
}
