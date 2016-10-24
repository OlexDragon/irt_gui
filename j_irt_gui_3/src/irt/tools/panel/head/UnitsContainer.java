package irt.tools.panel.head;

import irt.data.packet.LinkHeader;
import irt.tools.panel.DevicePanel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Arrays;

import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

@SuppressWarnings("serial")
public class UnitsContainer extends JPanel{

	private final Logger logger = (Logger) LogManager.getLogger();

	private final int SPACE = 5;

	private static ComponentListener componentListener;

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
	}

	public boolean contains(LinkHeader linkHeader){
		logger.entry(linkHeader);

		boolean result = false;

		synchronized (UnitsContainer.class) {
			for (Component c : getComponents()) {
				if (c instanceof DevicePanel) {
					LinkHeader lh = ((DevicePanel) c).getLinkHeader();
					if (linkHeader == null) {
						if (lh == null) {
							result = true;
							break;
						}
					} else if (linkHeader.equals(lh)) {
						result = true;
						break;
					}
				}
			}
		}
		return logger.exit(result);
	}

	public boolean contains(Component component) {
		return Arrays.asList(getComponents()).contains(component);
	}

	@Override
	public Component add(Component unitPanel) {

		synchronized (UnitsContainer.class) {
			if(contains(unitPanel)){
				DevicePanel dp = (DevicePanel)getComponent(unitPanel.getClass());
				if(dp!=null)
					if(dp.getVarticalLabel().getText().equals(((DevicePanel)unitPanel).getVarticalLabel().getText()))
						unitPanel = dp;
					else{
						remove(unitPanel);
						add((Panel)unitPanel);
					}
			}else if(unitPanel instanceof Panel)
					add((Panel)unitPanel);
		}

		return unitPanel;
	}

	private void add(Panel panel) {

		synchronized (UnitsContainer.class) {
			super.add(panel);
			setLocations();
			panel.addComponentListener(componentListener);
		}
	}

	private void setLocations() {

		synchronized (UnitsContainer.class) {
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
	}

	public <T> Component getComponent(Class<T> instance) {

		Component[] cs = getComponents();
		Component component = null;

		if(cs!=null)
			for(Component c:cs)
				if(instance.isInstance(c)){
					component = c;
					break;
				}

		return component;
	}

	public boolean remove(LinkHeader linkHeader) {
		logger.entry(linkHeader);

		boolean removed = false;

		synchronized (UnitsContainer.class) {
			Component[] components = getComponents();

			if(components!=null)
				for(Component c:components){
					if(isDevicePanel(linkHeader, c)){
						super.remove(c);
						removed = true;
						break;
					}
				}
		}

		return logger.exit(removed);
	}

	public <T> void remove(Class<T> classToRemove) {
		Component[] cs = getComponents();
		for(Component c:cs){
			if(classToRemove.isInstance(c)){
				super.remove(c);
				break;
			}
		}
	}

	public void remove(String className) {
		synchronized (UnitsContainer.class) {
			Component[] cs = getComponents();
			for(Component c:cs){ //TODO stack trace
				if(c.getClass().getSimpleName().equals(className)){
					super.remove(c);
					break;
				}
			}
		}
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
		logger.entry(linkHeader, component);
		boolean isDevicePanel = false;

		if(component instanceof DevicePanel){
			DevicePanel devicePanel = (DevicePanel)component;
			LinkHeader lh = devicePanel.getLinkHeader();
			logger.trace("LinkHeader lh = {}", lh);
			isDevicePanel = linkHeader!=null ? linkHeader.equals(lh) : lh==null;
		}

		return isDevicePanel;
	}
}