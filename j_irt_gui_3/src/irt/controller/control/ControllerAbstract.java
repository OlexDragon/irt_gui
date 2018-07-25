package irt.controller.control;

import java.awt.Component;
import java.util.Objects;
import java.util.Observable;
import java.util.Optional;

import javax.swing.JPanel;
import javax.swing.event.EventListenerList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiControllerAbstract;
import irt.controller.serial_port.value.setter.SetterAbstract;
import irt.data.DeviceInfo.DeviceType;
import irt.data.FireValue;
import irt.data.MyThreadFactory;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.PacketWork;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.interfaces.PacketThreadWorker;

public abstract class ControllerAbstract implements UnitController{

	protected final Logger logger = LogManager.getLogger(getClass().getName());

	public enum Style{
		CHECK_ONCE,
		CHECK_ALWAYS
	}
	private PacketWork packetWork;
	protected ValueChangeListener valueChangeListener;

	protected volatile boolean run = true;
	protected volatile boolean send = true;

	protected Style style;

	private int waitTime = 3000;
	private JPanel owner;

	protected Observable observable;
	private String name;
	protected Optional<DeviceType> deviceType;

	public ControllerAbstract(Optional<DeviceType> deviceType, String controllerName, PacketWork packetWork, JPanel panel, Style style) {
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

		if(packetWork==null)
			return;

		try{

			GuiControllerAbstract.getComPortThreadQueue().addPacketListener(this);

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

			GuiControllerAbstract.getComPortThreadQueue().removePacketListener(this);
			clear();

		}catch (Exception e) {
			logger.catching(e);
		}
	}

	protected boolean isWait() {
		return true;
	}

	protected void sendPacketWorker() {
		Optional
		.ofNullable(packetWork)
		.map(PacketWork::getPacketThread)
		.map(
				pt->{
					try {
						pt.join();
					} catch (InterruptedException e) {
						logger.catching(e);
					}
					return pt; })
		.map(PacketThreadWorker::getPacket)
		.map(p->packetWork)
		.ifPresent(GuiControllerAbstract.getComPortThreadQueue()::add);
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

	@Override
	public void start() {
		
	}

	@Override
	public void stop() {

		this.run = false;

		GuiControllerAbstract.getComPortThreadQueue().removePacketListener(this);

		final ControllerAbstract controller = this;

		new MyThreadFactory(()->{
				try{
					synchronized (controller) {
						controller.notify();
					}
				}catch (Exception e) {
					logger.catching(e);
				}
			}, getClass().getSimpleName() + ".stop()");
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
		super.finalize();
	}

	@Override
	public void onPacketReceived(Packet packet) {
		if (setPacketWork(packet) && getPacketWork() instanceof SetterAbstract && style == Style.CHECK_ONCE)
			setSend(false);
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
		new MyThreadFactory(()->new FireValue(statusChangeListeners, valueChangeEvent), "ControllerAbstract.fireStatusChangeListener()");
	}
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	@Override
	public String toString() {
		return "\n\t" + getClass().getSimpleName()
				+ " [name=" + name + ", packetWork=" + packetWork + ", run=" + run + ", send=" + send + ", style=" + style + ", waitTime=" + waitTime
				+ "]";
	}
}
