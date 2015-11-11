package irt.tools.panel.subpanel;

import java.awt.GridLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import irt.controller.GuiControllerAbstract;
import irt.controller.interfaces.Refresh;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.data.listener.PacketListener;
import irt.data.packet.AlarmsIDsPacket;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.packet.PacketImp;
import irt.data.packet.Payload;

public class AlarmsPanel extends JPanel implements Refresh{

	private static final long serialVersionUID = -3029893758378178725L;

	protected final Logger logger = (Logger) LogManager.getLogger();
	private final byte 			linkAddr;
	private final AlarmsPanel 	thisPanel;

	public AlarmsPanel(final int deviceType, final LinkHeader linkHeader) {

		thisPanel = this;
		setLayout(new GridLayout(0, 1, 0, 0));
		linkAddr = linkHeader!=null ? linkHeader.getAddr() : 0;

		addAncestorListener(new AncestorListener() {
			public void ancestorMoved(AncestorEvent arg0) { }

			public void ancestorAdded(AncestorEvent arg0) {
			}
			public void ancestorRemoved(AncestorEvent arg0) {
			}
		});

		new AlarmGetter();
		new AlarmIDsListener();
	}

	@Override
	public void refresh() {
	}

	//********************************   AlarmSetter   ******************************************
	public class AlarmGetter extends Thread{

		public AlarmGetter() {

			int priority = getPriority();
			if(priority>Thread.MIN_PRIORITY)
				setPriority(--priority);
			setDaemon(true);
			start();
		}

		@Override
		public void run() {
			final ComPortThreadQueue comPortThreadQueue = GuiControllerAbstract.getComPortThreadQueue();
			comPortThreadQueue.add(new AlarmsIDsPacket(linkAddr));
			
			
		}

	}

	//********************************   AlarmSetter   ******************************************
	private class AlarmIDsListener implements PacketListener {
		final ComPortThreadQueue comPortThreadQueue = GuiControllerAbstract.getComPortThreadQueue();

		public AlarmIDsListener() {
			comPortThreadQueue.addPacketListener(this);
		}

		@Override
		public void packetRecived(Packet packet) {
			new AlarmsBuilder(packet);
		}
	}

	private class AlarmsBuilder extends SwingWorker<Void, Short>{

		private Packet packet;

		public AlarmsBuilder(Packet packet) {
			this.packet = packet;
			execute();
		}

		@Override
		protected Void doInBackground() throws Exception {

			if(packet!=null && packet.getHeader().getGroupId()==PacketImp.GROUP_ID_ALARM)
				if(packet.getHeader().getOption()==PacketImp.NO_ERROR){
					final Payload payload = packet.getPayload(0);

					if(payload.getParameterHeader().getCode()==PacketImp.ALARMS_IDs){
						logger.trace(packet);

						final short[] ids = payload.getArrayShort();
						for(Short id:ids)
							publish(id);
					}

				}else
					logger.warn("\n\t The Packet has error: {}", packet);
			return null;
		}

		@Override
		protected void process(List<Short> ids) {

			for(Short alarmId:ids){
				final AlarmField alarmField = new AlarmField(linkAddr, alarmId);
				thisPanel.add(alarmField);
				logger.trace("\n\t alarmId: {}", alarmId);
			}
		}
		
	}
}
