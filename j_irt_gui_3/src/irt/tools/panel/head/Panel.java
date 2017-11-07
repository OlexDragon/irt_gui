package irt.tools.panel.head;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import irt.controller.GuiControllerAbstract;
import irt.controller.interfaces.Refresh;
import irt.data.listener.PacketListener;
import irt.data.packet.AlarmStatusPacket.AlarmSeverities;
import irt.data.packet.Packet;
import irt.data.packet.PacketAbstract;
import irt.data.packet.Packets;
import irt.data.packet.interfaces.PacketWork;
import irt.tools.fx.MonitorPanelFx.ParameterHeaderCodeBUC;
import irt.tools.fx.MonitorPanelFx.ParameterHeaderCodeFCM;
import irt.tools.fx.MonitorPanelFx.StatusBitsBUC;
import irt.tools.fx.MonitorPanelFx.StatusBitsFCM;
import irt.tools.label.LED;
import irt.tools.label.VarticalLabel;
import irt.tools.panel.ConverterPanel;
import irt.tools.panel.PicobucPanel;

@SuppressWarnings("serial")
public class Panel extends JPanel implements PacketListener {

	private final Logger logger = (Logger) LogManager.getLogger();

//	public int MIN_WIDTH = 25;
//	public int MID_WIDTH = 310;
//	public int MAX_WIDTH = 615;
//	public int MIN_HEIGHT = 25;
//	public int MAX_HEIGHT = 444;
//	public int BTN_WIDTH;

	protected Color backgroundColor = new Color(0x0B,0x17,0x3B);

	private LED led;
	private VarticalLabel verticalLabel;
	protected JPanel userPanel;
	protected JPanel extraPanel;

	protected JLabel lblAddress;

	private JPanel panel;

	private JButton btnLeft;

	private JButton btnRight;

	private final byte addr;

	public Panel(byte addr, String verticalLabelText, int minWidth, int midWidth, int maxWidth, int minHeight, int maxHeight) {
		this.addr = addr;
		addHierarchyListener(
				hierarchyEvent->
				Optional
				.of(hierarchyEvent)
				.filter(e->(e.getChangeFlags()&HierarchyEvent.PARENT_CHANGED)!=0)
				.map(HierarchyEvent::getChanged)
				.filter(c->c instanceof ConverterPanel || c instanceof PicobucPanel)
				.filter(c->c.getParent()==null)
				.ifPresent(c->GuiControllerAbstract.getComPortThreadQueue().removePacketListener(Panel.this)));

		setBorder(null);

		setOpaque(false);

		verticalLabel = new VarticalLabel(verticalLabelText, false);
		verticalLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		verticalLabel.setOpaque(true);
		verticalLabel.setBackground(new Color(0, 153, 255));
		verticalLabel.setForeground(getForeground());
		verticalLabel.setFont(new Font("Tahoma", Font.PLAIN, 16));
		verticalLabel.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		verticalLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent mouseEvent) {
				setAllPanelsVisible(!(userPanel.isVisible() || extraPanel.isVisible()));
			}
		});

		userPanel = new JPanel();
		userPanel.addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent event) {
				resize();
			}
			public void ancestorMoved(AncestorEvent event) {
			}
			public void ancestorRemoved(AncestorEvent event) {
				resize();
			}
		});
		userPanel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentHidden(ComponentEvent e) {
				btnLeft.setText("[ ]");
				resize();
			}
			@Override
			public void componentShown(ComponentEvent e) {
				btnLeft.setText("_");
				resize();
			}
		});
		userPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, Color.WHITE, Color.BLACK));
		userPanel.setMinimumSize(new Dimension(270, 419));
		Color color = new Color(0x0B,0x17,0x3B);
		userPanel.setBackground(color);
		userPanel.setLayout(null);

		extraPanel = new JPanel();	
		extraPanel.addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent event) {
				resize();
			}
			public void ancestorMoved(AncestorEvent event) {
			}
			public void ancestorRemoved(AncestorEvent event) {
				resize();
			}
		});
		extraPanel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentHidden(ComponentEvent e) {
				btnRight.setText("[ ]");
				resize();
			}
			@Override
			public void componentShown(ComponentEvent e) {
				btnRight.setText("_");
				resize();
			}
		});
		extraPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, Color.WHITE, Color.BLACK));
		extraPanel.setBackground(new Color(0,0x33,0x33));
		extraPanel.setLayout(null);

		led = new LED(new Color(0, 153, 255), null);
		led.setName("Status Led");
		led.setOn(true);
		
		panel = new JPanel();
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(led, 25, 25, 25)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(panel, GroupLayout.PREFERRED_SIZE, 197, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(verticalLabel, 25, 25, 25)
							.addGap(3)
							.addComponent(userPanel, 270, 270, 270)
							.addGap(3)
							.addComponent(extraPanel, 305, 305, 305))))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
						.addComponent(panel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(led, GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE))
					.addGap(3)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(verticalLabel, 419, 419, 419)
						.addComponent(userPanel, 419, 419, 419)
						.addComponent(extraPanel, 419, 419, 419)))
		);
		
		JButton btnMin = new JButton("_");
		btnMin.setToolTipText("Hide the Panels");
		btnMin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnMin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try{
					setAllPanelsVisible(!(userPanel.isVisible() || extraPanel.isVisible()));
				}catch(Exception ex){
					logger.catching(ex);
				}

			}
		});
		btnMin.setBackground(SystemColor.info);
		btnMin.setMargin(new Insets(0, 0, 0, 0));
		btnMin.setFont(new Font("Tahoma", Font.BOLD, 14));
		
		btnLeft = new JButton("_");
		btnLeft.setForeground(Color.YELLOW);
		btnLeft.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnLeft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					boolean visible = !userPanel.isVisible();

					if (!visible && !extraPanel.isVisible())
						setAllPanelsVisible(false);
					else {
						userPanel.setVisible(visible);
						btnLeft.setToolTipText(visible ? "Hide the Monitor Panel" : "Show the Monitor Panel");
					}
					revalidate();
					getParent().getParent().getParent().repaint();
				} catch (Exception ex) {
					logger.catching(ex);
				}
			}
		});
		btnLeft.setMargin(new Insets(0, 0, 0, 0));
		btnLeft.setFont(new Font("Tahoma", Font.BOLD, 16));
		btnLeft.setBackground(color);
		
		btnRight = new JButton("_");
		btnRight.setForeground(Color.YELLOW);
		btnRight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					boolean visible = !extraPanel.isVisible();
					if (!visible && !userPanel.isVisible())
						setAllPanelsVisible(false);
					else {
						extraPanel.setVisible(visible);
						btnRight.setToolTipText(visible ? "Hide the Info Panel" : "Show the Info Panel");
					}
					revalidate();
					getParent().getParent().getParent().repaint();
				} catch (Exception ex) {
					logger.catching(ex);
				}
			}
		});
		btnRight.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnRight.setMargin(new Insets(0, 0, 0, 0));
		btnRight.setFont(new Font("Tahoma", Font.BOLD, 16));
		btnRight.setBackground(new Color(0,0x33,0x33));
		
		lblAddress = new JLabel("Address: ");
		lblAddress.setFont(new Font("Tahoma", Font.BOLD, 14));
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addComponent(btnMin, 25, 25, 25)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnLeft, 25, 25, 25)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnRight, 25, 25, 25)
					.addGap(18)
					.addComponent(lblAddress))
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addComponent(btnMin, 25, 25, 25)
				.addComponent(btnLeft, 25, 25, 25)
				.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
					.addComponent(btnRight, 25, 25, 25)
					.addComponent(lblAddress))
		);
		panel.setLayout(gl_panel);
		setLayout(groupLayout);

		GuiControllerAbstract.getComPortThreadQueue().addPacketListener(this);
	}

	public void setVerticalLabelForeground(Color labelForeground) {
		verticalLabel.setForeground(labelForeground);
	}

	public void setVerticalLabelBackground(Color labelBackground) {
		if(!verticalLabel.getBackground().equals(labelBackground)){
			verticalLabel.setBackground(labelBackground);
		}
	}

	public void setVerticalLabelFont(Font font) {
		verticalLabel.setFont(font);
	}

	public void setVerticalLabelText(String text) {
		verticalLabel.setText(text);
	}

	public VarticalLabel getVarticalLabel() {
		return verticalLabel;
	}

	public void refresh() {
		refresh(this);
	}

	private void refresh(Component component){
		logger.trace("* refresh({})", component.getClass().getSimpleName());
		if(component instanceof JComponent)
			for(Component c:((Container)component).getComponents()){
				if(c instanceof Refresh){
					logger.debug("{} is refreshed", c.getClass().getSimpleName());
					((Refresh)c).refresh();
				}else if(c instanceof JComponent){
					logger.trace("Next component is {}", c.getClass().getSimpleName());
					refresh(c);
				}
			}
	}

//	public void setMute(boolean isMute) {
//		this.isMute = isMute;
//		if(isMute){
//			if(alarm<AlarmsController.ALARMS_STATUS_WARNING)
//				setVerticalLabelBackground(Color.YELLOW);
//			if(led.getLedColor()!=Color.YELLOW)
//				led.setLedColor(Color.YELLOW);
//		}else
//			if(led.getLedColor()!=Color.GREEN)
//				led.setLedColor(Color.GREEN);
//	}
//	public void setAlarm(int alarm) {
//		this.alarm = alarm;
//
//		switch(alarm){
//		case AlarmsController.ALARMS_STATUS_INFO:
//		case AlarmsController.ALARMS_STATUS_NO_ALARM:
//			if(!isMute)
//				setVerticalLabelBackground(Color.GREEN);
//			break;
//		case AlarmsController.ALARMS_STATUS_WARNING:
//		case AlarmsController.ALARMS_STATUS_MINOR:
//			setVerticalLabelBackground(WARNING_COLOR);
//			break;
//		case AlarmsController.ALARMS_STATUS_ALARM:
//		case AlarmsController.ALARMS_STATUS_FAULT:
//			setVerticalLabelBackground(Color.RED);
//			break;
//		}
//	}

	private void setAllPanelsVisible(boolean visible) {
		userPanel.setVisible(visible);
		extraPanel.setVisible(visible);
		panel.setVisible(visible);
	}

	public void resize(){
		if(!getSize().equals(getPreferredSize()))
			setSize(getPreferredSize());
	}

	private AlarmSeverities alarmSeverities;

	@SuppressWarnings("unchecked")
	@Override
	public void onPacketRecived(Packet packet) {

		final Optional<? extends PacketAbstract> oP = Optional
														.of(packet)
														.map(p->p.getHeader())
														.filter(
																h->
																h.getPacketId()==PacketWork.PACKET_ID_ALARMS_SUMMARY ||
																h.getPacketId()==PacketWork.PACKET_ID_MEASUREMENT_ALL)
														.flatMap(h->Packets.cast(packet));

		final PacketAbstract p = oP.orElse(null);

		if(p==null || p.getLinkHeader().getAddr()!=addr) return;

		final Object value = p.getValue();

		if(value instanceof AlarmSeverities){
			alarmSeverities = (AlarmSeverities)value;

			final Color background = alarmSeverities.getBackground();
			setVerticalLabelBackground(background);

		}else if(value instanceof Map){

			if(alarmSeverities==null || alarmSeverities == AlarmSeverities.NO_ALARM || alarmSeverities == AlarmSeverities.INFO)
				setVerticalLabelBackground(((Map<Object, Object>)value)
					.entrySet()
					.parallelStream()
					.filter(es->(es.getKey().equals(ParameterHeaderCodeFCM.STATUS) || es.getKey().equals(ParameterHeaderCodeBUC.STATUS)))
					.map(Map.Entry::getValue)
					.filter(o->o instanceof Object[])
					.map(v->(Object[])v)
					.flatMap(Arrays::stream)
					.filter(st->(st == StatusBitsBUC.MUTE || st == StatusBitsFCM.MUTE_TTL || st == StatusBitsFCM.MUTE))
					.map(mute->Color.YELLOW)
					.findAny()
					.orElse(AlarmSeverities.NO_ALARM.getBackground()));
		}
	}
}
