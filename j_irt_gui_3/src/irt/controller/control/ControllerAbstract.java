package irt.controller.control;

import java.awt.Component;
import java.util.Objects;
import java.util.Observable;

import javax.swing.JPanel;
import javax.swing.event.EventListenerList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiControllerAbstract;
import irt.controller.serial_port.value.setter.SetterAbstract;
import irt.data.FireValue;
import irt.data.MyThreadFactory;
import irt.data.PacketWork;
import irt.data.RundomNumber;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.PacketListener;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.Packet;

public abstract class ControllerAbstract implements Runnable{

	protected final Logger logger = LogManager.getLogger(getClass().getName());

	public enum Style{
		CHECK_ONCE,
		CHECK_ALWAYS
	}
	private PacketWork packetWork;
	protected PacketListener packetListener;
	protected ValueChangeListener valueChangeListener;

	protected volatile boolean run = true;
	protected volatile boolean send = true;

	protected Style style;

	private int waitTime = 3000;
	private JPanel owner;

	protected Observable observable;
	private String name;
	protected int deviceType;

	public ControllerAbstract(int deviceType, String controllerName, PacketWork packetWork, JPanel panel, Style style) {
		Objects.requireNonNull(packetWork);

		this.packetWork = packetWork;
		this.style = style;
		this.deviceType = deviceType;
		setName(controllerName);
 		setListeners();

 		valueChangeListener = addGetterValueChangeListener();
		if(valueChangeListener!=null && packetWork!=null){
			packetWork.addVlueChangeListener(valueChangeListener);
		}
		packetListener = getNewPacketListener();

		if(panel!=null){
			setComponents(panel);
			owner = panel;
		}
	}

	protected abstract void 				setListeners();
	protected abstract boolean 				setComponent(Component component);
	protected abstract ValueChangeListener 	addGetterValueChangeListener();

	private void setComponents(JPanel panel) {
		Component[] cs = panel.getComponents();
		if(cs!=null)
			for(Component c:cs)
				if(c.getName()!=null)
					setComponent(c);
	}

	@Override
	public void run() {
		logger.entry(run, packetListener);

		if(packetWork!=null){

			if(packetListener!=null)
				GuiControllerAbstract.getComPortThreadQueue().addPacketListener(packetListener);

			while(run){
				synchronized (this) {
					try {


						if(send){
							sendPacketWorker();
							if(isWait())
								wait(waitTime);
						}else
							wait();

					} catch (Exception e) {
						logger.catching(e);
					}
				}
			}
			logger.trace("{} is stopped", ControllerAbstract.this.getClass().getSimpleName());

			if(packetListener!=null)
				GuiControllerAbstract.getComPortThreadQueue().removePacketListener(packetListener);
			clear();
		}

		logger.exit();
	}

	protected boolean isWait() {
		return true;
	}

	protected void sendPacketWorker() {
		GuiControllerAbstract.getComPortThreadQueue().add(packetWork);
	}

	protected void clear(){
		packetWork.removeVlueChangeListeners();
		packetWork.clear();
		packetWork = null;
	}

	protected boolean setPacketWork(Packet packet) {
		return packetWork!=null ? packetWork.set(packet) : false;
	}

	protected void setPacketWork(PacketWork packetWork) {
		Objects.requireNonNull(packetWork);

		if(this.packetWork!=null)
			this.packetWork.removeVlueChangeListener(valueChangeListener);
		this.packetWork = packetWork;
	}

	public PacketWork getPacketWork() {
		return packetWork;
	}

	protected PacketListener getNewPacketListener() {
		return new PacketListener() {

			@Override
			public void packetRecived(Packet packet) {
				if (setPacketWork(packet) && getPacketWork() instanceof SetterAbstract && style == Style.CHECK_ONCE)
					setSend(false);
			}
		};
	}

	public void stop() {
		this.run = false;
		final ControllerAbstract controller = this;

		new MyThreadFactory().newThread(new Runnable() {
			
			@Override
			public void run() {
				synchronized (controller) {
					controller.notify();
				}
			}
		}).start();
	}

	public synchronized boolean isSend() {
		return send;
	}

	public synchronized void setSend(boolean send) {
		setSend(send, send);
	}

	public synchronized void setSend(boolean send, boolean isNotify) {

		this.send = send;

		if(isNotify || send)
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

	public void setObservable(Observable observable) {
		this.observable = observable;
	}

	public JPanel getOwner() {
		return owner;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

		Thread t = new Thread(new FireValue(statusChangeListeners, valueChangeEvent), ControllerAbstract.this.getName()+".FireValue-"+new RundomNumber().toString());
		int priority = t.getPriority();
		if(priority>Thread.MIN_PRIORITY)
			t.setPriority(priority-1);
		t.setDaemon(true);
		t.start();
	}
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	@Override
	public String toString() {
		return "\n\t" + getClass().getSimpleName()
				+ " [name=" + name + ", packetWork=" + packetWork + ", run=" + run + ", send=" + send + ", style=" + style + ", waitTime=" + waitTime
				+ "]";
	}
}
