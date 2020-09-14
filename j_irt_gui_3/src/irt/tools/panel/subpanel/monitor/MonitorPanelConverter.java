package irt.tools.panel.subpanel.monitor;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.DefaultController;
import irt.controller.control.ControllerAbstract;
import irt.controller.translation.Translation;
import irt.data.DeviceInfo.DeviceType;
import irt.data.packet.PacketImp;
import irt.data.packet.ParameterHeader;
import irt.data.packet.Payload;
import irt.data.packet.PacketIDs;
import irt.data.value.ValueDouble;
import irt.tools.label.LED;

@SuppressWarnings("serial")
public class MonitorPanelConverter extends MonitorPanelAbstract {

	protected static final Logger logger = LogManager.getLogger();

	public static final int
			LOCK1			= 1,	//FCM_STATUS_LOCK_DETECT_PLL1
			LOCK2			= 1<<1,	//FCM_STATUS_LOCK_DETECT_PLL2
			MUTE			= 1<<2,	//FCM_STATUS_MUTE
			MUTE_TTL		= 1<<3,	//FCM_STATUS_TTL_MUTE_CONTROL
			LOCK3			= 1<<4,	//FCM_STATUS_LOCK_DETECT_PLL3
			LOCK			= 1<<5,	//FCM_STATUS_LOCK_DETECT_SUMMARY
			INPUT_OWERDRIVE	= 1<<6;	//FCM_STATUS_INPUT_OVERDRIVE

	private JLabel lblOutputPower;
	private JLabel lbl_13V2Output;
	private JLabel lbl5V5Output;
	private JLabel lblUnitTemp;
	private JLabel lblInputPower;
	private JLabel lblCpuTemp;
	private JLabel lbl13V2Output;
	private LED ledMute;
	private LED ledLock;

	private int output = Integer.MIN_VALUE;
	private int temperature = Integer.MIN_VALUE;
	private int status = Integer.MIN_VALUE;
	private int input = Integer.MIN_VALUE;
	private int temperatureCPU = Integer.MIN_VALUE;
	private int mon_13v2_neg = Integer.MIN_VALUE;
	private int mon_13v2_pos = Integer.MIN_VALUE;
	private int mon_5v5 = Integer.MIN_VALUE;

	public MonitorPanelConverter(Optional<DeviceType> deviceType) {
		super(deviceType, null, "IrtControllPanel", 214, 210);
		
		ledMute = new LED(Color.YELLOW, "MUTE");
		ledMute.setName("Mute");
		ledMute.setForeground(Color.GREEN);
		ledMute.setFont(new Font("Tahoma", Font.BOLD, 18));
		ledMute.setBounds(116, 175, 84, 28);
		add(ledMute);
		
		ledLock = new LED(Color.GREEN, "LOCK");
		ledLock.setName("Lock");
		ledLock.setForeground(Color.GREEN);
		ledLock.setFont(new Font("Tahoma", Font.BOLD, 18));
		ledLock.setBounds(18, 175, 81, 28);
		add(ledLock);
		
		JLabel lblCpuTemp_1 = new JLabel("CPU Temp:");
		lblCpuTemp_1.setName("");
		lblCpuTemp_1.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCpuTemp_1.setForeground(new Color(153, 255, 255));
		lblCpuTemp_1.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblCpuTemp_1.setBounds(15, 86, 93, 17);
		add(lblCpuTemp_1);
		
		lblCpuTemp = new JLabel(":");
		lblCpuTemp.setName("CPU Temp");
		lblCpuTemp.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCpuTemp.setForeground(Color.WHITE);
		lblCpuTemp.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblCpuTemp.setBounds(121, 86, 82, 17);
		add(lblCpuTemp);
		
		JLabel lblUnitTemp_1 = new JLabel("Unit Temp:");
		lblUnitTemp_1.setName("");
		lblUnitTemp_1.setHorizontalAlignment(SwingConstants.RIGHT);
		lblUnitTemp_1.setForeground(new Color(153, 255, 255));
		lblUnitTemp_1.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblUnitTemp_1.setBounds(15, 62, 93, 17);
		add(lblUnitTemp_1);
		
		JLabel label_3 = new JLabel("Input Power:");
		label_3.setName("");
		label_3.setHorizontalAlignment(SwingConstants.RIGHT);
		label_3.setForeground(new Color(153, 255, 255));
		label_3.setFont(new Font("Tahoma", Font.PLAIN, 12));
		label_3.setBounds(15, 18, 93, 17);
		add(label_3);
		
		lblInputPower = new JLabel(":");
		lblInputPower.setName("Input Power");
		lblInputPower.setHorizontalAlignment(SwingConstants.RIGHT);
		lblInputPower.setForeground(Color.WHITE);
		lblInputPower.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblInputPower.setBounds(121, 18, 82, 17);
		add(lblInputPower);
		
		lblUnitTemp = new JLabel(":");
		lblUnitTemp.setName("Unit Temp");
		lblUnitTemp.setHorizontalAlignment(SwingConstants.RIGHT);
		lblUnitTemp.setForeground(Color.WHITE);
		lblUnitTemp.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblUnitTemp.setBounds(121, 62, 82, 17);
		add(lblUnitTemp);
		
		JLabel lblV_2 = new JLabel("-13.2 V:");
		lblV_2.setName("");
		lblV_2.setHorizontalAlignment(SwingConstants.RIGHT);
		lblV_2.setForeground(new Color(153, 255, 255));
		lblV_2.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblV_2.setBounds(15, 158, 93, 17);
		add(lblV_2);
		
		JLabel lblV_1 = new JLabel("13.2 V:");
		lblV_1.setName("");
		lblV_1.setHorizontalAlignment(SwingConstants.RIGHT);
		lblV_1.setForeground(new Color(153, 255, 255));
		lblV_1.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblV_1.setBounds(15, 134, 93, 17);
		add(lblV_1);
		
		JLabel lblV = new JLabel("5.5 V:");
		lblV.setName("");
		lblV.setHorizontalAlignment(SwingConstants.RIGHT);
		lblV.setForeground(new Color(153, 255, 255));
		lblV.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblV.setBounds(15, 110, 93, 17);
		add(lblV);
		
		lbl5V5Output = new JLabel(":");
		lbl5V5Output.setName("5.5V");
		lbl5V5Output.setHorizontalAlignment(SwingConstants.RIGHT);
		lbl5V5Output.setForeground(Color.WHITE);
		lbl5V5Output.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lbl5V5Output.setBounds(121, 110, 82, 17);
		add(lbl5V5Output);
		
		lbl13V2Output = new JLabel(":");
		lbl13V2Output.setName("13.2V");
		lbl13V2Output.setHorizontalAlignment(SwingConstants.RIGHT);
		lbl13V2Output.setForeground(Color.WHITE);
		lbl13V2Output.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lbl13V2Output.setBounds(121, 134, 82, 17);
		add(lbl13V2Output);
		
		lbl_13V2Output = new JLabel(":");
		lbl_13V2Output.setName("-13.2V");
		lbl_13V2Output.setHorizontalAlignment(SwingConstants.RIGHT);
		lbl_13V2Output.setForeground(Color.WHITE);
		lbl_13V2Output.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lbl_13V2Output.setBounds(121, 158, 82, 17);
		add(lbl_13V2Output);
		
		JLabel lblOutputPower_1 = new JLabel("Output Power:");
		lblOutputPower_1.setName("");
		lblOutputPower_1.setHorizontalAlignment(SwingConstants.RIGHT);
		lblOutputPower_1.setForeground(new Color(153, 255, 255));
		lblOutputPower_1.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblOutputPower_1.setBounds(15, 38, 93, 17);
		add(lblOutputPower_1);
		
		lblOutputPower = new JLabel(":");
		lblOutputPower.setName("Output Power");
		lblOutputPower.setHorizontalAlignment(SwingConstants.RIGHT);
		lblOutputPower.setForeground(Color.WHITE);
		lblOutputPower.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblOutputPower.setBounds(121, 38, 82, 17);
		add(lblOutputPower);
	}

	@Override
	protected List<ControllerAbstract> getControllers() {
		List<ControllerAbstract> controllers = new ArrayList<>();
		DefaultController defaultController = getController(
				"IrtControllPanel",
				PacketImp.PARAMETER_MEASUREMENT_FCM_ALL,
				PacketIDs.MEASUREMENT_ALL);
		controllers.add(defaultController);
		return controllers;
	}

	@Override
	protected void packetRecived(List<Payload> payloads) {

		if(payloads!=null){
			byte flags = 1;
			int value = 0;

			for(Payload pl:payloads){
				ParameterHeader parameterHeader = pl.getParameterHeader();
				switch(parameterHeader.getSize()){
				case 2:
					value = pl.getShort(0);
					break;
				case 3:
					flags = (byte) (pl.getByte()&0x03);
					value =  pl.getShort((byte)1);
					break;
				case 4:
					value = pl.getInt(0);
				}

				packetRecived(parameterHeader.getCode(), flags, value);
			}
		}
	}

	private void packetRecived(byte parameter, byte flags, int value) {
		logger.trace("parameter={}, flags={}, value={}", parameter, flags, value);

		String text;
		ValueDouble v;
		switch(parameter){
		case PacketImp.PARAMETER_MEASUREMENT_FCM_INPUT_POWER:
			if (value != input) {
				input = value;

				if (flags == 0) {
					text = "N/A";
				} else {
					v = new ValueDouble(value, 1);
					v.setPrefix(Translation.getValue(String.class, "dbm", "dBm"));
					text = getOperator(flags) + v.toString();
				}

				lblInputPower.setText(text);
				logger.trace("PARAMETER_MEASUREMENT_PICOBUC_INPUT_POWER, flags={}, value={}", flags, value);
			}
			break;
		case PacketImp.PARAMETER_MEASUREMENT_FCM_OUTPUT_POWER:
			if (value != output) {
				output = value;

				if (flags == 0)
					text = "N/A";
				else {
					v = new ValueDouble(value, 1);
					v.setPrefix(Translation.getValue(String.class, "dbm", "dBm"));
					text = getOperator(flags) + v.toString();
				}

				lblOutputPower.setText(text);
				logger.trace("PARAMETER_MEASUREMENT_FCM_OUTPUT_POWER, flags={}, value={}", flags, value);
			}
			break;
		case PacketImp.PARAMETER_MEASUREMENT_TEMPERATURE:
			if(value!=temperature){
				temperature = value;
				v = new ValueDouble(value, 1);
				v.setPrefix(" C");
				lblUnitTemp.setText(v.toString());
				logger.trace("PARAMETER_MEASUREMENT_FCM_TEMPERATURE, flags={}, value={}", flags, value);
			}
			break;
		case PacketImp.PARAMETER_MEASUREMENT_FCM_TEMPERATURE_CPU:
			if(value!=temperatureCPU){
				temperatureCPU = value;
				v = new ValueDouble(value, 1);
				v.setPrefix(" C");
				lblCpuTemp.setText(v.toString());
				logger.trace("PARAMETER_MEASUREMENT_FCM_TEMPERATURE_CPU, flags={}, value={}", flags, value);
			}
			break;
		case PacketImp.PARAMETER_MEASUREMENT_FCM_MON_13V2_NEG:
			if(value!=mon_13v2_neg){
				mon_13v2_neg = value;
				v = new ValueDouble(value, 3);
				v.setPrefix(" V");
				lbl_13V2Output.setText(v.toString());
				logger.trace("PARAMETER_MEASUREMENT_FCM_MON_13V2_NEG, flags={}, value={}", flags, value);
			}
			break;
		case PacketImp.PARAMETER_MEASUREMENT_FCM_MON_13V2_POS:
			if(value!=mon_13v2_pos){
				mon_13v2_pos = value;
				v = new ValueDouble(value, 3);
				v.setPrefix(" V");
				lbl13V2Output.setText(v.toString());
				logger.trace("PARAMETER_MEASUREMENT_FCM_MON_13V2_POS, flags={}, value={}", flags, value);
			}
			break;
		case PacketImp.PARAMETER_MEASUREMENT_FCM_MON_5V5:
			if(value!=mon_5v5){
				mon_5v5 = value;
				v = new ValueDouble(value, 3);
				v.setPrefix(" V");
				lbl5V5Output.setText(v.toString());
				logger.trace("PARAMETER_MEASUREMENT_FCM_MON_5V5, flags={}, value={}", flags, value);
			}
			break;
		case PacketImp.PARAMETER_MEASUREMENT_FCM_STATUS:
			if(status!=value){
				status = value;
				setStatus(value);
			}
		}
	}

	private void setStatus(int status) {

		boolean isNotLock = (status&LOCK)==0;
		boolean isMute = (status&(MUTE|MUTE_TTL)) > 0;

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

//		boolean isInputOverdrive = (status&INPUT_OWERDRIVE)==0;
//			if(isInputOverdrive){
//				lblInputPower.setForeground(Color.RED);
//				lblInputPower.setToolTipText("Overdrive");
//			}else{
//				lblInputPower.setBackground(Color.BLACK);
//				lblInputPower.setToolTipText("");
//			}
	
	}
}
