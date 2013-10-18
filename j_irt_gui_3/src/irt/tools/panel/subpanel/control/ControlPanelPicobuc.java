package irt.tools.panel.subpanel.control;

import irt.controller.control.ControlControllerPicobuc;
import irt.controller.control.ControllerAbstract;
import irt.controller.translation.Translation;
import irt.data.Listeners;
import irt.data.packet.LinkHeader;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicComboBoxUI;

@SuppressWarnings("serial")
public class ControlPanelPicobuc extends ControlPanel {

	private JComboBox<String>  cbLoSelect;

	public ControlPanelPicobuc(LinkHeader linkHeader) {
		super(linkHeader, ControlPanel.FLAG_ATTENUATION);
		
		String selectedLanguage = Translation.getSelectedLanguage();
		Font font = Translation.getFont().deriveFont(16f);

		cbLoSelect = new JComboBox<String>();
		cbLoSelect.setName("LO Select");
		cbLoSelect.setUI(new BasicComboBoxUI(){ @Override protected JButton createArrowButton() { return new JButton(){ @Override public int getWidth() { return 0; }};}});
		cbLoSelect.addPopupMenuListener(Listeners.popupMenuListener);
		cbLoSelect.setForeground(Color.YELLOW);
		cbLoSelect.setBackground(color);
		cbLoSelect.setCursor(cursor);
		cbLoSelect.setFont(font);
		cbLoSelect.setBounds(10, 141, 194, 26);
		add(cbLoSelect);
		
		font = font.deriveFont(new Float(properties.getProperty("control.label.mute.font.size_"+selectedLanguage)));

		JLabel label = new JLabel(Translation.getValue(String.class, "save", "SAVE"));
		label.setHorizontalAlignment(SwingConstants.LEFT);
		label.setForeground(Color.YELLOW);
		label.setFont(font);
		label.setBounds(153, 107, 61, 20);
		add(label);
		((JLabel)cbLoSelect.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
	}

	@Override
	protected ControllerAbstract getNewController() {
		return new ControlControllerPicobuc(getLinkHeader(),this);
	}

	@Override
	protected Point setConfigButtonPosition() {
		return new Point(118, 101);
	}
}
