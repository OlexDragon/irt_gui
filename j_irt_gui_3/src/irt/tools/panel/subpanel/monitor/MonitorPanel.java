package irt.tools.panel.subpanel.monitor;
import irt.controller.control.ControllerAbstract;
import irt.controller.monitor.MonitorController;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;


@SuppressWarnings("serial")
public class MonitorPanel extends MonitorPanelAbstract {

	private final Logger logger = (Logger) LogManager.getLogger();

	private LED ledLock;
	private LED ledMute;
	private JLabel lblInputPowerTxt;
	private JLabel lblOutputPowerTxt;
	private JLabel lblTemperatureTxt;
	private JLabel lblInputPower;
	private JLabel lblOutputPower;
	private JLabel lblTemperature;

	public MonitorPanel(LinkHeader linkHeader) {
		super(linkHeader, Translation.getValue(String.class, "monitor", "Monitor"), 214, 210);


		ledLock = new LED(Color.GREEN, Translation.getValue(String.class, "lock", "LOCK"));
		ledLock.setName("Lock");
		ledLock.setForeground(Color.GREEN);
		add(ledLock);
		
		ledMute = new LED(Color.YELLOW, Translation.getValue(String.class, "mute", "MUTE"));
		ledMute.setName("Mute");
		ledMute.setForeground(Color.GREEN);

		add(ledMute);

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

		lblOutputPower = new JLabel(":");
		lblOutputPower.setName("Output Power");
		lblOutputPower.setHorizontalAlignment(SwingConstants.RIGHT);
		lblOutputPower.setForeground(Color.WHITE);
		lblOutputPower.setBounds(99, 50, 100, 17);
		add(lblOutputPower);

		lblOutputPowerTxt = new JLabel();
		lblOutputPowerTxt.setName("");
		lblOutputPowerTxt.setHorizontalAlignment(SwingConstants.RIGHT);
		lblOutputPowerTxt.setForeground(new Color(153, 255, 255));
		lblOutputPowerTxt.setBounds(2, 50, 104, 17);
		add(lblOutputPowerTxt);
		new TextWorker(lblOutputPowerTxt, "output_power", "Output Power").execute();

		lblTemperature = new JLabel(":");
		lblTemperature.setName("Temperature");
		lblTemperature.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTemperature.setForeground(Color.WHITE);
		lblTemperature.setBounds(99, 78, 100, 17);
		add(lblTemperature);

		lblTemperatureTxt = new JLabel();
		lblTemperatureTxt.setName("");
		lblTemperatureTxt.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTemperatureTxt.setForeground(new Color(153, 255, 255));
		lblTemperatureTxt.setBounds(2, 78, 104, 17);
		add(lblTemperatureTxt);
		new TextWorker(lblTemperatureTxt, "temperature", "Temperature").execute();

		swingWorkers();
	}

	private void swingWorkers() {
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
					Font font = get();
					ledLock.setFont(font);
					ledMute.setFont(font);
				} catch (InterruptedException | ExecutionException e) {
					logger.catching(e);
				}
			}
		}.execute();

		new SwingWorker<Font, Void>() {
			@Override
			protected Font doInBackground() throws Exception {
				Thread.currentThread().setName("MonitorPanel.labels.setFont");
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
				lblOutputPower.setFont(font);
				lblOutputPowerTxt.setFont(font);
				lblTemperature.setFont(font);
				lblTemperatureTxt.setFont(font);
				}catch(InterruptedException | ExecutionException e){
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

		new SwingWorker<Rectangle, Void>() {

			@Override
			protected Rectangle doInBackground() throws Exception {
				Thread.currentThread().setName("MonitorPanel.ledMute.setBounds");
				return new Rectangle(Translation.getValue(Integer.class, "monitor.led.mute.x", 17)
						, Translation.getValue(Integer.class, "monitor.led.mute.y", 138)
						, Translation.getValue(Integer.class, "monitor.led.mute.width", 100)
						, 28);
			}

			@Override
			protected void done() {
				try {
					ledMute.setBounds(get());
				} catch (InterruptedException | ExecutionException e) {
					logger.catching(e);
				}
			}
		}.execute();

	}

	@Override
	protected ControllerAbstract getNewController() {
		return new MonitorController(getLinkHeader(), this) ;
	}

	@Override
	public void refresh() {
		super.refresh();
		logger.entry();

//		new SwingWorker<String, Void>() {
//			@Override
//			protected String doInBackground() throws Exception {
//				Thread.currentThread().setName("MonitorPanel.titledBorder.setTitle");
//				return logger.exit(Translation.getValue(String.class, "monitor", "Monitor"));
//			}
//			@Override
//			protected void done() {
//				try {
//					String title = get();
//					logger.trace("title={}", title);
//					titledBorder.setTitle(title);
//				} catch (InterruptedException | ExecutionException e) {
//					logger.catching(e);
//				}
//			}
//		}.execute();
		titledBorder.setTitle(Translation.getValue(String.class, "monitor", "Monitor"));

		new TextWorker(ledMute, "mute", "MUTE").execute();
		new TextWorker(ledLock, "lock", "LOCK").execute();
		new TextWorker(lblInputPowerTxt, "input_power", "Input Power").execute();
		new TextWorker(lblOutputPowerTxt, "output_power", "Output Power").execute();
		new TextWorker(lblTemperatureTxt, "temperature", "Temperature").execute();

		swingWorkers();

		logger.exit();
	}

	//***********************************************************************************
	private class TextWorker extends SwingWorker<String, Void>{
		JLabel label;
		String key;
		String defaultValue;

		public TextWorker(JLabel label, String key, String defaultValue) {
			this.label = label;
			this.key = key;
			this.defaultValue = defaultValue;
		}

		@Override
		protected String doInBackground() throws Exception {
			Thread.currentThread().setName("MonitorPanel."+key+".setText");
			return Translation.getValue(String.class, key, defaultValue);
		}

		@Override
		protected void done() {
			try {
				label.setText(get());
			} catch (InterruptedException | ExecutionException e) {
				logger.catching(e);
			}
		}
		
	}
}
