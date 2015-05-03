package irt.data;

import irt.controller.serial_port.value.getter.ValueChangeListenerClass;
import irt.data.event.ValueChangeEvent;
import irt.data.value.Value;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.logging.log4j.LogManager;

public class PllRegisterTextFieldSlider extends ValueChangeListenerClass{

	private static final int MAXIMUM = 127;
	private JTextField textField;
	private JSlider slider;
	private ChangeListener sliderChangeListener;
	private int id;
	private Value value;
	private int multiplier;
	private KeyListener textFieldKeyListener;
	private ActionListener actionListener;
	private FocusListener focusListener;

	public PllRegisterTextFieldSlider(int id, JTextField textField, JSlider slider, int multiplier) {
		super(LogManager.getLogger());
		setListeners();
		this.id = id;
		this.multiplier = multiplier;
		setTextField(textField);
		this.slider = slider;
		slider.setMaximum(MAXIMUM);
		slider.setValue(value.getRelativeValue());
		slider.addChangeListener(sliderChangeListener);
	}

	private void setTextField(JTextField textField) {
		this.textField = textField;
		String str = textField.getText().replaceAll("\\D", "");
		
		value = new Value(str.isEmpty() ? 0 : Integer.parseInt(str)/multiplier, 0, MAXIMUM, 0);

		textField.addKeyListener(textFieldKeyListener);
		textField.addActionListener(actionListener);
		textField.addFocusListener(focusListener);
	}

	private void setListeners() {
		focusListener = new FocusListener() {

			@Override public void focusGained(FocusEvent arg0) { }
			@Override
			public void focusLost(FocusEvent arg0) {
				setText();
			}
		};

		actionListener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (PllRegisterTextFieldSlider.this.textField != null) {
					String text = PllRegisterTextFieldSlider.this.textField.getText().replaceAll("\\D", "");
					value.setValue(text.isEmpty() ? 0 : Integer.parseInt(text) / multiplier);
					slider.setValue(value.getRelativeValue());
					setText();
				}
			}
		};

		sliderChangeListener = new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent arg0) {

				value.setValue(slider.getValue());

				if(slider.isFocusOwner())
					setText();

				if (!slider.getValueIsAdjusting()) {
					fireValueChangeListener(new ValueChangeEvent(value, id));
				}
			}
		};

		textFieldKeyListener = new KeyListener() {
			
			@Override public void keyTyped(KeyEvent arg0) { }
			@Override public void keyPressed(KeyEvent arg0) { }	
			@Override
			public void keyReleased(KeyEvent ke) {
				int kc = ke.getKeyCode();
				if(kc==KeyEvent.VK_UP){
					slider.setValue(slider.getValue()+1);
					setText();
				}else if(kc==KeyEvent.VK_DOWN){
					slider.setValue(slider.getValue()-1);
					setText();
				}
			}
		};
	}

	private void setText() {
		textField.setToolTipText(value.toString());
		textField.setText(""+value.getValue()*multiplier);
	}

	public void clear() {
		textField.removeFocusListener(focusListener);
		focusListener = null;
		textField.removeActionListener(actionListener);
		actionListener = null;
		textField.removeKeyListener(textFieldKeyListener);
		textFieldKeyListener = null;
		textField = null;

		slider.removeChangeListener(sliderChangeListener);
		sliderChangeListener = null;
		slider = null;
	}

}
