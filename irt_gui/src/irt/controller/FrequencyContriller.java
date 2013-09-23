package irt.controller;

import irt.controller.serial_port.value.seter.ConfigurationSetter;
import irt.data.PacketWork;
import irt.data.Range;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.value.ValueFrequency;

import java.awt.Component;

import javax.swing.JSlider;
import javax.swing.JTextField;

public class FrequencyContriller extends ValueRangeControllerAbstract {

	private Style style;

	public FrequencyContriller(LinkHeader linkHeader, JTextField txtField,JSlider slider, JTextField txtStep, Style style) {
		super(new ConfigurationSetter(linkHeader, Packet.IRT_SLCP_PARAMETER_FCM_CONFIG_FREQUENCY_RANGE, PacketWork.PACKET_ID_CONFIGURATION_FREQUENCY_RANGE), txtField, slider, txtStep, Style.CHECK_ONCE);
		this.style = style;
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return new ValueChangeListener() {

			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				if(valueChangeEvent.getID()==PacketWork.PACKET_ID_CONFIGURATION_FREQUENCY_RANGE){
					boolean isConverter = getPacketWork().getPacketThread().getLinkHeader()==null;
					Object source = valueChangeEvent.getSource();
					if(source instanceof Range){
						Range r = (Range) source;
						startTextSliderController(new ValueFrequency(0,r.getMinimum(), r.getMaximum()), PacketWork.PACKET_ID_CONFIGURATION_FREQUENCY, isConverter ? Packet.IRT_SLCP_PARAMETER_FCM_CONFIG_FREQUENCY : Packet.IRT_SLCP_PARAMETER_25W_BAIS_CONFIGURATION_ATTENUATION, style);
					}
				}
			}
		};
	}

	@Override
	protected boolean setComponent(Component component) {
		return false;
	}

}
