package irt.controller;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Optional;

import javax.swing.JCheckBox;

import irt.controller.control.ControllerAbstract;
import irt.controller.serial_port.value.getter.GetterAbstract;
import irt.controller.serial_port.value.setter.Setter;
import irt.controller.serial_port.value.setter.SetterAbstract;
import irt.data.DeviceInfo.DeviceType;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketWork;
import irt.data.packet.PacketWork.PacketIDs;
import irt.data.packet.interfaces.PacketThreadWorker;

public class SwitchController extends ControllerAbstract {

	private JCheckBox checkBox;
	private ActionListener actionListener;

	public SwitchController(Optional<DeviceType> deviceType, String controllerName, JCheckBox checkBox, PacketWork packetWork) {
		super(deviceType, controllerName, packetWork, null, null);
		this.checkBox = checkBox;
	}

	@Override
	protected void setListeners() {
		actionListener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					SetterAbstract as = (SetterAbstract) getPacketWork();
					PacketThreadWorker pt = as.getPacketThread();
					if (pt.getPacket() != null)
						doSwitch(as, pt.getValue());
				} catch (Exception ex) {
					logger.catching(ex);
				}
			}

			private void doSwitch(SetterAbstract as, Object value) {

				if(value!=null){
					if(value instanceof Integer)
						value = ((Integer)value) == 0 ? 1 : 0;
					else
						value = ((Byte)value) == 0 ? (byte)1 : (byte)0;

					setSend(true);

					Setter setter = new Setter(
							as.getLinkHeader(),
							PacketImp.PACKET_TYPE_COMMAND,
							as.getGroupId(),
							as.getPacketParameterHeaderCode(),
							PacketIDs.CONFIGURATION_DLRS_WGS_SWITCHOVER,
							value);
					DefaultController controller = new DefaultController(deviceType, "Calibration Mode UnitController", setter, Style.CHECK_ONCE){

						@Override
						protected ValueChangeListener addGetterValueChangeListener() {
							final DefaultController controller = this;

							return new ValueChangeListener() {

								@Override
								public void valueChanged(ValueChangeEvent valueChangeEvent) {
									controller.stop();
								}
							};
						}
					};
					Thread t = new Thread(controller);
					int priority = t.getPriority();
					if(priority>Thread.MIN_PRIORITY)
						t.setPriority(priority-1);
					t.setDaemon(true);
					t.start();
				}
			}
		};
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return new ValueChangeListener() {

			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				new ControllerWorker(valueChangeEvent);
			}
		};
	}

	@Override
	protected boolean setComponent(Component component) {
		return false;
	}

	@Override
	protected void clear() {
		super.clear();
		checkBox.removeActionListener(actionListener);
		checkBox = null;
		actionListener = null;
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
			int id = valueChangeEvent.getID();

			GetterAbstract as = (GetterAbstract)getPacketWork();
			if(id==as.getPacketId()){

				PacketThreadWorker pt = as.getPacketThread();
				pt.setValue(source);
				if(source instanceof Byte){
					checkBox.setSelected(((Byte)source)>0);
				}else
					checkBox.setSelected(((Integer)source)>0);

				if(checkBox.getActionListeners().length==0){
					checkBox.addActionListener(actionListener);
					checkBox.setEnabled(true);
				}
			}
			}catch (Exception e) {
				logger.catching(e);
			}
		}

	}
}
