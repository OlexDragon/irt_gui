package irt.tools.panel.subpanel.monitor;

import irt.controller.control.ControllerAbstract;
import irt.controller.interfaces.Refresh;
import irt.controller.translation.Translation;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.tools.panel.PicobucPanel;
import irt.tools.panel.head.IrtPanel;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

@SuppressWarnings("serial")
public abstract class MonitorPanelAbstract extends JPanel implements Refresh  {

	protected ControllerAbstract controller;
	private LinkHeader linkHeader;
	private ValueChangeListener statusListener;
	protected TitledBorder titledBorder;

	protected Properties properties = getProperties();

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

		Font font = Translation.getFont()
				.deriveFont(new Float(properties.getProperty("titledBorder.font.size")))
				.deriveFont(IrtPanel.fontStyle.get(properties.getProperty("titledBorder.font.type")));

		titledBorder = new TitledBorder(UIManager.getBorder("TitledBorder.border"), title, TitledBorder.LEADING, TitledBorder.TOP, font, Color.WHITE);
		setBorder(titledBorder);
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

	private Properties getProperties() {
		Properties properties = new Properties();
		try {
			properties.load(PicobucPanel.class.getResourceAsStream("PicoBucPanel.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return properties;
	}

	public void refresh() {
		Font font = Translation.getFont()
				.deriveFont(new Float(properties.getProperty("titledBorder.font.size")))
				.deriveFont(IrtPanel.fontStyle.get(properties.getProperty("titledBorder.font.type")));
		titledBorder.setTitleFont(font);
	}
}
