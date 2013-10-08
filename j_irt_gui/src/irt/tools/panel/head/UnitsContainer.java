package irt.tools.panel.head;

import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.tools.panel.DevicePanel;

import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class UnitsContainer extends JPanel{

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

	public boolean contains(Component component) {
		return Arrays.asList(getComponents()).contains(component);
	}

	@Override
	public Component add(Component unitPanel) {

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

		shift = 0;
		Component[] cs = getComponents();
		for(Component c:cs)
			if(c instanceof Panel){
				Panel p = (Panel)c;
				ComponentListener[] cls = removeComponentListeners(p);
				
				if(p!=source){
					p.setThinSize();
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

		boolean removed;
		List<Component> components = Arrays.asList(getComponents());
		Component componentToReamove = null;

		if(components!=null)
			for(Component c:components){
				if(c instanceof DevicePanel){
					DevicePanel devicePanel = (DevicePanel)c;

					if((linkHeader==null && devicePanel.getLinkHeader()==null) || (linkHeader!=null && devicePanel.getLinkHeader()!=null && devicePanel.getLinkHeader().getAddr()==linkHeader.getAddr())){
						componentToReamove = c;
					break;
					}
				}
			}

		if(componentToReamove!=null){
			super.remove(componentToReamove);
			removed = true;
		}else
			removed = false;

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
		// TODO Auto-generated method stub
		
	}
}