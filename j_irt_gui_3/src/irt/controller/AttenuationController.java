package irt.controller;

import irt.controller.serial_port.value.seter.ConfigurationSetter;
import irt.controller.translation.Translation;
import irt.data.PacketWork;
import irt.data.Range;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.value.ValueDouble;

import javax.swing.JSlider;
import javax.swing.JTextField;

public class AttenuationController extends ValueRangeControllerAbstract {

	private Style style;

	public AttenuationController(LinkHeader linkHeader, JTextField txtField, JSlider slider, JTextField txtStep, Style style) {
		super(new ConfigurationSetter(linkHeader, Packet.IRT_SLCP_DATA_FCM_CONFIG_ATTENUATION_RANGE, PacketWork.PACKET_ID_CONFIGURATION_ATTENUATION_RANGE), txtField, slider, txtStep, Style.CHECK_ONCE);
		this.style = style;
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return new ValueChangeListener() {
			
			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				if(valueChangeEvent.getID()==PacketWork.PACKET_ID_CONFIGURATION_ATTENUATION_RANGE){
					boolean isConverter = getPacketWork().getPacketThread().getLinkHeader()==null;
					Object source = valueChangeEvent.getSource();
					if(source instanceof Range){
						Range r = (Range) source;

						long minimum = r.getMinimum();
						long maximum = r.getMaximum();
						ValueDouble stepValue = new ValueDouble(1, 1, maximum-minimum, 1);
						stepValue.setPrefix("dB");
						setStepValue(stepValue);

						ValueDouble value = new ValueDouble(0,r.getMinimum(), r.getMaximum(), 1);
						value.setPrefix(Translation.getValue(String.class, "db", " dB"));
						startTextSliderController(value, PacketWork.PACKET_ID_CONFIGURATION_ATTENUATION, isConverter ? Packet.IRT_SLCP_DATA_FCM_CONFIG_ATTENUATION : Packet.IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_ATTENUATION, style);
					}
				}
			}
		};
	}
}
