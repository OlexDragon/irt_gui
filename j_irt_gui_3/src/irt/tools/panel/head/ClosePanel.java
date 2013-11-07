package irt.tools.panel.head;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;


@SuppressWarnings("serial")
public class ClosePanel extends JPanel {

//	private JFrame fraim;

	/**
	 * Create the panel.
	 */
	public ClosePanel(JFrame fraim) {
//		this.fraim = fraim;

		setOpaque(false);
		setSize(25, 25);
//		SwitchBox pin = new SwitchBox(new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/pin.gif")).getImage(), new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/pin-in.png")).getImage());
//		pin.setBounds(0, 0, 37, 40);
//		pin.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				ClosePanel.this.fraim.setAlwaysOnTop(((SwitchBox) e.getSource()).isSelected());
//			}
//		});
//		add(pin);

		JButton btnClose = new JButton("X");
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		btnClose.setFont(new Font("Tahoma", Font.BOLD, 15));
		btnClose.setBackground(new Color(0xEE, 0x3D, 0x47));
		btnClose.setForeground(Color.WHITE);
		btnClose.setMargin(new Insets(0, 0, 0, 0));
		btnClose.setFocusPainted(false);
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addComponent(btnClose, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(93, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addComponent(btnClose)
					.addContainerGap(27, Short.MAX_VALUE))
		);
		setLayout(groupLayout);
	}

}
