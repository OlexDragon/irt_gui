package irt.tools.panel.subpanel;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

@SuppressWarnings("serial")
public class AlarmPanel extends JPanel {

	public AlarmPanel() {
		setOpaque(false);
		setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Alarm", TitledBorder.LEADING, TitledBorder.TOP, new Font("Tahoma", Font.PLAIN, 14), Color.WHITE));
		setSize(105, 319);
	}
}
