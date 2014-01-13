package irt.controller;

import irt.controller.control.ControllerAbstract;
import irt.controller.serial_port.value.getter.GetterAbstract;
import irt.data.PacketWork;
import irt.data.RegisterValue;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.PacketHeader;
import irt.data.value.Value;

import java.awt.Component;
import java.text.DecimalFormat;

import javax.swing.JLabel;

public class AdcController extends ControllerAbstract {

	private JLabel label;
	
	Value value = new Value(0, 0, 4095, 0);

	public AdcController(JLabel label, PacketWork packetWork) {
		super(packetWork, null, null);
		this.label = label;
	}

	public AdcController(JLabel label, PacketWork packetWork, Value value) {
		this(label, packetWork);
		this.value = value;
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return new ValueChangeListener() {

			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {

				if(valueChangeEvent.getID()==((GetterAbstract)getPacketWork()).getPacketId()){

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
						setText(new DecimalFormat("#.## A").format(5.4*sourceValue/1000), string);
					}
				}
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
}
