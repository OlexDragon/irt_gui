package irt.tools.panel.subpanel;

import java.awt.GridLayout;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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

	private final 	ComPortThreadQueue 		cptq 					= GuiControllerAbstract.getComPortThreadQueue();
	private final 	ScheduledExecutorService scheduledThreadPool 	= Executors.newScheduledThreadPool(1);
	private final 	ScheduledFuture<?> 		scheduleAtFixedRate;
	private final 	PacketSender 			alarmGetter 			= new PacketSender();

	private final AlarmsIDsPacket packetWork;

	private final byte 			linkAddr;
	private final AlarmsPanel 	thisPanel;

	public AlarmsPanel(final int deviceType, final LinkHeader linkHeader) {

		thisPanel = this;
		setLayout(new GridLayout(0, 1, 0, 0));
		linkAddr = linkHeader!=null ? linkHeader.getAddr() : 0;

		packetWork = new AlarmsIDsPacket(linkAddr);

		addAncestorListener(new AncestorListener() {
			public void ancestorMoved(AncestorEvent arg0) { }

			public void ancestorAdded(AncestorEvent arg0) {
			}
			public void ancestorRemoved(AncestorEvent arg0) {
				scheduleAtFixedRate.cancel(true);
			}
		});

		scheduleAtFixedRate = scheduledThreadPool.scheduleAtFixedRate(alarmGetter, 1, 2000, TimeUnit.MILLISECONDS);
		new AlarmIDsGetter();
	}

	@Override
	public void refresh() {
	}

	//********************************   PacketSender   ******************************************
	private class PacketSender implements Runnable{

		@Override
		public void run() {
			
			cptq.add(packetWork);

		}

	}

	//********************************   AlarmSetter   ******************************************
	private class AlarmIDsGetter implements PacketListener {

		public AlarmIDsGetter() {
			cptq.addPacketListener(this);
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

			synchronized (logger) {
				if(!scheduleAtFixedRate.isCancelled())
					if(packet!=null && packet.getHeader().getGroupId()==PacketImp.GROUP_ID_ALARM)
						if(packet.getHeader().getOption()==PacketImp.NO_ERROR){
							final Payload payload = packet.getPayload(0);

							if(payload.getParameterHeader().getCode()==PacketImp.ALARMS_IDs){
								logger.trace(packet);

								final short[] ids = payload.getArrayShort();
								for(Short id:ids)
									publish(id);

								scheduleAtFixedRate.cancel(true);
							}

						}else
							logger.warn("\n\t The Packet has error: {}", packet);
			}
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
