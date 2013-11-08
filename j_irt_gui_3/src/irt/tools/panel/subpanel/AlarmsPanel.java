package irt.tools.panel.subpanel;

import irt.controller.AlarmsController;
import irt.data.packet.LinkHeader;

import java.awt.Color;
import java.awt.Font;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.awt.Component;
import javax.swing.border.LineBorder;

public class AlarmsPanel extends JPanel {
	private static final int ALARMS_WIDTH = 90;

	private static final long serialVersionUID = -3029893758378178725L;

	protected final Logger logger = (Logger) LogManager.getLogger();

	private AlarmsController alarmsController;
	
	public AlarmsPanel(final LinkHeader linkHeader) {
		
		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel.setName("PLL Out Off Lock");
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_1.setName("Ower-Current");
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_2.setName("Under-Current");
		
		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_3.setName("Ower-Temperature");
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(panel, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
						.addComponent(panel_1, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
						.addComponent(panel_2, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
						.addComponent(panel_3, GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(20)
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 25, 25)
					.addGap(20)
					.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 25, 25)
					.addGap(20)
					.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, 25, 25)
					.addGap(20)
					.addComponent(panel_3, GroupLayout.PREFERRED_SIZE, 25, 25)
					.addContainerGap(4, Short.MAX_VALUE))
		);
		groupLayout.linkSize(SwingConstants.VERTICAL, new Component[] {panel, panel_1, panel_2, panel_3});
		
		JLabel label_6 = new JLabel("Ower-Temperature");
		label_6.setForeground(new Color(0, 0, 255));
		label_6.setFont(new Font("Tahoma", Font.BOLD, 14));
		
		JLabel lblOwerTemperature = new JLabel("No Alarm");
		lblOwerTemperature.setEnabled(false);
		lblOwerTemperature.setOpaque(true);
		lblOwerTemperature.setName("Ower-Temperature");
		lblOwerTemperature.setHorizontalAlignment(SwingConstants.CENTER);
		lblOwerTemperature.setForeground(new Color(204, 204, 204));
		lblOwerTemperature.setFont(new Font("Tahoma", Font.BOLD, 14));
		GroupLayout gl_panel_3 = new GroupLayout(panel_3);
		gl_panel_3.setHorizontalGroup(
			gl_panel_3.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_panel_3.createSequentialGroup()
					.addContainerGap()
					.addComponent(label_6, GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblOwerTemperature, ALARMS_WIDTH, ALARMS_WIDTH, ALARMS_WIDTH)
					)
		);
		gl_panel_3.setVerticalGroup(
			gl_panel_3.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_3.createSequentialGroup()
					.addGap(2)
					.addGroup(gl_panel_3.createParallelGroup(Alignment.BASELINE)
						.addComponent(label_6)
						.addComponent(lblOwerTemperature))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		panel_3.setLayout(gl_panel_3);
		
		JLabel label_4 = new JLabel("Under-Current");
		label_4.setForeground(new Color(0, 0, 255));
		label_4.setFont(new Font("Tahoma", Font.BOLD, 14));
		
		JLabel lblUnderCurrent = new JLabel("No Alarm");
		lblUnderCurrent.setEnabled(false);
		lblUnderCurrent.setOpaque(true);
		lblUnderCurrent.setName("Under-Current");
		lblUnderCurrent.setHorizontalAlignment(SwingConstants.CENTER);
		lblUnderCurrent.setForeground(new Color(204, 204, 204));
		lblUnderCurrent.setFont(new Font("Tahoma", Font.BOLD, 14));
		GroupLayout gl_panel_2 = new GroupLayout(panel_2);
		gl_panel_2.setHorizontalGroup(
			gl_panel_2.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_panel_2.createSequentialGroup()
					.addContainerGap()
					.addComponent(label_4, GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblUnderCurrent, ALARMS_WIDTH, ALARMS_WIDTH, ALARMS_WIDTH)
					)
		);
		gl_panel_2.setVerticalGroup(
			gl_panel_2.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_2.createSequentialGroup()
					.addGap(2)
					.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
						.addComponent(label_4)
						.addComponent(lblUnderCurrent))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		panel_2.setLayout(gl_panel_2);
		
		JLabel label_2 = new JLabel("Ower-Current");
		label_2.setForeground(new Color(0, 0, 255));
		label_2.setFont(new Font("Tahoma", Font.BOLD, 14));
		
		JLabel lblOwerCurrent = new JLabel("No Alarm");
		lblOwerCurrent.setEnabled(false);
		lblOwerCurrent.setOpaque(true);
		lblOwerCurrent.setName("Ower-Current");
		lblOwerCurrent.setHorizontalAlignment(SwingConstants.CENTER);
		lblOwerCurrent.setForeground(new Color(204, 204, 204));
		lblOwerCurrent.setFont(new Font("Tahoma", Font.BOLD, 14));
		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
		gl_panel_1.setHorizontalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_panel_1.createSequentialGroup()
					.addContainerGap()
					.addComponent(label_2, GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblOwerCurrent, ALARMS_WIDTH, ALARMS_WIDTH, ALARMS_WIDTH)
					)
		);
		gl_panel_1.setVerticalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addGap(2)
					.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
						.addComponent(label_2)
						.addComponent(lblOwerCurrent))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		panel_1.setLayout(gl_panel_1);
		
		JLabel lblPllOutOf = new JLabel("PLL Out of Lock");
		lblPllOutOf.setForeground(new Color(0, 0, 255));
		lblPllOutOf.setFont(new Font("Tahoma", Font.BOLD, 14));
		
		JLabel lblPllOutOffLock = new JLabel("No Alarm");
		lblPllOutOffLock.setEnabled(false);
		
		lblPllOutOffLock.setOpaque(true);
		lblPllOutOffLock.setName("PLL Out Off Lock");
		lblPllOutOffLock.setHorizontalAlignment(SwingConstants.CENTER);
		lblPllOutOffLock.setForeground(new Color(204, 204, 204));
		lblPllOutOffLock.setFont(new Font("Tahoma", Font.BOLD, 14));
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.TRAILING)
				.addGroup(Alignment.LEADING, gl_panel.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblPllOutOf, GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblPllOutOffLock, ALARMS_WIDTH, ALARMS_WIDTH, ALARMS_WIDTH)
					)
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(2)
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblPllOutOf)
						.addComponent(lblPllOutOffLock))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		panel.setLayout(gl_panel);
		setLayout(groupLayout);
		addAncestorListener(new AncestorListener() {
			public void ancestorMoved(AncestorEvent arg0) { }

			public void ancestorAdded(AncestorEvent arg0) {
				try {
					alarmsController = new AlarmsController(linkHeader, AlarmsPanel.this);
					Thread t = new Thread(alarmsController);
					t.start();
				} catch (Exception e) {
					logger.catching(e);
				}
			}
			public void ancestorRemoved(AncestorEvent arg0) {
				if(alarmsController!=null){
					alarmsController.setRun(false);
					alarmsController = null;
				}
			}
		});

	}
}
