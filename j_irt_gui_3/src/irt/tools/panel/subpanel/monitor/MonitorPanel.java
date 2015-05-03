package irt.tools.panel.subpanel.monitor;
import irt.controller.translation.Translation;
import irt.data.DeviceInfo;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.value.ValueDouble;
import irt.tools.label.LED;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.util.concurrent.ExecutionException;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;


@SuppressWarnings("serial")
public class MonitorPanel extends MonitorPanelSSPA {

	protected JLabel lblInputPowerTxt;
	protected JLabel lblInputPower;
	private LED ledLock;
	private int input;

	public MonitorPanel(int deviceType, LinkHeader linkHeader) {
		super(deviceType, linkHeader);


		lblInputPower = new JLabel(":");
		lblInputPower.setName("Input Power");
		lblInputPower.setHorizontalAlignment(SwingConstants.RIGHT);
		lblInputPower.setForeground(Color.WHITE);
		int y = deviceType!=DeviceInfo.DEVICE_TYPE_L_TO_KU_OUTDOOR ? 22 : 45;
		lblInputPower.setBounds(99, y, 100, 17);
		add(lblInputPower);

		lblInputPowerTxt = new JLabel();
		lblInputPowerTxt.setName("");
		lblInputPowerTxt.setHorizontalAlignment(SwingConstants.RIGHT);
		lblInputPowerTxt.setForeground(new Color(153, 255, 255));
		lblInputPowerTxt.setBounds(2, y, 104, 17);
		add(lblInputPowerTxt);
		new TextWorker(lblInputPowerTxt, "input_power", "Input Power").execute();

		ledLock = new LED(Color.GREEN, Translation.getValue(String.class, "lock", "LOCK"));
		ledLock.setName("Lock");
		ledLock.setForeground(Color.GREEN);
		add(ledLock);

		swingWorkers();
	}

	@Override
	protected void swingWorkers() {
		super.swingWorkers();
		//Set LABELS font
		new SwingWorker<Font, Void>() {
			@Override
			protected Font doInBackground() throws Exception {
				return Translation.getFont()
						.deriveFont(Translation.getValue(Float.class, "monitor.labels.font.size", 12f))
						.deriveFont(Translation.getValue(Integer.class, "monitor.labels.font.style", Font.PLAIN));
			}
			@Override
			protected void done() {
				try{
				Font font = get();
				lblInputPower.setFont(font);
				lblInputPowerTxt.setFont(font);
				}catch(InterruptedException | ExecutionException e){
					logger.catching(e);
				}
			}
		}.execute();
		new SwingWorker<Font, Void>() {
			@Override
			protected Font doInBackground() throws Exception {
				return Translation.getFont().deriveFont(Translation.getValue(Float.class, "monitor.leds.font.size", 12f))
						.deriveFont(Translation.getValue(Integer.class, "monitor.leds.font.style", Font.PLAIN));
			}
			@Override
			protected void done() {
				try {
					ledLock.setFont(get());
				} catch (InterruptedException | ExecutionException e) {
					logger.catching(e);
				}
			}
		}.execute();

		new SwingWorker<Rectangle, Void>() {

			@Override
			protected Rectangle doInBackground() throws Exception {
				return new Rectangle(Translation.getValue(Integer.class, "monitor.led.lock.x", 17)
						, Translation.getValue(Integer.class, "monitor.led.lock.y", 138)
						, Translation.getValue(Integer.class, "monitor.led.lock.width", 100)
						, 28);
			}

			@Override
			protected void done() {
				try {
					ledLock.setBounds(get());
				} catch (InterruptedException | ExecutionException e) {
					logger.catching(e);
				}
			}
		}.execute();

	}

	@Override
	public void refresh() {
		logger.entry();
		new TextWorker(ledLock, "lock", "LOCK").execute();
		new TextWorker(lblInputPowerTxt, "input_power", "Input Power").execute();
		super.refresh();
		logger.exit();
	}

	@Override
	protected void packetRecived(byte parameter, byte flags, int value) {
		logger.debug("parameter={}, flags={}, value={}", parameter, flags, value);

		ValueDouble v;
		switch(parameter){
		case Packet.PARAMETER_MEASUREMENT_FCM_INPUT_POWER:
			if(deviceType!=DeviceInfo.DEVICE_TYPE_L_TO_KU_OUTDOOR){
				super.packetRecived(parameter, flags, value);
				break;
			}
		case Packet.IRT_SLCP_PARAMETER_MEASUREMENT_PICOBUC_INPUT_POWER:
			if (value != input) {
				input = value;
				v = new ValueDouble(value, 1);
				v.setPrefix(Translation.getValue(String.class, "dbm", "dBm"));
				lblInputPower.setText(getOperator(flags)+v.toString());
			}
			break;
		default:
			super.packetRecived(parameter, flags, value);
		}
	}

	@Override
	protected void setStatus(int status) {
		super.setStatus(status);

		if ((status & LOCK) == 0)
			ledLock.setLedColor(Color.RED);
		else
			ledLock.setLedColor(Color.GREEN);
	}

	@Override
	protected void setConverterStatus(int status) {
		super.setConverterStatus(status);

		if ((status & MonitorPanelConverter.LOCK) == 0)
			ledLock.setLedColor(Color.RED);
		else
			ledLock.setLedColor(Color.GREEN);
	}
}
