package irt.controller;

import irt.controller.serial_port.value.setter.ConfigurationSetter;
import irt.data.PacketWork;
import irt.data.Range;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.value.ValueDouble;

import javax.swing.JSlider;
import javax.swing.JTextField;

public class GainController extends ValueRangeControllerAbstract {

	private Style style;

	public GainController(int deviceType, LinkHeader linkHeader, JTextField txtField, JSlider slider, JTextField txtStep, Style style) {
		super(deviceType, "Gain Controller", new ConfigurationSetter(linkHeader, Packet.IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_GAIN_RANGE, PacketWork.PACKET_ID_CONFIGURATION_GAIN_RANGE), txtField, slider, txtStep, Style.CHECK_ONCE);
		this.style = style;
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return new ValueChangeListener() {
			
			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				if(valueChangeEvent.getID()==PacketWork.PACKET_ID_CONFIGURATION_GAIN_RANGE)
					new ControllerWorker(valueChangeEvent);
			}
		};
	}

	//********************* class ControllerWorker *****************
	private class ControllerWorker extends Thread {

		private ValueChangeEvent valueChangeEvent;

		public ControllerWorker(ValueChangeEvent valueChangeEvent){
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
			if(source instanceof Range){
				Range r = (Range) source;
				ValueDouble value = new ValueDouble(0, r.getMinimum(), r.getMaximum(), 1);
				value.setPrefix(" dB");
				startTextSliderController(GainController.this.getName(), value, PacketWork.PACKET_ID_CONFIGURATION_GAIN, Packet.IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_GAIN, style);
			}
		}

	}
}
