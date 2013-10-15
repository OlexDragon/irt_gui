package irt.tools.panel.head;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JPanel;


@SuppressWarnings("serial")
public class ClosePanel extends JPanel {

	/**
	 * Create the panel.
	 */
	public ClosePanel() {

		setOpaque(false);
		setSize(80, 50);
		setLayout(null);

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
