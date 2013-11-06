package irt.controller;

import irt.controller.control.ControllerAbstract;
import irt.controller.serial_port.value.getter.GetterAbstract;
import irt.controller.serial_port.value.seter.Setter;
import irt.data.PacketWork;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;

import java.awt.Component;

import javax.swing.JOptionPane;

public class StoreConfigController extends ControllerAbstract {

	private Component owner;

	public StoreConfigController(LinkHeader linkHeader, Component owner, Style style) {
		super(new Setter(linkHeader, Packet.IRT_SLCP_PACKET_ID_CONFIG_PROFILE, Packet.IRT_SLCP_PACKET_ID_CONFIG_PROFILE_SAVE, PacketWork.PACKET_ID_STORE_CONFIG), null, style);
		setSend(false);
		getPacketWork().getPacketThread().setDataPacketTypeCommand();

		this.owner = owner;

		Thread t = new Thread(this);
		int priority = t.getPriority();
		if(priority>Thread.MIN_PRIORITY)
			t.setPriority(priority-1);
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
							setRun(false);
							JOptionPane.showMessageDialog( null, "The Configuration has been stored.");
					}else{

						if(count<3)
							setSend(true);
						else{
							if(JOptionPane.showConfirmDialog( null, "Could not stor the configuration. Try one more time?", "Store Config", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){
								setSend(true);
							}else
								setRun(false);
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
		if(JOptionPane.showConfirmDialog( owner, "Do you want to store the configuration?", "Store Config", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){
			setSend(true);
			super.run();
		}else
			clear();
	}

}
