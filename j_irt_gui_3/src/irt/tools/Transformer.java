package irt.tools;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JComponent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Transformer implements Runnable{
	private final Logger logger = LogManager.getLogger();

	public static final int ACTION_SHOW = 1;
	
	private JComponent component;
	private Integer height;
	private int speed;
	private int step;
	private int actions;
	private Map<JComponent, Integer> processingComponents = new HashMap<>();

	@Override
	public void run() {
		try{
		if (component != null) {
			int realWidth = component.getWidth();

			while (true) {
				processing();
				if(transform(realWidth))
					break;
			}
		}
		}catch (Exception e) {
			logger.catching(e);
		}
	}

	private void processing() {
		for(Entry<JComponent, Integer> e:processingComponents.entrySet()){
			if((e.getValue()&ACTION_SHOW)>0) {
				JComponent component = e.getKey();
				component.setVisible(!component.isVisible());
			}
		}
	}

	protected boolean transform(int realWidth) {

		if (speed == 0 || step == 0) {
			component.setSize(realWidth, height);
		}
		return true;
	}

	public void addProcessingComponent(int actions, JComponent component){
		processingComponents.put(component, actions);
	}

	public JComponent getComponent() {
		return component;
	}

	public Integer getHeight() {
		return height;
	}

	public void setComponent(JComponent component) {
		this.component = component;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	public int getSpeed() {
		return speed;
	}

	public int getStep() {
		return step;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public int getActions() {
		return actions;
	}

	public void setActions(int actions) {
		this.actions = actions;
	}
}
