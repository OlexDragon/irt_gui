package irt.controller;

import javax.swing.JSlider;
import javax.swing.JTextField;

import irt.controller.serial_port.value.setter.ConfigurationSetter;
import irt.controller.translation.Translation;
import irt.data.DeviceInfo;
import irt.data.Range;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.interfaces.PacketWork;
import irt.data.value.ValueDouble;

public class AttenuationController extends ValueRangeControllerAbstract {

	private Style style;
	protected int deviceType;

	public AttenuationController(int deviceType, LinkHeader linkHeader, JTextField txtField, JSlider slider, JTextField txtStep, Style style) {
		super(deviceType,
				"Attenuation Controller",
				new ConfigurationSetter(linkHeader,
						PacketImp.PARAMETER_CONFIG_ATTENUATION_RANGE,
						PacketWork.PACKET_ID_CONFIGURATION_ATTENUATION_RANGE),
				txtField,
				slider,
				txtStep,
				Style.CHECK_ONCE);
		this.style = style;
		this.deviceType = deviceType;
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return new ValueChangeListener() {
			
			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				if(valueChangeEvent.getID()==PacketWork.PACKET_ID_CONFIGURATION_ATTENUATION_RANGE)
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
			boolean isConverter = getPacketWork().getPacketThread().getLinkHeader()==null;
			Object source = valueChangeEvent.getSource();
			if(source instanceof Range){
				String prefix = Translation.getValue(String.class, "db", " dB");

				Range r = (Range)source;

				long minimum = r.getMinimum();
				long maximum = r.getMaximum();
				ValueDouble stepValue = new ValueDouble(1, 1, maximum-minimum, 1);
				stepValue.setPrefix(prefix);
				setStepValue(stepValue);

				ValueDouble value = new ValueDouble(0, minimum, maximum, 1);
				value.setPrefix(prefix);
				startTextSliderController(AttenuationController.this.getName(), value, PacketWork.PACKET_ID_CONFIGURATION_ATTENUATION, isConverter || deviceType==DeviceInfo.DEVICE_TYPE_L_TO_KU_OUTDOOR ? PacketImp.PARAMETER_CONFIG_FCM_ATTENUATION : PacketImp.PARAMETER_ID_CONFIGURATION_ATTENUATION, style);
			}
			}catch (Exception e) {
				logger.catching(e);
			}
		}

	}
}
