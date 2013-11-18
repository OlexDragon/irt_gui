package irt.tools.panel.subpanel;

import irt.controller.AlarmsController;
import irt.controller.interfaces.Refresh;
import irt.controller.translation.Translation;
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

public class AlarmsPanel extends JPanel implements Refresh{
	private static final int ALARMS_WIDTH = 90;

	private static final long serialVersionUID = -3029893758378178725L;

	protected final Logger logger = (Logger) LogManager.getLogger();

	private AlarmsController alarmsController;

	private JLabel lblPllOutOfLockTxt;

	private JLabel lblOverCurrentTxt;

	private JLabel lblUnderCurrentTxt;

	private JLabel lblOverTemperatureTxt;

	private JLabel lblPllOutOffLock;

	private JLabel lblOverCurrent;

	private JLabel lblUnderCurrent;

	private JLabel lblOverTemperature;
	
	public AlarmsPanel(final LinkHeader linkHeader) {

		Font font = Translation.getFont().deriveFont(14f);

		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel.setName("PLL Out Off Lock");
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_1.setName("Over-Current");
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_2.setName("Under-Current");
		
		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_3.setName("Over-Temperature");
		
		JPanel panel_4 = new JPanel();
		panel_4.setName("Over-Temperature");
		panel_4.setBorder(new LineBorder(new Color(0, 0, 0)));
		
		JLabel label = new JLabel(Translation.getValue(String.class, "other", "Other"));
		label.setForeground(Color.BLUE);
		label.setFont(new Font("Tahoma", Font.BOLD, 14));

		String noAlarmTxt = Translation.getValue(String.class, "no_alarm", "No Alarm");

		JLabel label_1 = new JLabel(noAlarmTxt);
		label_1.setOpaque(true);
		label_1.setName("Other");
		label_1.setHorizontalAlignment(SwingConstants.CENTER);
		label_1.setForeground(new Color(204, 204, 204));
		label_1.setFont(new Font("Tahoma", Font.BOLD, 14));
		GroupLayout gl_panel_4 = new GroupLayout(panel_4);
		gl_panel_4.setHorizontalGroup(
			gl_panel_4.createParallelGroup(Alignment.TRAILING)
				.addGap(0, 278, Short.MAX_VALUE)
				.addGroup(gl_panel_4.createSequentialGroup()
					.addContainerGap()
					.addComponent(label, GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(label_1, 90, 90, 90))
		);
		gl_panel_4.setVerticalGroup(
			gl_panel_4.createParallelGroup(Alignment.LEADING)
				.addGap(0, 25, Short.MAX_VALUE)
				.addGroup(gl_panel_4.createSequentialGroup()
					.addGap(2)
					.addGroup(gl_panel_4.createParallelGroup(Alignment.BASELINE)
						.addComponent(label)
						.addComponent(label_1))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		panel_4.setLayout(gl_panel_4);
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(panel, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
						.addComponent(panel_1, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
						.addComponent(panel_2, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
						.addComponent(panel_3, GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
						.addComponent(panel_4, GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE))
					.addGap(10))
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
					.addGap(18)
					.addComponent(panel_4, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(69, Short.MAX_VALUE))
		);
		groupLayout.linkSize(SwingConstants.VERTICAL, new Component[] {panel, panel_1, panel_2, panel_3});
		
		lblOverTemperatureTxt = new JLabel(Translation.getValue(String.class, "over_temperatute", "Over-Temperature"));
		lblOverTemperatureTxt.setForeground(new Color(0, 0, 255));
		lblOverTemperatureTxt.setFont(font);
		
		lblOverTemperature = new JLabel(noAlarmTxt);
		lblOverTemperature.setEnabled(false);
		lblOverTemperature.setOpaque(true);
		lblOverTemperature.setName("Over-Temperature");
		lblOverTemperature.setHorizontalAlignment(SwingConstants.CENTER);
		lblOverTemperature.setForeground(new Color(204, 204, 204));
		lblOverTemperature.setFont(font);
		GroupLayout gl_panel_3 = new GroupLayout(panel_3);
		gl_panel_3.setHorizontalGroup(
			gl_panel_3.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_panel_3.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblOverTemperatureTxt, GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblOverTemperature, ALARMS_WIDTH, ALARMS_WIDTH, ALARMS_WIDTH)
					)
		);
		gl_panel_3.setVerticalGroup(
			gl_panel_3.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_3.createSequentialGroup()
					.addGap(2)
					.addGroup(gl_panel_3.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblOverTemperatureTxt)
						.addComponent(lblOverTemperature))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		panel_3.setLayout(gl_panel_3);
		
		lblUnderCurrentTxt = new JLabel(Translation.getValue(String.class, "under_current", "Under-Current"));
		lblUnderCurrentTxt.setForeground(new Color(0, 0, 255));
		lblUnderCurrentTxt.setFont(font);
		
		lblUnderCurrent = new JLabel(noAlarmTxt);
		lblUnderCurrent.setEnabled(false);
		lblUnderCurrent.setOpaque(true);
		lblUnderCurrent.setName("Under-Current");
		lblUnderCurrent.setHorizontalAlignment(SwingConstants.CENTER);
		lblUnderCurrent.setForeground(new Color(204, 204, 204));
		lblUnderCurrent.setFont(font);
		GroupLayout gl_panel_2 = new GroupLayout(panel_2);
		gl_panel_2.setHorizontalGroup(
			gl_panel_2.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_panel_2.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblUnderCurrentTxt, GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblUnderCurrent, ALARMS_WIDTH, ALARMS_WIDTH, ALARMS_WIDTH)
					)
		);
		gl_panel_2.setVerticalGroup(
			gl_panel_2.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_2.createSequentialGroup()
					.addGap(2)
					.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblUnderCurrentTxt)
						.addComponent(lblUnderCurrent))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		panel_2.setLayout(gl_panel_2);
		
		lblOverCurrentTxt = new JLabel(Translation.getValue(String.class, "over_current", "Over-Current"));
		lblOverCurrentTxt.setForeground(new Color(0, 0, 255));
		lblOverCurrentTxt.setFont(font);

		lblOverCurrent = new JLabel(noAlarmTxt);
		lblOverCurrent.setEnabled(false);
		lblOverCurrent.setOpaque(true);
		lblOverCurrent.setName("Over-Current");
		lblOverCurrent.setHorizontalAlignment(SwingConstants.CENTER);
		lblOverCurrent.setForeground(new Color(204, 204, 204));
		lblOverCurrent.setFont(font);
		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
		gl_panel_1.setHorizontalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_panel_1.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblOverCurrentTxt, GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblOverCurrent, ALARMS_WIDTH, ALARMS_WIDTH, ALARMS_WIDTH)
					)
		);
		gl_panel_1.setVerticalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addGap(2)
					.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblOverCurrentTxt)
						.addComponent(lblOverCurrent))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		panel_1.setLayout(gl_panel_1);

		lblPllOutOfLockTxt = new JLabel(Translation.getValue(String.class, "pll_out_of_lock", "PLL Out of Lock"));
		lblPllOutOfLockTxt.setForeground(new Color(0, 0, 255));
		lblPllOutOfLockTxt.setFont(font);
		
		lblPllOutOffLock = new JLabel(noAlarmTxt);
		lblPllOutOffLock.setEnabled(false);
		
		lblPllOutOffLock.setOpaque(true);
		lblPllOutOffLock.setName("PLL Out Off Lock");
		lblPllOutOffLock.setHorizontalAlignment(SwingConstants.CENTER);
		lblPllOutOffLock.setForeground(new Color(204, 204, 204));
		lblPllOutOffLock.setFont(font);
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.TRAILING)
				.addGroup(Alignment.LEADING, gl_panel.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblPllOutOfLockTxt, GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblPllOutOffLock, ALARMS_WIDTH, ALARMS_WIDTH, ALARMS_WIDTH)
					)
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(2)
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblPllOutOfLockTxt)
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
					int priority = t.getPriority();
					if(priority>Thread.MIN_PRIORITY)
						t.setPriority(priority-1);
					t.setDaemon(true);
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

	@Override
	public void refresh() {
		lblPllOutOfLockTxt.setText(Translation.getValue(String.class, "pll_out_of_lock", "PLL Out of Lock"));
		lblOverCurrentTxt.setText(Translation.getValue(String.class, "over_current", "Over-Current"));
		lblUnderCurrentTxt.setText(Translation.getValue(String.class, "under_current", "Under-Current"));
		lblOverTemperatureTxt.setText(Translation.getValue(String.class, "over_temperatute", "Over-Temperature"));

		Font font = Translation.getFont().deriveFont(14f);

		lblPllOutOfLockTxt.setFont(font);
		lblOverCurrentTxt.setFont(font);
		lblUnderCurrentTxt.setFont(font);
		lblOverTemperatureTxt.setFont(font);

		lblPllOutOffLock.setFont(font);
		lblOverCurrent.setFont(font);
		lblUnderCurrent.setFont(font);
		lblOverTemperature.setFont(font);
	}
}
