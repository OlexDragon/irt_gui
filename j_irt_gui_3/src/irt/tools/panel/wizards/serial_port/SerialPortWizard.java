package irt.tools.panel.wizards.serial_port;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.TitledBorder;

import irt.controller.GuiControllerAbstract;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.controller.serial_port.ComPortPureJava.Baudrate;
import irt.data.listener.DragWorker;

public class SerialPortWizard extends JDialog {
	private static final long serialVersionUID = -7475717089138024568L;
	private final JPanel contentPanel = new JPanel();

	public SerialPortWizard() {
		setUndecorated(true);
		setName("Serial Port");
		setBounds(100, 100, 202, 96);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new TitledBorder(null, "Baudrate", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		final JComboBox<Baudrate> comboBox = new JComboBox<>();
		comboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange()==ItemEvent.SELECTED){
					GuiControllerAbstract.getComPortThreadQueue();
					ComPortThreadQueue.getSerialPort().setBaudrate(((Baudrate)comboBox.getSelectedItem()).getBaudrate());
				}
			}
		});
		comboBox.setModel(new DefaultComboBoxModel<>(Baudrate.values()));
		GuiControllerAbstract.getComPortThreadQueue();
		ComPortThreadQueue.getSerialPort();
		comboBox.setSelectedItem(Baudrate.valueOf(ComPortThreadQueue.getSerialPort().getBaudrate()));

		JLabel label = new JLabel("Baudrate(bps):");
		
		JButton btnClose = new JButton("Close");
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING)
						.addComponent(btnClose)
						.addGroup(gl_contentPanel.createSequentialGroup()
							.addComponent(label, GroupLayout.PREFERRED_SIZE, 85, GroupLayout.PREFERRED_SIZE)
							.addGap(18)
							.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap(86, Short.MAX_VALUE))
		);
		gl_contentPanel.setVerticalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addGap(14)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(label)
						.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
					.addComponent(btnClose)
					.addContainerGap())
		);
		contentPanel.setLayout(gl_contentPanel);

		new DragWorker(this, this);
		
	}
}
