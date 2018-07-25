package irt.controller;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Optional;

import javax.swing.JTextField;
import javax.swing.Timer;

import irt.controller.control.ControllerAbstract;
import irt.data.DeviceInfo.DeviceType;
import irt.data.MyThreadFactory;
import irt.data.RegisterValue;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.PacketWork;

public class DeviceDebagSaveController extends ControllerAbstract {

	private JTextField txtField;
	private Timer timer;

	private ActionListener timerListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				stop();
				logger.trace("Timer.actionPerformed, this={}", this);
			} catch (Exception ex) {
				logger.catching(ex);
			}
		}
	};

	public DeviceDebagSaveController(Optional<DeviceType> deviceType, JTextField txtField, PacketWork packetWork, Style stile) {
		super(deviceType, "DeviceDebagSaveController", packetWork, null, stile);
		this.txtField = txtField;

		timer = new Timer(9000, timerListener);
		timer.setRepeats(false);
		timer.start();

		new MyThreadFactory(this, "DeviceDebagSaveController");
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
