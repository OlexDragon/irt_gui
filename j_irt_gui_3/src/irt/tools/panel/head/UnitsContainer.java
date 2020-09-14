package irt.tools.panel.head;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiControllerAbstract;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.controller.serial_port.SerialPortInterface;
import irt.data.ThreadWorker;
import irt.data.packet.LinkHeader;
import irt.data.packet.PacketWork;
import irt.data.packet.alarm.AlarmsSummaryPacket;
import irt.data.packet.interfaces.LinkedPacket;
import irt.tools.panel.DevicePanel;

@SuppressWarnings("serial")
public class UnitsContainer extends JPanel implements Runnable{

	private final Logger logger = LogManager.getLogger();

	private final int SPACE = 5;

	private static ComponentListener componentListener;

	private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(new ThreadWorker("UnitsContainer"));
	private final List<PacketWork> packets = new ArrayList<>();
	private int index;

	public UnitsContainer(){
		setBorder(null);
		setOpaque(false);
		setLayout(null);
		componentListener = new ComponentListener() {
			
			@Override public void componentShown(ComponentEvent arg0) { }
			@Override public void componentMoved(ComponentEvent arg0) { }
			@Override public void componentHidden(ComponentEvent arg0) { }
			@Override public void componentResized(ComponentEvent componentEvent) {
				setLocations();
			}
		};

		service.scheduleAtFixedRate(this, 3, 3, TimeUnit.SECONDS);
	}

//	public boolean contains(LinkHeader linkHeader){
//		logger.entry(linkHeader);
//
//		Stream<LinkHeader> map = Optional.ofNullable(getComponents()).map(Arrays::stream).orElse(Stream.empty()).filter(DevicePanel.class::isInstance).map(DevicePanel.class::cast).map(DevicePanel::getLinkHeader);
//		logger.error(map);
//
//		boolean result = false;
//
//		synchronized (UnitsContainer.class) {
//			for (Component c : getComponents()) {
//				if (c instanceof DevicePanel) {
//					LinkHeader lh = ((DevicePanel) c).getLinkHeader();
//					if (linkHeader == null) {
//						if (lh == null) {
//							result = true;
//							break;
//						}
//					} else if (linkHeader.equals(lh)) {
//						result = true;
//						break;
//					}
//				}
//			}
//		}
//		return logger.traceExit(result);
//	}

	public boolean contains(Component component) {
		return Arrays.asList(getComponents()).contains(component);
	}

	@Override
	public Component add(Component unitPanel) {
		logger.trace(unitPanel);

			if(contains(unitPanel)){
				DevicePanel dp = (DevicePanel)getComponent(unitPanel.getClass());
				if(dp!=null)
					if(dp.getVarticalLabel().getText().equals(((DevicePanel)unitPanel).getVarticalLabel().getText()))
						unitPanel = dp;
					else{
						remove(unitPanel);
						add((Panel)unitPanel);
					}

			}else if(unitPanel instanceof Panel) {
				add((Panel)unitPanel);
				Optional.of(unitPanel).filter(DevicePanel.class::isInstance).map(DevicePanel.class::cast).map(DevicePanel::getLinkHeader).map(LinkHeader::getAddr).ifPresent(
						linkAddr->{
							if(packets.stream().map(LinkedPacket.class::cast).map(LinkedPacket::getLinkHeader).map(LinkHeader::getAddr).filter(addr->addr==linkAddr).findAny().isPresent())
								return;
							packets.add(new AlarmsSummaryPacket(linkAddr));
						});
			}

		return unitPanel;
	}

	private synchronized void add(Panel panel) {

			super.add(panel);
			setLocations();
			panel.addComponentListener(componentListener);
	}

	private void setLocations() {

			int x = 0,  height = 0;
			Dimension containerPreferredSize = getPreferredSize();

			Component[] components = getComponents();
			Arrays.sort(components);

			for(Component c:components){
				Dimension preferredSize = c.getPreferredSize(); //TODO stack trace
				c.setLocation(x, 0);
				x += SPACE + preferredSize.width;
				if(height<preferredSize.height)
					height = preferredSize.height;
			}

				containerPreferredSize.height = height;
				containerPreferredSize.width = x;
				setPreferredSize(containerPreferredSize);
				setSize(containerPreferredSize);
	}

	public <T> Component getComponent(Class<T> instance) {
		return Optional.ofNullable(getComponents()).map(Arrays::stream).orElse(Stream.empty()).filter(instance::isInstance).findAny().orElse(null);
	}

	public synchronized boolean remove(LinkHeader linkHeader) {
		logger.trace(linkHeader);

		Optional<Component> oComponent = Optional.ofNullable(getComponents()).map(Arrays::stream).orElse(Stream.empty()).filter(c->isDevicePanel(linkHeader, c)).findAny();
		oComponent.ifPresent(super::remove);
		packets.stream().map(LinkedPacket.class::cast).filter(lp->lp.getLinkHeader().equals(linkHeader)).findAny().map(Object.class::cast).ifPresent(packets::remove);

		// true if removed
		return oComponent.isPresent();
	}

	public synchronized <T> void remove(Class<T> classToRemove) {
		Optional.ofNullable(getComponents()).map(Arrays::stream).orElse(Stream.empty()).filter(classToRemove::isInstance).forEach(super::remove);
	}

	public void remove(String className) {
		try { remove(Class.forName(className)); } catch (ClassNotFoundException e) { logger.catching(e); }
	}

	@Override
	public void removeAll() {
		super.removeAll();
	}

	public void refresh() {
		try{
		for(Component c:getComponents()){
			if(c instanceof Panel)
				((Panel)c).refresh();
		}
		}catch(Exception e){
			logger.catching(e);
		}
	}

	public DevicePanel getDevicePanel(LinkHeader linkHeader) {

		DevicePanel devicePanel = null;
		Component[] components = getComponents();

		if(components!=null)
			for(Component c:components){
				if(isDevicePanel(linkHeader, c)){
					devicePanel = (DevicePanel) c;
					break;
				}
			}

		return devicePanel;
	}

	private boolean isDevicePanel(LinkHeader linkHeader, Component component){
		boolean isDevicePanel = false;

		if(component instanceof DevicePanel){
			DevicePanel devicePanel = (DevicePanel)component;
			LinkHeader lh = devicePanel.getLinkHeader();
			logger.trace("LinkHeader lh = {}", lh);
			isDevicePanel = linkHeader!=null ? linkHeader.equals(lh) : lh==null;
		}

		return isDevicePanel;
	}

	@Override
	public void run() {

		if(packets.isEmpty())
			return;

		if(index>=packets.size())
			index = 0;

//		logger.error(packets);
		Optional.ofNullable(ComPortThreadQueue.getSerialPort()).filter(SerialPortInterface::isOpened).ifPresent(sp->GuiControllerAbstract.getComPortThreadQueue().add(packets.get(index++)));
	}
}