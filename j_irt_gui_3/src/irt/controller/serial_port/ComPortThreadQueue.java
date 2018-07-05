package irt.controller.serial_port;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.event.EventListenerList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiController;
import irt.data.MyThreadFactory;
import irt.data.listener.PacketListener;
import irt.data.packet.PacketSuper;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketWork;
import irt.data.packet.interfaces.Packet;
import irt.tools.panel.head.Console;
import jssc.SerialPortException;
import purejavacomm.PortInUseException;

public class ComPortThreadQueue implements Runnable {

	public static final String GUI_IS_CLOSED_CORRECTLY = "gui is closed correctly";

	private final static Logger logger = LogManager.getLogger();

	public final static int QUEUE_SIZE_TO_DELAY = 5;
	public final static int DELAY_TIMES = 10;
	

	private PriorityBlockingQueue<PacketWork> comPortQueue = new PriorityBlockingQueue<>(300, Collections.reverseOrder());
	private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(new MyThreadFactory());
	private ScheduledFuture<?> scheduledFuture;

	private static SerialPortInterface serialPort;

	private final static Preferences prefs = GuiController.getPrefs();

	public ComPortThreadQueue(){
		scheduledFuture = service.scheduleAtFixedRate(this, 10, 10, TimeUnit.MILLISECONDS);
	}

	@Override
	public void run() {

		try {
			PacketWork packetWork = comPortQueue.take();
			logger.debug(packetWork);

			Optional
			.ofNullable(serialPort)
			.filter(SerialPortInterface::isOpened)
			.ifPresent(sp->{
				
				Optional
				.ofNullable(packetWork)
				.map(sp::send)
				.ifPresent(
						p->{
							logger.trace(p);
							firePacketListener(p);
						});
			});

		} catch (InterruptedException e) {
			Optional.ofNullable(serialPort).filter(sp->sp.isOpened()).ifPresent(sp->sp.closePort());
		} catch (Exception e) {
			logger.catching(e);
			Console.appendLn(e.getLocalizedMessage(), "ComPortThreadQueue:run");
		}
	}

	public synchronized void add(PacketWork packetWork){
//		logger.error(packetWork);
//		if(packetWork instanceof DeviceDebugInfoPacket)
//			logger.debug(packetWork.getClass().getSimpleName());//catching(new Throwable());

		try {

			final Optional<PacketSuper> oPacket = Optional.of(packetWork).filter(PacketSuper.class::isInstance).map(PacketSuper.class::cast);

			if(oPacket.map(PacketSuper::getHeader).map(PacketHeader::getPacketType).filter(pt->pt==PacketImp.PACKET_TYPE_COMMAND).isPresent()){

				comPortQueue.add(packetWork);
				return;
			}

			if (comPortQueue.size() < 100){
				if (!comPortQueue.contains(packetWork)) {

					packetWork.getPacketThread().start();

					oPacket.ifPresent(p->p.setTimestamp(System.nanoTime()));

					comPortQueue.add(packetWork);

					logger.trace("<<< is added - {}", packetWork);
				} else
					logger.warn("Tis packet already in the queue. {}", packetWork);
			}else
				logger.error("comPortQueue is FULL");

		} catch (Exception e) {
			logger.catching(e);
			Console.appendLn(e.getLocalizedMessage(), "ComPortQueue:add");
		}
	}

	public synchronized void clear(){
		comPortQueue.clear();
	}

	public int size() {
		return comPortQueue.size();
	}

	public static SerialPortInterface getSerialPort() {
		return serialPort;
	}

	public synchronized void setSerialPort(SerialPortInterface serialPort) {

		// Close old serial p[ort
		Optional.ofNullable(ComPortThreadQueue.serialPort).ifPresent(sp->{
			sp.closePort();
		});
		clear();

		ComPortThreadQueue.serialPort = serialPort;

		Optional
		.ofNullable(serialPort)
		.filter(sp->sp.getPortName().startsWith("COM") || sp.getPortName().startsWith("/dev"))
		.ifPresent(sp->{

			try {

				sp.openPort();

			} catch (PortInUseException | SerialPortException e) {

				logger.catching(e);

				final String errorMessage = String.format("Serial port %s is in use.", sp);

				new SwingWorker<Object, Object>() {

					@Override
					protected Object doInBackground() throws Exception {
						JOptionPane.showMessageDialog(null, errorMessage);
						return null;
					}
				}.execute();

			} catch (Exception e) {
				logger.catching(e);
				Console.appendLn(e.getLocalizedMessage(), "ComPortQueue:setSerialPort 2");
			}
		});
	}
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private EventListenerList packetListeners = new EventListenerList();

	public void addPacketListener(PacketListener packetListener) {

		// Add if not exists
		if(Arrays.stream(packetListeners.getListenerList()).parallel().filter(l->l.equals(packetListener)).map(l->false).findAny().orElse(true))
			packetListeners.add(PacketListener.class, packetListener);
	}

	public void removePacketListener(PacketListener packetListener) {
		packetListeners.remove(PacketListener.class, packetListener);
	}

	public void firePacketListener(Packet packet) {

		Arrays
		.stream(packetListeners.getListenerList())
		.parallel()
		.filter(PacketListener.class::isInstance)
		.map(PacketListener.class::cast)
		.forEach(l->{
			try{

				l.onPacketRecived(packet);

			}catch (Exception e) {
				logger.catching(e);
			}
		});
	}
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public void close() {

		new Thread(()->Optional.ofNullable(serialPort).filter(sp->sp.isOpened()).ifPresent(sp->sp.closePort())).start();
		comPortQueue.clear();
	}

	public void stop() {
		prefs.putBoolean(GUI_IS_CLOSED_CORRECTLY, true);

		close();
		Optional.of(scheduledFuture).filter(shf->!shf.isCancelled()).ifPresent(serv->serv.cancel(true));
		Optional.of(service).filter(serv->!serv.isShutdown()).ifPresent(serv->serv.shutdownNow());
	}
}
