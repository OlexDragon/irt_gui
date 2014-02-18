package irt.tools.panel.wizards.address;

import irt.controller.AlarmsController;
import irt.controller.DefaultController;
import irt.controller.GuiController;
import irt.controller.control.ControllerAbstract;
import irt.controller.control.ControllerAbstract.Style;
import irt.controller.serial_port.value.getter.Getter;
import irt.data.PacketThread;
import irt.data.PacketWork;
import irt.data.packet.LinkHeader;
import irt.data.packet.LinkedPacket;
import irt.data.packet.Packet;
import irt.data.packet.PacketHeader;

import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class AutoAddressPanel extends JPanel {
	private static final long serialVersionUID = -2493955599780162739L;

	private static final Logger logger = (Logger) LogManager.getLogger();

	private List<ControllerAbstract> controllers = new ArrayList<>();
	private List<Byte> address = new ArrayList<>();

	private JProgressBar progressBar;

	private volatile boolean run = true;

	public AutoAddressPanel() {
		
		JLabel lblWaitPlease = new JLabel("Wait Please.");
		lblWaitPlease.setFont(new Font("Tahoma", Font.BOLD, 18));
		lblWaitPlease.setHorizontalAlignment(SwingConstants.CENTER);
		
		progressBar = new JProgressBar();
		progressBar.setMaximum(AddressWizard.MAX_ADDRESS);
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(lblWaitPlease, GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(10)
					.addComponent(progressBar, GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
					.addGap(10))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(40)
					.addComponent(lblWaitPlease)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(progressBar, GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
					.addGap(49))
		);
		setLayout(groupLayout);

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				logger.entry();
				new SwingWorker<Void, Void>() {

					@Override
					protected Void doInBackground() throws Exception {
						LinkHeader linkHeader;
						PacketWork packetWork;
						run = true;
						address.clear();

						for(int i=0; i<AddressWizard.MAX_ADDRESS; i++){

							logger.trace("Run={}", run);
							if(!run)
								break;

							linkHeader = new LinkHeader((byte)i, (byte)0, (short) 0);
							packetWork = new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_ALARM, AlarmsController.ALARMS_SUMMARY_STATUS, PacketWork.PACKET_ID_ALARMS_SUMMARY) {
								@Override
								public Integer getPriority() {
									return Integer.MAX_VALUE;
								}

								@Override
								public boolean set(Packet packet) {
									logger.entry(packet);
									if(isAddressEquals(packet))
										new AddressWorker(packet).execute();
									return logger.exit(true);
								}};

							DefaultController target = new DefaultController("Address Checker N"+i, packetWork, Style.CHECK_ONCE);
							synchronized (logger) {
								if(run)
									controllers.add(target);
							}
							Thread t = new Thread(target);
							t.setDaemon(true);
							t.start();
						}
						return null;
					}
				}.execute();
				logger.exit();
			}
		});
	}

	public void stop() {
		logger.debug("*** Stop ***");
		synchronized (logger) {
			for(ControllerAbstract c:controllers){
				c.setRun(false);
			}
			clear();
			GuiController.getComPortThreadQueue().clear();
		}
	}

	private void clear(){
		logger.debug("*** Clear ***");
		run = false;
		controllers.clear();
	}
	//***************************************************************************************************
	private class AddressWorker extends SwingWorker<Void, Void>{


		private final Logger logger = (Logger) LogManager.getLogger();
		private Packet packet;

		public AddressWorker(Packet packet) {
			this.packet = packet;
		}

		@Override
		protected Void doInBackground() throws Exception {
			logger.error(logger.getName());
			logger.trace(packet);
			if(packet!=null && packet instanceof LinkedPacket){
				LinkedPacket lp = (LinkedPacket)packet;
				PacketHeader header = lp.getHeader();
				byte addr = lp.getLinkHeader().getAddr();
				if(header.getType()==Packet.IRT_SLCP_PACKET_TYPE_RESPONSE)
					address.add(addr);
				new StopController(addr);
				int size;
				synchronized (logger) {
					size = controllers.size();
				}
				progressBar.setValue(AddressWizard.MAX_ADDRESS-size);
				logger.trace("counter={}", size);
			}
			return null;
		}
	}

	//*******************************************************************
	private class StopController extends Thread {

		private byte addr;

		public StopController(byte addr) {
			this.addr = addr;
			setDaemon(true);
			start();
		}

		@Override
		public void run() {
			synchronized (logger) {
				for(ControllerAbstract c:controllers){
					try{
						if(!run)
							break;

						PacketWork packetWork = c.getPacketWork();
						if(packetWork!=null){
							PacketThread packetThread = packetWork.getPacketThread();
							if(packetThread!=null){
								LinkedPacket lp = (LinkedPacket) packetThread.getPacket();

								if(lp!=null && lp.getLinkHeader().getAddr()==addr){
									c.setRun(false);
									controllers.remove(c);
									break;
								}
							}
						}

					}catch(Exception ex){
						logger.catching(ex);
					}
				}
			}
		}
		
	}
}
