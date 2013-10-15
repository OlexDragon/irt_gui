package irt.controller;

import irt.controller.control.ControllerAbstract;
import irt.data.DacValue;
import irt.data.PacketWork;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.value.Value;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class DacController extends ControllerAbstract {

	private int dacNumber;
	private JSlider slider;
	private JTextField txtDacValue;
	private FocusListener textFieldFocusListener;
	private FocusListener sliderFocusListener;
	private ChangeListener sliderChangeListener;

	private Value value;

	public DacController(PacketWork packetWork, int dacNumber, JSlider slider, JTextField txtDacValue) {
		super(packetWork, null, null);
		value = setValue();
		setListeners();
		this.dacNumber = dacNumber;
		this.slider = slider;
		setTextField(txtDacValue);
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return new ValueChangeListener() {
			
			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				DacValue dv = (DacValue)valueChangeEvent.getSource();
				if(dv.getDacNumber()==dacNumber)
					setAllValues(dv.getDacValue());
				
			}
		};
	}

	@Override
	protected void setListeners() {
		textFieldFocusListener = new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent fe) {
				slider.removeFocusListener(sliderFocusListener);
			}
			
			@Override
			public void focusGained(FocusEvent fe) {
				slider.addFocusListener(sliderFocusListener);
			}
		};
		sliderFocusListener = new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				slider.removeChangeListener(sliderChangeListener);
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				slider.addChangeListener(sliderChangeListener);
			}
		};

		sliderChangeListener = new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				if (slider.isFocusOwner())
					if (!slider.getValueIsAdjusting()) {
						setValue(slider.getValue());
						txtDacValue.requestFocusInWindow();
					} else {
						if(value!=null){
							Value v = value.getCopy();
							v.setRelativeValue(slider.getValue());
							txtDacValue.setText(v.toString());
						}
					}
			}
		};
	}

	private void setAllValues(short dacValue) {
		value.setValue(dacValue);
		txtDacValue.setText(value.toString());
		slider.setValue(value.getRelativeValue());
	}

	protected Value setValue() {
		return new Value(0, 0, 4095, 0);
	}

	protected void setValue(int value) {

		this.value.setRelativeValue(value);
//		((DacSetter)getPacketWork()).preparePacketToSend((short)this.value.getValue());
		setSend(true);
	}

	private void setTextField(JTextField txtDacValue) {
		this.txtDacValue = txtDacValue;
		this.txtDacValue.addFocusListener(textFieldFocusListener);
	}

	@Override
	protected boolean setComponent(Component component) {
		return false;
	}

	@Override
	protected void clear() {
		super.clear();
		txtDacValue.removeFocusListener(textFieldFocusListener);
		txtDacValue = null;
		slider.removeFocusListener(sliderFocusListener);
		slider.removeChangeListener(sliderChangeListener);
		slider = null;

		value = null;
	}
}
