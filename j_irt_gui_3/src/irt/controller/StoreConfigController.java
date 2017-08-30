package irt.controller;

import java.awt.Component;
import java.util.Optional;

import javax.swing.JOptionPane;

import irt.controller.control.ControllerAbstract;
import irt.controller.serial_port.value.getter.GetterAbstract;
import irt.controller.serial_port.value.setter.Setter;
import irt.data.DeviceInfo.DeviceType;
import irt.data.RundomNumber;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.interfaces.PacketWork;

public class StoreConfigController extends ControllerAbstract {

	private Component owner;

	public StoreConfigController(Optional<DeviceType> deviceType, LinkHeader linkHeader, Component owner, Style style) {
		super(deviceType, "Stor Config UnitController", new Setter(linkHeader, PacketImp.GROUP_ID_CONFIG_PROFILE, PacketImp.PACKET_ID_CONFIG_PROFILE_SAVE, PacketWork.PACKET_ID_STORE_CONFIG), null, style);
		setSend(false);
		getPacketWork().getPacketThread().setDataPacketTypeCommand();

		this.owner = owner;

		Thread t = new Thread(this, "StoreConfigController-"+new RundomNumber());
		int priority = t.getPriority();
		if(priority>Thread.MIN_PRIORITY)
			t.setPriority(priority-1);
		t.setDaemon(true);
		t.start();
	}

	@Override
	protected void setListeners() {
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return new ValueChangeListener() {

			private int count;

			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				Object source = valueChangeEvent.getSource();
				if(valueChangeEvent.getID()==((GetterAbstract)getPacketWork()).getPacketId())

					if(	source instanceof Boolean){

						count = 0;
							stop();
							JOptionPane.showMessageDialog( null, "The Configuration has been stored.");
					}else{

						if(count<3)
							setSend(true);
						else{
							if(JOptionPane.showConfirmDialog( null, "Could not stor the configuration. Try one more time?", "Store Config", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){
								setSend(true);
							}else
								stop();
						}

						count++;
					}
			}
		};
	}

	@Override
	protected boolean setComponent(Component component) {
		return false;
	}

	@Override
	public void run() {
		try{
		if(JOptionPane.showConfirmDialog( owner, "Do you want to store the configuration?", "Store Config", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){
			setSend(true);
			super.run();
		}else
			clear();
		}catch (Exception e) {
			logger.catching(e);
		}
	}

}
