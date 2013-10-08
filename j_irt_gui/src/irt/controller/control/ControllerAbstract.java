package irt.controller.control;

import irt.controller.GuiControllerAbstract;
import irt.controller.serial_port.value.seter.SetterAbstract;
import irt.data.FireValue;
import irt.data.PacketWork;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.PacketListener;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.Packet;

import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.event.EventListenerList;

public abstract class ControllerAbstract implements Runnable{

	public enum Style{
		CHECK_ONCE,
		CHECK_ALWAYS
	}
	private PacketWork packetWork;
	private PacketListener packetListener;
	private ValueChangeListener valueChangeListener;

	protected volatile boolean run = true;
	protected volatile boolean send = true;

	protected Style style;

	private int waitTime = 3000;
	private JPanel owner;

	public ControllerAbstract(PacketWork packetWork, Style style) {
		this.packetWork = packetWork;
		this.style = style;
 		setListeners();

 		valueChangeListener = addGetterValueChangeListener();
		if(valueChangeListener!=null && packetWork!=null)
			packetWork.addVlueChangeListener(valueChangeListener);
		packetListener = getNewPacketListener();
	}

	protected abstract void setListeners();
	protected abstract ValueChangeListener addGetterValueChangeListener();

	public ControllerAbstract(PacketWork packetWork, JPanel panel, Style stile) {
		this(packetWork, stile);
		if(panel!=null){
			setComponents(panel);
			owner = panel;
		}
	}

	private void setComponents(JPanel panel) {
		Component[] cs = panel.getComponents();
		if(cs!=null)
			for(Component c:cs)
				if(c.getName()!=null)
					setComponent(c);
	}

	protected abstract boolean setComponent(Component component);

	@Override
	public void run() {
		if(packetListener!=null && packetWork!=null){

			GuiControllerAbstract.getComPortThreadQueue().addPacketListener(packetListener);

			while(run){
				synchronized (this) {
					try {


						if(send){ send(); }
						if(isWait()) wait(waitTime);
					} catch (InterruptedException e) {
					}
				}
			}
			GuiControllerAbstract.getComPortThreadQueue().removePacketListener(packetListener);
			clear();
		}
	}

	protected boolean isWait() {
		return true;
	}

	protected void send() {
		GuiControllerAbstract.getComPortThreadQueue().add(packetWork);
	}

	protected void clear(){
		packetWork.removeVlueChangeListener(valueChangeListener);
		packetWork.clear();
		packetWork = null;
	}

	protected boolean setPacketWork(Packet packet) {
		return packetWork!=null ? packetWork.set(packet) : false;
	}

	protected void setPacketWork(PacketWork packetWork) {
		if(this.packetWork!=null)
			this.packetWork.removeVlueChangeListener(valueChangeListener);
		this.packetWork = packetWork;
		this.packetWork.removeVlueChangeListener(valueChangeListener);
	}

	public PacketWork getPacketWork() {
		return packetWork;
	}

	protected PacketListener getNewPacketListener() {
		return new PacketListener() {

			@Override
			public void packetRecived(Packet packet) {
				if(setPacketWork(packet) && getPacketWork() instanceof SetterAbstract && style==Style.CHECK_ONCE)
					setSend(false);
			}
		};
	}

	public synchronized void setRun(boolean run) {
		this.run = run;
		notify();
	}

	public synchronized boolean isSend() {
		return send;
	}

	public synchronized void setSend(boolean send) {
		setSend(send, send);
	}

	public synchronized void setSend(boolean send, boolean isNotify) {

		this.send = send;

		if(isNotify)
			notify();
	}

	public synchronized boolean isRun() {
		return run;
	}

	public int getWaitTime() {
		return waitTime;
	}

	public void setWaitTime(int waitTime) {

		if(waitTime>0)
			this.waitTime = waitTime;

		synchronized (this) {
			notify();
		}
	}

	@Override
	protected void finalize() throws Throwable {
//		System.out.println("+++++++++++++++++++++++++++++++ - finalize - "+getClass().getSimpleName()+" - ++++++++++++++++++++++++++++++++++++++");
		super.finalize();
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private EventListenerList statusChangeListeners = new EventListenerList();

	public void addStatusListener(ValueChangeListener valueChangeListener) {
		statusChangeListeners.add(ValueChangeListener.class, valueChangeListener);
	}

	public void removeStatusChangeListener(ValueChangeListener valueChangeListener) {
		statusChangeListeners.remove(ValueChangeListener.class, valueChangeListener);
	}

	protected void fireStatusChangeListener(ValueChangeEvent valueChangeEvent) {

		Thread t = new Thread(new FireValue(statusChangeListeners, valueChangeEvent), ControllerAbstract.this.getClass().getSimpleName());
		t.setPriority(t.getPriority()-1);
		t.start();
	}
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public JPanel getOwner() {
		return owner;
	}
}
