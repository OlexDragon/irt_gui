package irt.tools.panel.subpanel.control;

import irt.controller.translation.Translation;
import irt.data.Listeners;
import irt.data.packet.LinkHeader;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicComboBoxUI;

@SuppressWarnings("serial")
public class ControlPanelPicobuc extends ControlPanelSSPA {

	private JComboBox<String>  cbLoSelect;

	public ControlPanelPicobuc(LinkHeader linkHeader) {
		super(linkHeader, ControlPanel.FLAG_FREQUENCY);
		
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
		
		((JLabel)cbLoSelect.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
	}
}
