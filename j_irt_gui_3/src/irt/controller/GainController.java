package irt.controller;

import java.util.Optional;

import javax.swing.JSlider;
import javax.swing.JTextField;

import irt.controller.serial_port.value.setter.ConfigurationSetter;
import irt.data.DeviceInfo.DeviceType;
import irt.data.Range;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketIDs;
import irt.data.value.ValueDouble;

public class GainController extends ValueRangeControllerAbstract {

	private Style style;

	public GainController(Optional<DeviceType> deviceType, LinkHeader linkHeader, JTextField txtField, JSlider slider, JTextField txtStep, Style style) {
		super(deviceType,
				"Gain UnitController",
				new ConfigurationSetter(linkHeader,
						PacketImp.PARAMETER_ID_CONFIGURATION_GAIN_RANGE,
						PacketIDs.CONFIGURATION_GAIN_RANGE),
				txtField,
				slider,
				txtStep,
				Style.CHECK_ONCE);
		this.style = style;
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return new ValueChangeListener() {
			
			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				if(PacketIDs.CONFIGURATION_GAIN_RANGE.match((short) valueChangeEvent.getID()))
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
			try{
			Object source = valueChangeEvent.getSource();
			if(source instanceof Range){
				Range r = (Range) source;
				ValueDouble value = new ValueDouble(0, r.getMinimum(), r.getMaximum(), 1);
				value.setPrefix(" dB");
				startTextSliderController(GainController.this.getName(), value, PacketIDs.CONFIGURATION_GAIN, PacketImp.PARAMETER_ID_CONFIGURATION_GAIN, style);
			}
			}catch (Exception e) {
				logger.catching(e);
			}
		}

	}
}
