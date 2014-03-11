package irt.controller;

import irt.controller.control.ControllerAbstract;
import irt.data.PacketWork;
import irt.data.RegisterValue;
import irt.data.RundomNumber;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextField;
import javax.swing.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class DeviceDebagSaveController extends ControllerAbstract {

	private final Logger logger = (Logger) LogManager.getLogger();

	private JTextField txtField;
	private Timer timer;

	private ActionListener timerListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			stop();
			logger.trace("Timer.actionPerformed, this={}", this);
		}
	};

	public DeviceDebagSaveController(JTextField txtField, PacketWork packetWork, Style stile) {
		super("DeviceDebagSaveController", packetWork, null, stile);
		this.txtField = txtField;

		timer = new Timer(9000, timerListener);
		timer.start();

		Thread t = new Thread(this, "DeviceDebagSaveController-"+new RundomNumber().toString());
		int priority = t.getPriority();
		if(priority>Thread.MIN_PRIORITY)
			t.setPriority(priority-1);
		t.setDaemon(true);
		t.start();
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return new ValueChangeListener() {

			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				logger.trace("DeviceDebagSaveController.valueChanged: {}", valueChangeEvent);
				String str = txtField.getText();
				if (valueChangeEvent.getSource() instanceof RegisterValue){
					logger.trace("Saved");
					txtField.setText("Saved");
				}else{
					logger.trace("ERROR");
					txtField.setText("Error");
				}

				synchronized (this) { try { wait(500); } catch (InterruptedException e) { logger.catching(e); } }
				txtField.setText(str);

				stop();
			}
		};
	}

	@Override protected void setListeners() { }
	@Override protected boolean setComponent(Component component) { return false; }

	@Override
	protected void clear() {
		super.clear();
		timer.stop();
		timer.removeActionListener(timerListener);
		timer = null;
	}
}
