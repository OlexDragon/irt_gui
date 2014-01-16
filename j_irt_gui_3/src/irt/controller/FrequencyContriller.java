package irt.controller;

import irt.controller.serial_port.value.seter.ConfigurationSetter;
import irt.data.PacketWork;
import irt.data.Range;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.packet.PacketHeader;
import irt.data.value.ValueFrequency;

import java.awt.Component;

import javax.swing.JSlider;
import javax.swing.JTextField;

public class FrequencyContriller extends ValueRangeControllerAbstract {

	private final boolean isConverter;
	private Style style;

	public FrequencyContriller(LinkHeader linkHeader, JTextField txtField, JSlider slider, JTextField txtStep, Style style) {
		super("Frequency Controller", new ConfigurationSetter(
				linkHeader,
				linkHeader==null ? Packet.IRT_SLCP_DATA_FCM_CONFIG_FREQUENCY_RANGE : Packet.IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_USER_FREQUENCY_RANGE,
						PacketWork.PACKET_ID_CONFIGURATION_FREQUENCY_RANGE), txtField, slider, txtStep, Style.CHECK_ONCE);

		isConverter = linkHeader==null;
		this.style = style;
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return new ValueChangeListener() {

			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				int id = valueChangeEvent.getID();
				if(id==PacketWork.PACKET_ID_CONFIGURATION_FREQUENCY_RANGE)
					new ControllerWorker(valueChangeEvent);
			}
		};
	}

	@Override
	protected boolean setComponent(Component component) {
		return false;
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
			Object source = valueChangeEvent.getSource();
			if(source instanceof Range){
				txtField.setToolTipText("");
				Range r = (Range) source;

				long minimum = r.getMinimum();
				long maximum = r.getMaximum();
				setStepValue(new ValueFrequency(1, 1, maximum-minimum));

				startTextSliderController(FrequencyContriller.this.getName(), new ValueFrequency(0,minimum, maximum), PacketWork.PACKET_ID_CONFIGURATION_FREQUENCY, isConverter ? Packet.IRT_SLCP_DATA_FCM_CONFIG_FREQUENCY : Packet.IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_USER_FREQUENCY, style);

			}else if(source instanceof Byte)
				txtField.setToolTipText(PacketHeader.getOptionStr((byte) source));
		}

	}
}
