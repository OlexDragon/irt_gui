package irt.controller;

import java.awt.Component;
import java.text.DecimalFormat;

import javax.swing.JLabel;

import org.apache.logging.log4j.Logger;

import irt.controller.control.ControllerAbstract;
import irt.controller.serial_port.value.getter.GetterAbstract;
import irt.data.PacketWork;
import irt.data.RegisterValue;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.PacketHeader;
import irt.data.value.Value;

public class AdcController extends ControllerAbstract {

	private final double multiplier;

	private JLabel label;
	
	Value value = new Value(0, 0, 4095, 0);

	public AdcController(int deviceType, String controllerName, JLabel label, PacketWork packetWork, double multiplier, Logger logger) {
		super(deviceType, controllerName, packetWork, null, null, logger);
		this.label = label;
		this.multiplier = multiplier;
	}

	public AdcController(int deviceType, String controllerName, JLabel label, PacketWork packetWork, Value value, double multiplier, Logger logger) {
		this(deviceType, controllerName, label, packetWork, multiplier, logger);
		this.value = value;
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return new ValueChangeListener() {

			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {

				if(valueChangeEvent.getID()==((GetterAbstract)getPacketWork()).getPacketId())
					new ControllerWorker(valueChangeEvent, multiplier);
			}
		};
	}

	public void setText(String toolTip, String text) {
		label.setText(text);
		label.setToolTipText(toolTip);
	}

	@Override
	protected void setListeners() {
	}

	@Override
	protected boolean setComponent(Component component) {
		return false;
	}

	@Override
	protected void clear() {
		super.clear();
		label = null;
	}

	//********************* class ControllerWorker *****************
	private class ControllerWorker extends Thread {

		private final double multiplier;
		private ValueChangeEvent valueChangeEvent;

		public ControllerWorker(ValueChangeEvent valueChangeEvent, double multiplier){
			this.multiplier = multiplier;
			this.valueChangeEvent = valueChangeEvent;
			int priority = getPriority();
			if(priority>Thread.MIN_PRIORITY)
				setPriority(priority-1);
			setDaemon(true);
			start();
		}

		@Override
		public void run() {
			Object source = valueChangeEvent.getSource();
			if(source instanceof Byte){

				Byte b = (Byte)source;
				label.setText("error"+b);
				label.setToolTipText(PacketHeader.getOptionStr((byte) Math.abs(b)));

			}else{
				
				RegisterValue urv = (RegisterValue)getPacketWork().getPacketThread().getValue();
				Value uv = urv.getValue();
				long sourceValue = ((RegisterValue)source).getValue().getValue();
				if(uv==null){
					value.setValue(sourceValue);
					urv.setValue(value);
				}else
					value.setValue(sourceValue);
			
				String string = value.toString();
				setText(new DecimalFormat("#.### A").format(multiplier*sourceValue/1000), string);
			}
		}

	}
}
