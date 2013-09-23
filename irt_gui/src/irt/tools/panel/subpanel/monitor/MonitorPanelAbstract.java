package irt.tools.panel.subpanel.monitor;

import irt.controller.control.ControllerAbstract;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

@SuppressWarnings("serial")
public abstract class MonitorPanelAbstract extends JPanel {

	protected static final Font FONT = new Font("Tahoma", Font.PLAIN, 14);

	protected ControllerAbstract controller;

	private LinkHeader linkHeader;

	private ValueChangeListener statusListener;

//	public MonitorPanelAbstract(LinkHeader linkHeader){
//		this(linkHeader, "Monitor", 214, 210);
//	}

	protected MonitorPanelAbstract(LinkHeader linkHeader, String title,int wisth, int height) {
		this.linkHeader = linkHeader;
		addAncestorListener(new AncestorListener() {

			public void ancestorAdded(AncestorEvent arg0) {

				controller = getNewController();
				 if(statusListener!=null)
					 controller.addStatusListener(statusListener);
				Thread thread = new Thread(controller);
				thread.setPriority(thread.getPriority()-1);
				thread.start();
			}
			public void ancestorMoved(AncestorEvent arg0) {}

			public void ancestorRemoved(AncestorEvent arg0) {
				if(controller!=null && controller.isRun())
					controller.setRun(false);
			}
		});
//TODO		setBackground(new Color(51, 51, 153));
		setOpaque(false);
		setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), title, TitledBorder.LEADING, TitledBorder.TOP, FONT, Color.WHITE));
		setSize(wisth, height);
		setLayout(null);
	}

	protected abstract ControllerAbstract getNewController();

	public LinkHeader getLinkHeader() {
		return linkHeader;
	}

	public void addStatusListener(ValueChangeListener valueChangeListener) {
		statusListener = valueChangeListener;
		if(controller!=null)
			controller.addStatusListener(valueChangeListener);
	}
}
