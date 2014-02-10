package irt.tools.panel.subpanel.monitor;
import irt.controller.translation.Translation;
import irt.data.packet.LinkHeader;
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

	private LED ledLock;

	public MonitorPanel(LinkHeader linkHeader) {
		super(linkHeader);


		lblInputPower = new JLabel(":");
		lblInputPower.setName("Input Power");
		lblInputPower.setHorizontalAlignment(SwingConstants.RIGHT);
		lblInputPower.setForeground(Color.WHITE);
		lblInputPower.setBounds(99, 22, 100, 17);
		add(lblInputPower);

		lblInputPowerTxt = new JLabel();
		lblInputPowerTxt.setName("");
		lblInputPowerTxt.setHorizontalAlignment(SwingConstants.RIGHT);
		lblInputPowerTxt.setForeground(new Color(153, 255, 255));
		lblInputPowerTxt.setBounds(2, 22, 104, 17);
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
		new SwingWorker<Font, Void>() {
			@Override
			protected Font doInBackground() throws Exception {
				Thread.currentThread().setName("MonitorPanel.leds.setFont");
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
				Thread.currentThread().setName("MonitorPanel.ledLock.setBounds");
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
		super.refresh();
		logger.exit();
	}
}
