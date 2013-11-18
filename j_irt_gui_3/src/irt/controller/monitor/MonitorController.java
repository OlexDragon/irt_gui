package irt.controller.monitor;

import irt.controller.MeasurementController;
import irt.controller.control.ControllerAbstract;
import irt.controller.serial_port.value.getter.GetterAbstract;
import irt.controller.serial_port.value.getter.MeasurementGetter;
import irt.controller.translation.Translation;
import irt.data.LinkedPacketThread;
import irt.data.PacketWork;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.value.Value;
import irt.data.value.ValueDouble;
import irt.tools.label.LED;
import irt.tools.panel.subpanel.monitor.MonitorPanelAbstract;
import irt.tools.panel.subpanel.progressBar.ProgressBar;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;

public class MonitorController extends ControllerAbstract {

	public static final int MUTE = 1,
							LOCK = 2;

	private LED ledLock;
	private LED ledMute;
	private List<ControllerAbstract> controllerList;

	public MonitorController(LinkHeader linkHeader, MonitorPanelAbstract monitorPanel) {
		super(new MeasurementGetter(linkHeader), monitorPanel, Style.CHECK_ONCE);
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return new ValueChangeListener() {

			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				if(valueChangeEvent.getID()==((GetterAbstract)getPacketWork()).getPacketId()){

					Object source = valueChangeEvent.getSource();
					ledLock.setToolTipText(source.toString());
					if(source instanceof Long){
							Long status = ((Long)source);

						if((status&LOCK)==0)
							ledLock.setLedColor(Color.RED);
						else
							ledLock.setLedColor(Color.GREEN);

						ledMute.setOn((	status&MUTE)>0);

						fireStatusChangeListener(new ValueChangeEvent( status, PacketWork.PACKET_ID_MEASUREMENT_STATUS));
					}
					ledMute.setToolTipText("Status flags= "+source);
				}
			}
		};
	}

	@Override
	protected boolean setComponent(Component component) {
		if(controllerList==null)
			controllerList = new ArrayList<>();

		boolean isSet = true;
		String name = component.getName();
		Thread t;
		if(name!=null) {
			String prefix = Translation.getValue(String.class, "dbm", " dBm");
			switch(name){
			case "Lock":
				ledLock = (LED)component;
				break;
			case "Mute":
				ledMute = (LED)component;
				break;
			case "Input Power":
				Value value = new ValueDouble(0, 1);
				value.setPrefix(prefix);
				ControllerAbstract abstractController = new MeasurementController(((LinkedPacketThread)getPacketWork().getPacketThread()).getLinkHeader(),(JLabel)component, Packet.IRT_SLCP_PARAMETER_PICOBUC_MEASUREMENT_INPUT_POWER, value, PacketWork.PACKET_ID_MEASUREMENT_INPUT_POWER);
				t = new Thread(abstractController, "Input Power");
				t.setDaemon(true);
				t.start();
				controllerList.add(abstractController);
				break;
			case "Output Power":
				value = new ValueDouble(0, 1);
				value.setPrefix(prefix);
				abstractController = new MeasurementController(((LinkedPacketThread)getPacketWork().getPacketThread()).getLinkHeader(),(JLabel)component, Packet.IRT_SLCP_PARAMETER_PICOBUC_MEASUREMENT_OUTPUT_POWER, value, PacketWork.PACKET_ID_MEASUREMENT_BAIAS_25W_OUTPUT_POWER);
				t = new Thread(abstractController, "Output Power");
				t.setDaemon(true);
				t.start();
				controllerList.add(abstractController);
				abstractController.setObservable(ProgressBar.getValue());
				break;
			case "Temperature":
				value = new ValueDouble(0, 1);
				value.setPrefix(" C");
				abstractController = new MeasurementController(((LinkedPacketThread)getPacketWork().getPacketThread()).getLinkHeader(),(JLabel)component, Packet.IRT_SLCP_PARAMETER_MEASUREMENT_25W_BAIS_TEMPERATURE, value, PacketWork.PACKET_ID_MEASUREMENT_BIAS_25W_TEMPERATURE);
				t = new Thread(abstractController, "Temperature");
				t.setDaemon(true);
				t.start();
				controllerList.add(abstractController);
				break;
			default:
				isSet = false;
			}
		} else
			isSet = false;

		return isSet;
	}

	@Override
	protected void clear() {
		super.clear();
		for(ControllerAbstract c:controllerList)
			c.setRun(false);
		controllerList.clear();
		controllerList = null;
		ledLock = null;
		ledMute = null;
	}

	@Override
	protected void setListeners() {
		// TODO Auto-generated method stub
		
	}
}
