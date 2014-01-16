package irt.controller;

import irt.controller.control.ControllerAbstract;
import irt.controller.serial_port.value.seter.ConfigurationSetter;
import irt.data.PacketWork;
import irt.data.packet.LinkHeader;
import irt.data.value.Value;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JSlider;
import javax.swing.JTextField;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public abstract class ValueRangeControllerAbstract extends ControllerAbstract {

	private final Logger logger = (Logger) LogManager.getLogger();

	private Value stepValue;

	protected JTextField txtField;
	private JSlider slider;
	private JTextField txtStep;

	private int stepPref;

	private ActionListener actionListener;
	private FocusListener focusListener;

	protected TextSliderController textSliderController;

	public ValueRangeControllerAbstract(String controllerName, PacketWork packetWork, JTextField txtField, JSlider slider, JTextField txtStep, Style style) {
		super(controllerName, packetWork, null, style);
		this.txtField = txtField;
		this.slider = slider;
		this.txtStep = txtStep;
		txtStep.addActionListener(actionListener);
		txtStep.addFocusListener(focusListener);
	}

	@Override
	protected boolean setComponent(Component component) {
		return false;
	}

	@Override
	protected void clear() {
		super.clear();
		logger.trace("clear(), class={}", getClass().getSimpleName());

		if(textSliderController!=null){
			textSliderController.setRun(false);
			textSliderController = null;
		}
		txtField = null;
		slider = null;
		txtStep.removeActionListener(actionListener);
		txtStep.removeFocusListener(focusListener);
		txtStep = null;
		actionListener = null;
		focusListener = null;
		stepValue = null;
	}

	@Override
	protected void setListeners() {
		setStepListener();
		setFocusListener();
	}

	private void setFocusListener() {
		focusListener = new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				setStep();
			}
			@Override public void focusGained(FocusEvent e) {}
		};
	}

	private void setStepListener() {
		actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setStep();
				txtField.requestFocusInWindow();
			}

		};
	}

	private void setStep() {
		logger.trace("setStep(), stepValue={}", stepValue);

		if(stepValue!=null){
			stepValue.setValue(ValueRangeControllerAbstract.this.txtStep.getText());

			String string = stepValue.toString();
			ValueRangeControllerAbstract.this.txtStep.setText(string);
			int v = (int) stepValue.getValue();

			JSlider s = ValueRangeControllerAbstract.this.slider;

			if (stepValue != null && s != null && stepPref != v) {
				s.setMinorTickSpacing(v);
				GuiControllerAbstract.getPrefs().putInt(ValueRangeControllerAbstract.this.getClass().getSimpleName() + " Step", v);
			}
		}
	}

	protected void startTextSliderController(String controllerName, Value value, short packetId, byte parameterId, Style style) {

		stepPref = GuiControllerAbstract.getPrefs().getInt(getClass().getSimpleName()+" Step", 1);

		slider.setMaximum((int) value.getRelativeMaxValue());
		slider.setMinorTickSpacing(stepPref);

		if(stepValue!=null){
			stepValue = value.getCopy();
			stepValue.setMinMax(1, slider.getMaximum());
			stepValue.setValue(stepPref);
			txtStep.setText(stepValue.toString());
		}

		LinkHeader linkHeader = getPacketWork().getPacketThread().getLinkHeader();
		textSliderController = new TextSliderController(controllerName, new ConfigurationSetter(linkHeader, parameterId, packetId), value, txtField, slider, style);
		Thread t = new Thread(textSliderController, getClass().getSimpleName());
		int priority = t.getPriority();
		if(priority>Thread.MIN_PRIORITY)
			t.setPriority(priority-1);
		t.setDaemon(true);
		t.start();

		setSend(false);
		setWaitTime(1000000);
	}

	public TextSliderController getTextSliderController() {
		return textSliderController;
	}

	public Value getStepValue() {
		return stepValue;
	}

	public void setStepValue(Value stepValue) {
		this.stepValue = stepValue;
		txtStep.setText(stepValue.toString());
	}
}
