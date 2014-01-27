package irt.controller.monitor;

import irt.controller.MeasurementController;
import irt.controller.control.ControllerAbstract;
import irt.controller.serial_port.value.getter.MeasurementGetter;
import irt.data.PacketWork;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.Packet;
import irt.data.value.Value;
import irt.data.value.ValueDouble;
import irt.tools.label.LED;
import irt.tools.panel.subpanel.progressBar.ProgressBar;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class MonitorControllerConverter extends ControllerAbstract {

	public static final int	LOCK1			= 1,	//FCM_STATUS_LOCK_DETECT_PLL1
							LOCK2			= 1<<1,	//FCM_STATUS_LOCK_DETECT_PLL2
							MUTE			= 1<<2,	//FCM_STATUS_MUTE
							MUTE_TTL		= 1<<3,	//FCM_STATUS_TTL_MUTE_CONTROL
							LOCK3			= 1<<4,	//FCM_STATUS_LOCK_DETECT_PLL3
							LOCK			= 1<<5,	//FCM_STATUS_LOCK_DETECT_SUMMARY
							INPUT_OWERDRIVE	= 1<<6;	//FCM_STATUS_INPUT_OVERDRIVE

	private LED ledLock;
	private LED ledMute;
	private List<ControllerAbstract> controllerList;

	private JLabel lblInputPower;

	public MonitorControllerConverter(JPanel monitorPanel) {
		super("Converter Monitor Controller", new MeasurementGetter(), monitorPanel, Style.CHECK_ONCE);
	}

	@Override
	protected void setListeners() {
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return new ValueChangeListener() {

			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				int id = valueChangeEvent.getID();
				if(id==getPacketWork().getPacketThread().getPacket().getHeader().getPacketId()){

					Object source = valueChangeEvent.getSource();
					if(source instanceof Long){
						Long status = (Long)source;
						boolean isNotLock = (status&LOCK)==0;
						boolean isMute = (status&(MUTE|MUTE_TTL)) > 0;
//						boolean isInputOverdrive = (status&INPUT_OWERDRIVE)==0;

						if(isNotLock){
							boolean isNotLock1 = (status&LOCK1)==0;
							boolean isNotLock2 = (status&LOCK2)==0;
							boolean isNotLock3 = (status&LOCK3)==0;
							ledLock.setLedColor(Color.RED);
							String s = (isNotLock1 ? "PLL1; ":"")+(isNotLock2 ? "PLL2;" : "")+(isNotLock3 ? "PLL3;" : "");
							ledLock.setToolTipText(s);
						}else{
							ledLock.setLedColor(Color.GREEN);
							ledLock.setToolTipText("Locked");
						}

						ledMute.setOn(isMute);
						ledMute.setToolTipText("Status flags= "+status);

//						if(isInputOverdrive){
//							lblInputPower.setForeground(Color.RED);
//							lblInputPower.setToolTipText("Overdrive");
//						}else{
//							lblInputPower.setBackground(Color.BLACK);
//							lblInputPower.setToolTipText("");
//						}

						fireStatusChangeListener(new ValueChangeEvent(new Integer((isNotLock ? 0 : MonitorController.LOCK)+(isMute ? MonitorController.MUTE : 0)), PacketWork.PACKET_ID_MEASUREMENT_STATUS));
					}else
						ledLock.setToolTipText("Error:"+source);
				}
			}
		};
	}

	@Override
	protected boolean setComponent(Component component) {
		boolean isSet = true;
		if(controllerList==null)
			controllerList = new ArrayList<>();

		Thread t;
		String name = component.getName();
		switch(name){
		case "Lock":
			ledLock = (LED) component;
			break;
		case "Mute":
			ledMute = (LED) component;
			break;
		case "Input Power":
			Value value = new ValueDouble(0, 1);
			value.setPrefix(" dBm");
			lblInputPower = (JLabel)component;
			ControllerAbstract abstractController = new MeasurementController(name, lblInputPower, Packet.IRT_SLCP_PARAMETER_FCM_MEASUREMENT_INPUT_POWER, value, PacketWork.PACKET_ID_MEASUREMENT_INPUT_POWER);
			t = new Thread(abstractController, name);
			t.setDaemon(true);
			t.start();
			controllerList.add(abstractController);
			break;
		case "Output Power":
			value = new ValueDouble(0, 1);
			value.setPrefix(" dBm");
			abstractController = new MeasurementController(name, (JLabel)component, Packet.IRT_SLCP_PARAMETER_FCM_MEASUREMENT_OUTPUT_POWER, value, PacketWork.PACKET_ID_MEASUREMENT_OUTPUT_POWER);
			t = new Thread(abstractController, name);
			t.setDaemon(true);
			t.start();
			controllerList.add(abstractController);
			abstractController.setObservable(ProgressBar.getValue());
			break;
		case "Unit Temp":
			value = new ValueDouble(0, 1);
			value.setPrefix(" C");
			abstractController = new MeasurementController(name, (JLabel)component, Packet.IRT_SLCP_PARAMETER_MEASUREMENT_FCM_TEMPERATURE, value, PacketWork.PACKET_ID_MEASUREMENT_UNIT_TEMPERATURE);
			t = new Thread(abstractController, name);
			t.setDaemon(true);
			t.start();
			controllerList.add(abstractController);
			break;
		case "CPU Temp":
			value = new ValueDouble(0, 1);
			value.setPrefix(" C");
			abstractController = new MeasurementController(name, (JLabel)component, Packet.IRT_SLCP_PARAMETER_MEASUREMENT_FCM_TEMPERATURE_CPU, value, PacketWork.PACKET_ID_MEASUREMENT_CPU_TEMPERATURE);
			t = new Thread(abstractController, name);
			t.setDaemon(true);
			t.start();
			controllerList.add(abstractController);
			break;
		case "5.5V":
			value = new ValueDouble(0, 3);
			value.setPrefix(" V");
			abstractController = new MeasurementController(name, (JLabel)component, Packet.IRT_SLCP_PARAMETER_MEASUREMENT_FCM_MON_5V5, value, PacketWork.PACKET_ID_MEASUREMENT_5V5);
			t = new Thread(abstractController, name);
			t.setDaemon(true);
			t.start();
			controllerList.add(abstractController);
			break;
		case "13.2V":
			value = new ValueDouble(0, 3);
			value.setPrefix(" V");
			abstractController = new MeasurementController(name, (JLabel)component, Packet.IRT_SLCP_PARAMETER_MEASUREMENT_FCM_MON_13V2_POS, value, PacketWork.PACKET_ID_MEASUREMENT_13V2);
			t = new Thread(abstractController, name);
			t.setDaemon(true);
			t.start();
			controllerList.add(abstractController);
			break;
		case "-13.2V":
			value = new ValueDouble(0, 3);
			value.setPrefix(" V");
			abstractController = new MeasurementController(name, (JLabel)component, Packet.IRT_SLCP_PARAMETER_MEASUREMENT_FCM_MON_13V2_NEG, value, PacketWork.PACKET_ID_MEASUREMENT_13V2_NEG);
			t = new Thread(abstractController, name);
			t.setDaemon(true);
			t.start();
			controllerList.add(abstractController);
			break;
//		default:
//			isSet = false;
//			System.out.println("Not used Monitor: "+component.getName()+" : "+component.getClass().getSimpleName());
		}

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

}
