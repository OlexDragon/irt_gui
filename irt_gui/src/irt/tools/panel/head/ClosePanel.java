package irt.tools.panel.head;

import irt.irt_gui.IrtGui;
import irt.tools.CheckBox.SwitchBox;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;


@SuppressWarnings("serial")
public class ClosePanel extends JPanel {

	private JFrame fraim;

	/**
	 * Create the panel.
	 */
	public ClosePanel(JFrame fraim) {
		this.fraim = fraim;

		setOpaque(false);
		setSize(80, 50);
		setLayout(null);
		SwitchBox pin = new SwitchBox(new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/pin.gif")).getImage(), new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/pin-in.png")).getImage());
		pin.setBounds(0, 0, 37, 40);
		pin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ClosePanel.this.fraim.setAlwaysOnTop(((SwitchBox) e.getSource()).isSelected());
			}
		});
		add(pin);

		JButton btnClose = new JButton("X");
		btnClose.setBounds(56, 0, 23, 23);
		btnClose.setFont(new Font("Tahoma", Font.BOLD, 15));
		btnClose.setBackground(new Color(0xEE, 0x3D, 0x47));
		btnClose.setForeground(Color.WHITE);
		btnClose.setMargin(new Insets(0, 0, 0, 0));
		btnClose.setFocusPainted(false);
		btnClose.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				System.exit(0);
			}
		});
		add(btnClose);
	}

}
