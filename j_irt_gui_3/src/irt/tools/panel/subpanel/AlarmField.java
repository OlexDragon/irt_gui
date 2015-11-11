
package irt.tools.panel.subpanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.AlarmPacketSender;
import irt.controller.GuiControllerAbstract;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.controller.translation.Translation;
import irt.data.listener.PacketListener;
import irt.data.packet.AlarmDescriptionPacket;
import irt.data.packet.AlarmStatusPacket;
import irt.data.packet.AlarmStatusPacket.AlarmSeverities;
import irt.data.packet.Packet;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.Payload;

public class AlarmField extends JPanel {
	private static final long serialVersionUID = 4201830616544799648L;

	private final Logger logger = LogManager.getLogger();

	private final JLabel lblValue;
	private final JLabel lblTitle;
	private final short alarmId;

	private final byte linkAddr;

	public AlarmField(final byte linkAddr, final short alarmId) {
		this.alarmId = alarmId;
		this.linkAddr = linkAddr;
		setBorder(new LineBorder(new Color(0, 0, 0)));
		setLayout(new BorderLayout(0, 0));
		
		lblTitle = new JLabel("Title");
		lblTitle.setForeground(Color.BLUE);
		lblTitle.setToolTipText("Alarm ID: " + alarmId);
		
		lblValue = new JLabel("Value");
		lblValue.setHorizontalAlignment(SwingConstants.CENTER);
		lblValue.setPreferredSize(new Dimension(90, 14));
		lblValue.setOpaque(true);
		
		
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(3, 10, 3, 3));
		add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));

		panel.add(lblTitle, BorderLayout.CENTER);
		panel.add(lblValue, BorderLayout.EAST);

		addAncestorListener(new AncestorListener() {

			private AlarmPacketSender alarmController;
			private final PacketListener packetListener = new PacketListener() {
				
				@Override
				public void packetRecived(Packet packet) {
					new FieldUpdater(packet);
				}
			};

			public void ancestorAdded(AncestorEvent event) {

				alarmController = new AlarmPacketSender( linkAddr, alarmId);

				final ComPortThreadQueue comPortThreadQueue = GuiControllerAbstract.getComPortThreadQueue();
				comPortThreadQueue.addPacketListener(packetListener);
			}

			public void ancestorRemoved(AncestorEvent event) {
				final ComPortThreadQueue comPortThreadQueue = GuiControllerAbstract.getComPortThreadQueue();
				comPortThreadQueue.removePacketListener(packetListener);

				alarmController.destroy();
				alarmController = null;
			}
			public void ancestorMoved(AncestorEvent event) { }
		});

		final ComPortThreadQueue comPortThreadQueue = GuiControllerAbstract.getComPortThreadQueue();
		comPortThreadQueue.add(new AlarmDescriptionPacket(linkAddr, alarmId));
	}

	//************************************************************   FieldUpdater   *********************************************************************
	public class FieldUpdater extends Thread{

		private Packet	packet;

		public FieldUpdater(Packet packet) {

			this.packet 	= packet;

			int priority = getPriority();
			if(priority>Thread.MIN_PRIORITY)
				setPriority(--priority);
			setDaemon(true);
			start();
		}

		@Override
		public void run() {
			if(packet!=null)
				if(packet.getHeader().getGroupId()==PacketImp.GROUP_ID_ALARM){
					final PacketHeader header = packet.getHeader();
					if(header.getPacketType()==PacketImp.PACKET_TYPE_RESPONSE && header.getOption()==PacketImp.ERROR_NO_ERROR){
						logger.trace(packet);

						final Payload payload = packet.getPayload(0);
						final byte[] buffer = payload.getBuffer();
						logger.trace("\n\t {}", buffer);

						if(alarmId==buffer[1])
							switch(payload.getParameterHeader().getCode()){
							case PacketImp.ALARM_STATUS:
								setAlarmStatus(buffer[5]&7);
								break;

							case PacketImp.ALARM_DESCRIPTION:
								SwingUtilities.invokeLater(new Runnable() {
									
									@Override
									public void run() {
										lblTitle.setText(payload.getStringData().toString());
									}
								});
							}
					}else
						logger.warn("Not possible to get answer: {}", packet);
				}
		}

		private void setAlarmStatus(int alarmStatus) {

			final AlarmSeverities 	alarmSeverities = AlarmStatusPacket.AlarmSeverities.values()[alarmStatus];
			final String 			name 			= alarmSeverities.name();

			if(!lblValue.getText().equals(name)) {
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						final String text = alarmSeverities.toString();
						logger.trace("\n\t{}", text);

						lblValue.setText(Translation.getValue(String.class, text, text));
						lblValue.setName(name);
						lblValue.setForeground(alarmSeverities.getForeground());
						lblValue.setBackground(alarmSeverities.getBackground());

						if(lblTitle.getName()==null){
							final ComPortThreadQueue comPortThreadQueue = GuiControllerAbstract.getComPortThreadQueue();
							comPortThreadQueue.add(new AlarmDescriptionPacket(linkAddr, alarmId));
						}
					}
				});

			}
		}
	}
}
