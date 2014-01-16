package irt.controller;

import irt.controller.control.ControllerAbstract;
import irt.controller.serial_port.value.seter.ConfigurationSetter;
import irt.data.IdValue;
import irt.data.PacketWork;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.PacketHeader;
import irt.data.value.Value;

import java.awt.Component;
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

public class TextSliderController extends ControllerAbstract {

	private Value value;
	private JTextField txtField;
	private JSlider slider;
	private FocusListener sliderFocusListener;
	private ChangeListener sliderChangeListener;
	private ActionListener txtActionListener;
	private KeyListener txtKeyListener;

	public TextSliderController(String controllerName, PacketWork packetWork, Value value, JTextField txtField, JSlider slider, Style style) {
		super(controllerName, packetWork, null, style);
		this.value = value;
		slider.setMinimum(value.getRelativeMinValue());
		slider.setMaximum((int) value.getRelativeMaxValue());
		this.txtField = txtField;
		txtField.addActionListener(txtActionListener);
		txtField.addKeyListener(txtKeyListener);
		txtField.requestFocusInWindow();
		this.slider = slider;
		slider.addFocusListener(sliderFocusListener);
		if(slider.isFocusOwner())
			slider.addChangeListener(sliderChangeListener);
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return new ValueChangeListener() {
			
			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				new ControllerWorker(valueChangeEvent);
			}
		};
	}

	@Override
	protected boolean setComponent(Component component) {
		return false;
	}

	@Override
	protected void clear() {
		super.clear();
		txtField.removeActionListener(txtActionListener);
		txtField.removeKeyListener(txtKeyListener);
		txtField = null;
		slider.removeFocusListener(sliderFocusListener);
		slider.removeChangeListener(sliderChangeListener);
		slider = null;
		value = null;
		txtActionListener = null;
		txtKeyListener = null;
		sliderFocusListener = null;
		sliderChangeListener = null;
	}

	@Override
	protected void setListeners() {

		txtActionListener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				value.setValue(txtField.getText());
				((ConfigurationSetter)getPacketWork()).preparePacketToSend(new IdValue(getPacketWork().getPacketThread().getPacket().getHeader().getPacketId(), value));
				setSend(true);
				slider.setValue(value.getRelativeValue());
			}
		};

		txtKeyListener = new KeyListener() {
			
			@Override public void keyTyped(KeyEvent keyEvent) {}
			@Override public void keyReleased(KeyEvent keyEvent) {

				switch(keyEvent.getExtendedKeyCode()){
					case KeyEvent.VK_UP:
						slider.setValue(slider.getValue()+(slider.getSnapToTicks() ? slider.getMinorTickSpacing() : 1));
						setValue(slider.getValue());
						break;
					case KeyEvent.VK_DOWN:
						slider.setValue(slider.getValue()-(slider.getSnapToTicks() ? slider.getMinorTickSpacing() : 1));
						setValue(slider.getValue());
				}
			}
			@Override public void keyPressed(KeyEvent arg0) {}
		};

		sliderChangeListener = new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				if (slider.isFocusOwner())
					if (!slider.getValueIsAdjusting()) {
						setValue(slider.getValue());
					} else {
						if(value!=null){
							Value v = value.getCopy();
							v.setRelativeValue(slider.getValue());
							txtField.setText(v.toString());
						}
					}
			}
		};

		sliderFocusListener = new FocusListener() {

			@Override
			public void focusLost(FocusEvent arg0) {
				if(slider!=null)
					slider.removeChangeListener(sliderChangeListener);
			}
			
			@Override
			public void focusGained(FocusEvent arg0) {
				if(slider!=null)
					slider.addChangeListener(sliderChangeListener);
			}
		};
	}

	protected void setValue(int value) {
		if(this.value!=null){
			this.value.setRelativeValue(value);
			short packetId = getPacketWork().getPacketThread().getPacket().getHeader().getPacketId();

			((ConfigurationSetter)getPacketWork()).preparePacketToSend(new IdValue(packetId, this.value));
			setSend(true);
		}
	}

	public Value getValue() {
		return value;
	}

	//********************* class ControllerWorker *****************
	private class ControllerWorker extends Thread {

		private ValueChangeEvent valueChangeEvent;

		public ControllerWorker(ValueChangeEvent valueChangeEvent){
			setDaemon(true);
			this.valueChangeEvent = valueChangeEvent;
			int priority = getPriority();
			if(priority>Thread.MIN_PRIORITY)
				setPriority(priority-1);
			start();
		}

		@Override
		public void run() {
			ConfigurationSetter cs = (ConfigurationSetter)getPacketWork();
			short packetId = cs.getPacketId();
			if( valueChangeEvent.getID()==packetId){
				Object source = valueChangeEvent.getSource();

				switch(source.getClass().getSimpleName()){
				case "Byte":
					txtField.setToolTipText(PacketHeader.getOptionStr((byte) source));
					cs.preparePacketToSend(new IdValue(packetId, null));
					setSend(true, false);
					break;
				case "Integer":
					txtField.setToolTipText("");
					setValue((Integer)valueChangeEvent.getSource());
					if(style==Style.CHECK_ONCE)
						setSend(false);
					else
						((ConfigurationSetter)getPacketWork()).preparePacketToSend(new IdValue(packetId, null));
					break;
				default:
					txtField.setToolTipText("");
					setValue((Long)valueChangeEvent.getSource());
					if(style==Style.CHECK_ONCE)
						setSend(false);
					else
						((ConfigurationSetter)getPacketWork()).preparePacketToSend(new IdValue(packetId, null));
				}
			}
		}

		private void setValue(long l) {
			value.setValue(l);
			txtField.setText(value.toString());
			slider.setValue(value.getRelativeValue());
		}
	}
}
