package irt.tools.panel.head;

import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.tools.panel.DevicePanel;

import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Arrays;

import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

@SuppressWarnings("serial")
public class UnitsContainer extends JPanel{

	private final Logger logger = (Logger) LogManager.getLogger();

	private int shift;
	private final int SPACE = 5;

	private static ValueChangeListener statusChangeListener;
	private static ComponentListener componentListener;

	public UnitsContainer(){
		setOpaque(false);
		setLayout(null);
		componentListener = new ComponentListener() {
			
			@Override public void componentShown(ComponentEvent arg0) { }
			@Override public void componentMoved(ComponentEvent arg0) { } 
			@Override public void componentHidden(ComponentEvent arg0) { }
			@Override public void componentResized(ComponentEvent componentEvent) {
				setLocation((Panel)componentEvent.getSource());
			}
		};
	}

	public boolean contains(LinkHeader linkHeader){
		logger.entry(linkHeader);

		boolean result = false;

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
		return logger.exit(result);
	}

	public boolean contains(Component component) {
		return Arrays.asList(getComponents()).contains(component);
	}

	@Override
	public synchronized Component add(Component unitPanel) {

		if(contains(unitPanel)){
			DevicePanel dp = (DevicePanel)getComponent(unitPanel.getClass());
			if(dp!=null)
				if(dp.getVarticalLabel().getText().equals(((DevicePanel)unitPanel).getVarticalLabel().getText()))
					unitPanel = dp;
				else{
					remove(unitPanel);
					unitPanel.setLocation(shift, 0);
					add((Panel)unitPanel);
				}
		}else if(unitPanel instanceof Panel)
				add((Panel)unitPanel);

		return unitPanel;
	}

	private void add(Panel unitPanel) {

		setThinSize();
		unitPanel.setLocation(shift, 0);
		super.add(unitPanel);
		if(unitPanel instanceof DevicePanel && statusChangeListener!=null)
			((DevicePanel)unitPanel).addStatusChangeListener(statusChangeListener);
		unitPanel.addComponentListener(componentListener);
	}

	private void setLocation(Panel source) {
		int width = (int) Math.round(source.getSize().getWidth());

		shift = 0;
		Component[] cs = getComponents();
		Arrays.sort(cs);
		for(Component c:cs)
			if(c instanceof Panel){
				Panel p = (Panel)c;
				ComponentListener[] cls = removeComponentListeners(p);
				
				if(p!=source){
					if(width>=p.MAX_WIDTH)
						p.setThinSize();
					else if(width>=p.MID_WIDTH)
						p.setMidSize();
					else
						p.setMaxSize();
				}

				p.setLocation(shift, 0);
				addComponentListeners(p, cls);
				shift += SPACE+p.getWidth();
			}
	} 

	private void addComponentListeners(Panel p, ComponentListener[] cls) {
		if(p!=null && cls!=null)
			for(ComponentListener cl:cls)
				p.addComponentListener(cl);
	}

	private ComponentListener[] removeComponentListeners(Panel panel) {
		ComponentListener[] cls = panel.getComponentListeners();
		for(ComponentListener cl:cls)
			panel.removeComponentListener(cl);
		return cls;
	}

	private void setThinSize() {

		shift = 0;
		Component[] cs = getComponents();
		for(Component c:cs)
			if(c instanceof Panel){
				Panel p = (Panel)c;
				ComponentListener[] cls = removeComponentListeners(p);
				p.setThinSize();
				p.setLocation(shift, 0);
				addComponentListeners(p, cls);
				shift += SPACE+p.getWidth();
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

		boolean removed = false;
		Component[] components = getComponents();

		if(components!=null)
			for(Component c:components){
				if(isDevicePanel(linkHeader, c)){
					super.remove(c);
					removed = true;
					break;
				}
			}

		return removed;
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
		Component[] cs = getComponents();
		for(Component c:cs){
			if(c.getClass().getSimpleName().equals(className)){
				super.remove(c);
				break;
			}
		}
	}

	public void addStatusListener(ValueChangeListener valueChangeListener) {
		statusChangeListener = valueChangeListener;
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
			isDevicePanel = (linkHeader.equals(lh));
		}

		return isDevicePanel;
	}
}