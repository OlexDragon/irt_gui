package irt.tools.panel.subpanel.monitor;

import irt.controller.control.ControllerAbstract;
import irt.controller.interfaces.Refresh;
import irt.controller.translation.Translation;
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
public abstract class MonitorPanelAbstract extends JPanel implements Refresh  {

	protected ControllerAbstract controller;
	private LinkHeader linkHeader;
	private ValueChangeListener statusListener;
	protected TitledBorder titledBorder;
	protected String selectedLanguage;

//	public MonitorPanelAbstract(LinkHeader linkHeader){
//		this(linkHeader, "Monitor", 214, 210);
//	}

	protected MonitorPanelAbstract(LinkHeader linkHeader, String title,int wisth, int height) {
		setName("MonitorPanel");
		this.linkHeader = linkHeader;

		selectedLanguage = Translation.getSelectedLanguage();

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

		Font font = getTitledBorderFont();

		titledBorder = new TitledBorder(UIManager.getBorder("TitledBorder.border"), title, TitledBorder.LEADING, TitledBorder.TOP, font, Color.WHITE);
		setBorder(titledBorder);
		setSize(wisth, height);
		setLayout(null);
	}

	private Font getTitledBorderFont() {
		return Translation.getFont()
				.deriveFont(Translation.getValue(Integer.class, "titledBorder.font.size", 18))
				.deriveFont(Translation.getValue(Integer.class, "titledBorder.font.type", Font.BOLD));
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

	public void refresh() {

		selectedLanguage = Translation.getSelectedLanguage();

		Font font = getTitledBorderFont();
		titledBorder.setTitleFont(font);
	}
}
