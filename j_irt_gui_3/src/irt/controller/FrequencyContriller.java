package irt.controller;

import java.util.Optional;

import javax.swing.JSlider;
import javax.swing.JTextField;

import irt.controller.serial_port.value.setter.ConfigurationSetter;
import irt.data.DeviceType;
import irt.data.Range;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketIDs;
import irt.data.value.ValueFrequency;

public class FrequencyContriller extends ValueRangeControllerAbstract {

	private final boolean isConverter;
	private Style style;

	public FrequencyContriller(Optional<DeviceType> deviceType, LinkHeader linkHeader, JTextField txtField, JSlider slider, JTextField txtStep, Style style) {
		super(deviceType,
				"Frequency UnitController",
				new ConfigurationSetter(
						linkHeader,
						linkHeader==null || linkHeader.getIntAddr()==0
												? PacketImp.PARAMETER_CONFIG_FCM_FREQUENCY_RANGE
												: PacketImp.PARAMETER_ID_CONFIGURATION_USER_FREQUENCY_RANGE,
												PacketIDs.CONFIGURATION_FREQUENCY_RANGE), txtField, slider, txtStep, Style.CHECK_ONCE);

		isConverter = linkHeader==null || linkHeader.getIntAddr()==0;
		this.style = style;
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		
		return new ValueChangeListener() {
			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				int id = valueChangeEvent.getID();
				if(PacketIDs.CONFIGURATION_FREQUENCY_RANGE.match((short) id))
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
				txtField.setToolTipText("");
				Range r = (Range) source;

				long minimum = r.getMinimum();
				long maximum = r.getMaximum();
				setStepValue(new ValueFrequency(1, 1, maximum-minimum));

				startTextSliderController(FrequencyContriller.this.getName(), new ValueFrequency(0,minimum, maximum), PacketIDs.CONFIGURATION_FREQUENCY, isConverter ? PacketImp.PARAMETER_CONFIG_FCM_FREQUENCY : PacketImp.PARAMETER_ID_CONFIGURATION_USER_FREQUENCY, style);

			}else if(source instanceof Byte)
				txtField.setToolTipText(PacketHeader.getOptionStr((byte) source));
			}catch (Exception e) {
				logger.catching(e);
			}
		}

	}
}
