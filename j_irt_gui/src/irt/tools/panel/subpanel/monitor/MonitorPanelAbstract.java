package irt.tools.panel.subpanel.monitor;

import irt.controller.GuiController;
import irt.controller.control.ControllerAbstract;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.irt_gui.IrtGui;
import irt.tools.panel.PicobucPanel;
import irt.tools.panel.head.HeadPanel;
import irt.tools.panel.head.IrtPanel;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

@SuppressWarnings("serial")
public abstract class MonitorPanelAbstract extends JPanel {

	protected ControllerAbstract controller;
	private LinkHeader linkHeader;
	private ValueChangeListener statusListener;
	protected TitledBorder titledBorder;

	protected Font font;
	protected Properties properties = getProperties();
	protected String selectedLanguage;

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

		selectedLanguage = setFont();
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

	protected String setFont() {
		String selectedLanguage = GuiController.getPrefs().get("locate", "en_US");
		
		String fontURL = HeadPanel.properties.getProperty("font_path_"+selectedLanguage);
		if(fontURL!=null)
		try {
			URL resource = IrtGui.class.getResource(fontURL);
			font = Font.createFont(Font.TRUETYPE_FONT, resource.openStream());
			int fontStyle = IrtPanel.fontStyle.get(properties.getProperty("font_style_"+selectedLanguage));
			float fontSize = Float.parseFloat(properties.getProperty("font_size_"+selectedLanguage));
			font = font.deriveFont(fontStyle).deriveFont(fontSize);
		} catch (FontFormatException | IOException e) {
			e.printStackTrace();
		}
		return selectedLanguage;
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
		selectedLanguage = setFont();
		titledBorder.setTitleFont(font);
	}
}
