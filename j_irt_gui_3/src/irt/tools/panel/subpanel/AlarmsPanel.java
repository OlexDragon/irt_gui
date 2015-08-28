package irt.tools.panel.subpanel;

import irt.controller.AlarmsController;
import irt.controller.interfaces.Refresh;
import irt.controller.translation.Translation;
import irt.data.DeviceInfo;
import irt.data.RundomNumber;
import irt.data.packet.LinkHeader;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class AlarmsPanel extends JPanel implements Refresh{
	private static final int ALARMS_WIDTH = 90;

	private static final long serialVersionUID = -3029893758378178725L;

	protected final Logger logger = (Logger) LogManager.getLogger();

	private AlarmsController alarmsController;

	private JLabel lblPllOutOfLockTxt;
	private JLabel lblOverCurrentTxt;
	private JLabel lblUnderCurrentTxt;
	private JLabel lblOverTemperatureTxt;
	private JLabel lblOtherTxt;
	private JLabel lblRedundancyTxt;

	private JLabel lblPllOutOfLock;
	private JLabel lblOverCurrent;
	private JLabel lblUnderCurrent;
	private JLabel lblOverTemperature;
	private JLabel lblOther;
	private JLabel lblRedundancy;

	private int deviceType;

	public AlarmsPanel(final int deviceType, final LinkHeader linkHeader) {

		this.deviceType = deviceType;

		Font font = Translation.getFont().deriveFont(14f);

		JPanel pnlPllOutOffLock = new JPanel();
		pnlPllOutOffLock.setBorder(new LineBorder(new Color(0, 0, 0)));
		pnlPllOutOffLock.setName("PLL Out Off Lock");
		pnlPllOutOffLock.setVisible(deviceType!=DeviceInfo.DEVICE_TYPE_DLRS);

		JPanel pnlOverCurrent = new JPanel();
		pnlOverCurrent.setBorder(new LineBorder(new Color(0, 0, 0)));
		pnlOverCurrent.setName("Over-Current");
		
		JPanel pnlUnderCurrent = new JPanel();
		pnlUnderCurrent.setBorder(new LineBorder(new Color(0, 0, 0)));
		pnlUnderCurrent.setName("Under-Current");
		
		JPanel pnlOverTemperature = new JPanel();
		pnlOverTemperature.setBorder(new LineBorder(new Color(0, 0, 0)));
		pnlOverTemperature.setName("Over-Temperature");
		
		JPanel pnlOther = new JPanel();
		pnlOther.setName("Other");
		pnlOther.setBorder(new LineBorder(new Color(0, 0, 0)));
		
		lblOtherTxt = new JLabel(Translation.getValue(String.class, "other", "Other"));
		lblOtherTxt.setForeground(Color.BLUE);
		lblOtherTxt.setFont(font);

		String noAlarmTxt = Translation.getValue(String.class, "no_alarm", "No Alarm");

		lblOther = new JLabel(noAlarmTxt);
		lblOther.setEnabled(false);
		lblOther.setOpaque(true);
		lblOther.setName(AlarmsController.OTHER);
		lblOther.setHorizontalAlignment(SwingConstants.CENTER);
		lblOther.setForeground(new Color(204, 204, 204));
		lblOther.setFont(font);
		GroupLayout gl_pnlOther = new GroupLayout(pnlOther);
		gl_pnlOther.setHorizontalGroup(
			gl_pnlOther.createParallelGroup(Alignment.TRAILING)
				.addGap(0, 278, Short.MAX_VALUE)
				.addGroup(gl_pnlOther.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblOtherTxt, GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblOther, 90, 90, 90))
		);
		gl_pnlOther.setVerticalGroup(
			gl_pnlOther.createParallelGroup(Alignment.LEADING)
				.addGap(0, 25, Short.MAX_VALUE)
				.addGroup(gl_pnlOther.createSequentialGroup()
					.addGap(2)
					.addGroup(gl_pnlOther.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblOtherTxt)
						.addComponent(lblOther))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		pnlOther.setLayout(gl_pnlOther);
		
		JPanel pnlRedundancy = new JPanel();
		pnlRedundancy.setVisible(false);
		pnlRedundancy.setName("Redundancy");
		pnlRedundancy.setBorder(new LineBorder(new Color(0, 0, 0)));
		
		lblRedundancyTxt = new JLabel(Translation.getValue(String.class, "redundancy", "Redundant"));
		lblRedundancyTxt.setForeground(Color.BLUE);
		lblRedundancyTxt.setFont(font);
		
		lblRedundancy = new JLabel(noAlarmTxt);
		lblRedundancy.setEnabled(false);
		lblRedundancy.setOpaque(true);
		lblRedundancy.setName(AlarmsController.REDUNDANCY);
		lblRedundancy.setHorizontalAlignment(SwingConstants.CENTER);
		lblRedundancy.setForeground(new Color(204, 204, 204));
		lblRedundancy.setFont(font);
		GroupLayout gl_pnlRedundant = new GroupLayout(pnlRedundancy);
		gl_pnlRedundant.setHorizontalGroup(
			gl_pnlRedundant.createParallelGroup(Alignment.TRAILING)
				.addGap(0, 278, Short.MAX_VALUE)
				.addGroup(gl_pnlRedundant.createSequentialGroup()
						.addContainerGap()
					.addComponent(lblRedundancyTxt, GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblRedundancy, 90, 90, 90))
		);
		gl_pnlRedundant.setVerticalGroup(
			gl_pnlRedundant.createParallelGroup(Alignment.LEADING)
				.addGap(0, 25, Short.MAX_VALUE)
				.addGroup(gl_pnlRedundant.createSequentialGroup()
					.addGap(2)
					.addGroup(gl_pnlRedundant.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblRedundancyTxt)
						.addComponent(lblRedundancy))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		pnlRedundancy.setLayout(gl_pnlRedundant);
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(pnlPllOutOffLock, GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
							.addGap(10))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(pnlOverCurrent, GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
							.addGap(10))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(pnlUnderCurrent, GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
							.addGap(10))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(pnlOverTemperature, GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
							.addGap(10))
						.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addComponent(pnlRedundancy, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
								.addComponent(pnlOther, GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE))
							.addGap(10))))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(20)
					.addComponent(pnlPllOutOffLock, GroupLayout.PREFERRED_SIZE, 25, 25)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(pnlOverCurrent, GroupLayout.PREFERRED_SIZE, 25, 25)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(pnlUnderCurrent, GroupLayout.PREFERRED_SIZE, 25, 25)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(pnlOverTemperature, GroupLayout.PREFERRED_SIZE, 25, 25)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(pnlOther, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(pnlRedundancy, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(75, Short.MAX_VALUE))
		);
		groupLayout.linkSize(SwingConstants.VERTICAL, new Component[] {pnlPllOutOffLock, pnlOverCurrent, pnlUnderCurrent, pnlOverTemperature});
		
		lblOverTemperatureTxt = new JLabel(deviceType!=DeviceInfo.DEVICE_TYPE_DLRS ? Translation.getValue(String.class, "over_temperatute", "Over-Temperature") : Translation.getValue(String.class, "switch.waveguide", "Waveguide switch"));
		lblOverTemperatureTxt.setForeground(new Color(0, 0, 255));
		lblOverTemperatureTxt.setFont(font);
		
		lblOverTemperature = new JLabel(noAlarmTxt);
		lblOverTemperature.setEnabled(false);
		lblOverTemperature.setOpaque(true);
		lblOverTemperature.setName(AlarmsController.OVER_TEMPERATURE);
		lblOverTemperature.setHorizontalAlignment(SwingConstants.CENTER);
		lblOverTemperature.setForeground(new Color(204, 204, 204));
		lblOverTemperature.setFont(font);
		GroupLayout gl_pnlOverTemperature = new GroupLayout(pnlOverTemperature);
		gl_pnlOverTemperature.setHorizontalGroup(
			gl_pnlOverTemperature.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_pnlOverTemperature.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblOverTemperatureTxt, GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblOverTemperature, ALARMS_WIDTH, ALARMS_WIDTH, ALARMS_WIDTH)
					)
		);
		gl_pnlOverTemperature.setVerticalGroup(
			gl_pnlOverTemperature.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlOverTemperature.createSequentialGroup()
					.addGap(2)
					.addGroup(gl_pnlOverTemperature.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblOverTemperatureTxt)
						.addComponent(lblOverTemperature))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		pnlOverTemperature.setLayout(gl_pnlOverTemperature);

		lblUnderCurrentTxt = new JLabel(
				deviceType!=DeviceInfo.DEVICE_TYPE_L_TO_KU_OUTDOOR
				? Translation.getValue(String.class, "under_current", "Under-Current")
						: Translation.getValue(String.class, "alc_error", "ALC error"));
		lblUnderCurrentTxt.setForeground(new Color(0, 0, 255));
		lblUnderCurrentTxt.setFont(font);
		
		lblUnderCurrent = new JLabel(noAlarmTxt);
		lblUnderCurrent.setEnabled(false);
		lblUnderCurrent.setOpaque(true);
		lblUnderCurrent.setName(AlarmsController.UNDER_CURRENT2);
		lblUnderCurrent.setHorizontalAlignment(SwingConstants.CENTER);
		lblUnderCurrent.setForeground(new Color(204, 204, 204));
		lblUnderCurrent.setFont(font);
		GroupLayout gl_pnlUnderCurrent = new GroupLayout(pnlUnderCurrent);
		gl_pnlUnderCurrent.setHorizontalGroup(
			gl_pnlUnderCurrent.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_pnlUnderCurrent.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblUnderCurrentTxt, GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblUnderCurrent, ALARMS_WIDTH, ALARMS_WIDTH, ALARMS_WIDTH)
					)
		);
		gl_pnlUnderCurrent.setVerticalGroup(
			gl_pnlUnderCurrent.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlUnderCurrent.createSequentialGroup()
					.addGap(2)
					.addGroup(gl_pnlUnderCurrent.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblUnderCurrentTxt)
						.addComponent(lblUnderCurrent))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		pnlUnderCurrent.setLayout(gl_pnlUnderCurrent);
		
		lblOverCurrentTxt = new JLabel(
				deviceType!=DeviceInfo.DEVICE_TYPE_L_TO_KU_OUTDOOR
				? Translation.getValue(String.class, "over_current", "Over-Current")
						: Translation.getValue(String.class, "no_input", "Low input power"));
		lblOverCurrentTxt.setForeground(new Color(0, 0, 255));
		lblOverCurrentTxt.setFont(font);

		lblOverCurrent = new JLabel(noAlarmTxt);
		lblOverCurrent.setEnabled(false);
		lblOverCurrent.setOpaque(true);
		lblOverCurrent.setName(AlarmsController.OVER_CURRENT);
		lblOverCurrent.setHorizontalAlignment(SwingConstants.CENTER);
		lblOverCurrent.setForeground(new Color(204, 204, 204));
		lblOverCurrent.setFont(font);
		GroupLayout gl_pnlOverCurrent = new GroupLayout(pnlOverCurrent);
		gl_pnlOverCurrent.setHorizontalGroup(
			gl_pnlOverCurrent.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_pnlOverCurrent.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblOverCurrentTxt, GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblOverCurrent, ALARMS_WIDTH, ALARMS_WIDTH, ALARMS_WIDTH)
					)
		);
		gl_pnlOverCurrent.setVerticalGroup(
			gl_pnlOverCurrent.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlOverCurrent.createSequentialGroup()
					.addGap(2)
					.addGroup(gl_pnlOverCurrent.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblOverCurrentTxt)
						.addComponent(lblOverCurrent))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		pnlOverCurrent.setLayout(gl_pnlOverCurrent);

		String labelText = Translation.getValue(String.class, "pll_out_of_lock", "PLL Out of Lock");
		lblPllOutOfLockTxt = new JLabel(labelText);
		lblPllOutOfLockTxt.setForeground(new Color(0, 0, 255));
		lblPllOutOfLockTxt.setFont(font);
		
		lblPllOutOfLock = new JLabel(noAlarmTxt);
		lblPllOutOfLock.setEnabled(false);
		
		lblPllOutOfLock.setOpaque(true);
		lblPllOutOfLock.setName(AlarmsController.PLL_OUT_OF_LOCK2);
		lblPllOutOfLock.setHorizontalAlignment(SwingConstants.CENTER);
		lblPllOutOfLock.setForeground(new Color(204, 204, 204));
		lblPllOutOfLock.setFont(font);
		GroupLayout gl_pnlPllOutOffLock = new GroupLayout(pnlPllOutOffLock);
		gl_pnlPllOutOffLock.setHorizontalGroup(
			gl_pnlPllOutOffLock.createParallelGroup(Alignment.TRAILING)
				.addGroup(Alignment.LEADING, gl_pnlPllOutOffLock.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblPllOutOfLockTxt, GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblPllOutOfLock, ALARMS_WIDTH, ALARMS_WIDTH, ALARMS_WIDTH)
					)
		);
		gl_pnlPllOutOffLock.setVerticalGroup(
			gl_pnlPllOutOffLock.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlPllOutOffLock.createSequentialGroup()
					.addGap(2)
					.addGroup(gl_pnlPllOutOffLock.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblPllOutOfLockTxt)
						.addComponent(lblPllOutOfLock))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		pnlPllOutOffLock.setLayout(gl_pnlPllOutOffLock);
		setLayout(groupLayout);
		addAncestorListener(new AncestorListener() {
			public void ancestorMoved(AncestorEvent arg0) { }

			public void ancestorAdded(AncestorEvent arg0) {
				try {
					alarmsController = new AlarmsController(deviceType, linkHeader, AlarmsPanel.this, logger);
					Thread t = new Thread(alarmsController, "AlarmsPanel."+alarmsController.getName()+"-"+new RundomNumber());
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
					alarmsController.stop();
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
		lblOverTemperatureTxt.setText(deviceType!=DeviceInfo.DEVICE_TYPE_DLRS ? Translation.getValue(String.class, "over_temperatute", "Over-Temperature") : Translation.getValue(String.class, "switch.waveguide", "Waveguide switch"));
		lblOtherTxt.setText(deviceType!=DeviceInfo.DEVICE_TYPE_L_TO_KU_OUTDOOR ? Translation.getValue(String.class, "other", "Other") : Translation.getValue(String.class, "alc_error", "ALC error"));
		lblRedundancyTxt.setText(Translation.getValue(String.class, "redundancy", "Redundant"));

		Font font = Translation.getFont().deriveFont(14f);

		lblPllOutOfLockTxt.setFont(font);
		lblOverCurrentTxt.setFont(font);
		lblUnderCurrentTxt.setFont(font);
		lblOverTemperatureTxt.setFont(font);
		lblOtherTxt.setFont(font);
		lblRedundancyTxt.setFont(font);

		lblPllOutOfLock.setFont(font);
		lblOverCurrent.setFont(font);
		lblUnderCurrent.setFont(font);
		lblOverTemperature.setFont(font);
		lblOther.setFont(font);
		lblRedundancy.setFont(font);
	}
}
